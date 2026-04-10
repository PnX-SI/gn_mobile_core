package fr.geonature.datasync.ui.login

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.webkit.WebChromeClient
import android.webkit.WebResourceRequest
import android.webkit.WebSettings
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.ProgressBar
import androidx.appcompat.app.AppCompatActivity
import dagger.hilt.android.AndroidEntryPoint
import fr.geonature.compat.content.getParcelableExtraCompat
import fr.geonature.datasync.R
import fr.geonature.datasync.settings.DataSyncSettings
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.tinylog.Logger
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64
import java.util.Locale

/**
 * Handles Keycloak authentication through an embedded WebView.
 */
@AndroidEntryPoint
class KeycloakLoginActivity : AppCompatActivity() {

    private lateinit var webView: WebView
    private lateinit var progressBar: ProgressBar
    private lateinit var loginBootstrapUrl: String
    private lateinit var redirectUri: String
    private lateinit var providerId: String
    private lateinit var codeVerifier: String
    private lateinit var expectedState: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_keycloak_login)

        webView = findViewById(R.id.webview)
        progressBar = findViewById(android.R.id.progress)

        val dataSyncSettings = intent.getParcelableExtraCompat<DataSyncSettings>(EXTRA_SETTINGS)
        if (dataSyncSettings == null) {
            finishWithCancel()
            return
        }

        providerId = dataSyncSettings.keycloakProviderId ?: "keycloak"
        loginBootstrapUrl = buildLoginBootstrapUrl(dataSyncSettings)
        redirectUri = dataSyncSettings.keycloakRedirectUri
            ?: "${applicationContext.packageName}://auth/callback"
        codeVerifier = generateCodeVerifier()
        expectedState = randomUrlSafeToken(24)

        configureWebView()
        CoroutineScope(Dispatchers.Main).launch {
            val authorizeUrl = withContext(Dispatchers.IO) {
                buildAuthorizeUrlFromBootstrap(loginBootstrapUrl)
            }
            if (authorizeUrl.isNullOrBlank()) {
                finishWithCancel()
                return@launch
            }
            webView.loadUrl(authorizeUrl)
        }
    }

    private fun configureWebView() {
        webView.settings.apply {
            javaScriptEnabled = true
            domStorageEnabled = true
            mixedContentMode = WebSettings.MIXED_CONTENT_COMPATIBILITY_MODE
        }

        webView.webChromeClient = object : WebChromeClient() {
            override fun onProgressChanged(view: WebView?, newProgress: Int) {
                super.onProgressChanged(view, newProgress)
                progressBar.visibility = if (newProgress >= 100) View.GONE else View.VISIBLE
            }
        }

        webView.webViewClient = object : WebViewClient() {
            override fun shouldOverrideUrlLoading(
                view: WebView?,
                request: WebResourceRequest?
            ): Boolean {
                val url = request?.url ?: return false
                return handleRedirect(url)
            }

            override fun onPageStarted(
                view: WebView?,
                url: String?,
                favicon: android.graphics.Bitmap?
            ) {
                super.onPageStarted(view, url, favicon)
                if (url.isNullOrBlank()) return
                handleRedirect(Uri.parse(url))
            }

            override fun onPageFinished(view: WebView?, url: String?) {
                super.onPageFinished(view, url)
                if (url.isNullOrBlank()) {
                    return
                }
            }
        }
    }

    private fun handleRedirect(uri: Uri): Boolean {
        if (!uri.toString().startsWith(redirectUri, ignoreCase = true)) {
            return false
        }

        val code = uri.getQueryParameter("code")
        val state = uri.getQueryParameter("state")
        if (code.isNullOrBlank() || state.isNullOrBlank() || state != expectedState) {
            finishWithCancel()
            return true
        }

        setResult(
            RESULT_OK,
            Intent().apply {
                putExtra(EXTRA_PROVIDER_ID, providerId)
                putExtra(EXTRA_AUTH_CODE, code)
                putExtra(EXTRA_CODE_VERIFIER, codeVerifier)
                putExtra(EXTRA_REDIRECT_URI, redirectUri)
            }
        )
        finish()
        return true
    }

    private fun buildLoginBootstrapUrl(settings: DataSyncSettings): String {
        val providerId = settings.keycloakProviderId ?: "keycloak"
        val loginPath = settings.keycloakLoginPath ?: "api/auth/login/{provider_id}"
        val expandedPath = loginPath.replace(
            "{provider_id}",
            providerId,
            ignoreCase = true
        )
        return "${settings.geoNatureServerUrl.trimEnd('/')}/${expandedPath.trimStart('/')}"
    }

    private fun buildAuthorizeUrlFromBootstrap(bootstrapUrl: String): String? {
        return runCatching {
            val bootstrap = URL(bootstrapUrl)
            val connection = (bootstrap.openConnection() as HttpURLConnection).apply {
                requestMethod = "GET"
                instanceFollowRedirects = false
                connectTimeout = 10000
                readTimeout = 10000
            }
            connection.connect()
            val location = connection.getHeaderField("Location")
            connection.disconnect()
            location?.let { URL(bootstrap, it).toString() }
        }.mapCatching { location ->
            val parsed = Uri.parse(location ?: return@mapCatching null)
            val authorizeEndpoint = Uri.Builder()
                .scheme(parsed.scheme)
                .authority(parsed.authority)
                .path(parsed.path)
                .build()
                .toString()
            val clientId = parsed.getQueryParameter("client_id")
                ?: return@mapCatching null
            val scope = parsed.getQueryParameter("scope") ?: "openid profile email"
            val codeChallenge = codeChallenge(codeVerifier)

            Uri.parse(authorizeEndpoint).buildUpon()
                .appendQueryParameter("response_type", "code")
                .appendQueryParameter("client_id", clientId)
                .appendQueryParameter("redirect_uri", redirectUri)
                .appendQueryParameter("scope", scope)
                .appendQueryParameter("state", expectedState)
                .appendQueryParameter("code_challenge", codeChallenge)
                .appendQueryParameter("code_challenge_method", "S256")
                .build()
                .toString()
        }.onFailure {
            Logger.warn(it)
        }.getOrNull()
    }

    private fun finishWithCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }

    override fun onDestroy() {
        webView.stopLoading()
        webView.webChromeClient = null
        webView.webViewClient = WebViewClient()
        webView.destroy()
        super.onDestroy()
    }

    companion object {
        private const val EXTRA_SETTINGS = "extra_settings"
        const val EXTRA_PROVIDER_ID = "extra_provider_id"
        const val EXTRA_AUTH_CODE = "extra_auth_code"
        const val EXTRA_CODE_VERIFIER = "extra_code_verifier"
        const val EXTRA_REDIRECT_URI = "extra_redirect_uri"

        private fun randomUrlSafeToken(size: Int): String {
            val bytes = ByteArray(size)
            SecureRandom().nextBytes(bytes)
            return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
        }

        private fun generateCodeVerifier(): String {
            return randomUrlSafeToken(64)
        }

        private fun codeChallenge(codeVerifier: String): String {
            val digest = MessageDigest.getInstance("SHA-256")
                .digest(codeVerifier.toByteArray(Charsets.US_ASCII))
            return Base64.getUrlEncoder().withoutPadding().encodeToString(digest)
        }

        fun newIntent(
            context: Context,
            settings: DataSyncSettings
        ): Intent {
            return Intent(context, KeycloakLoginActivity::class.java).apply {
                putExtra(EXTRA_SETTINGS, settings)
            }
        }
    }
}
