package fr.geonature.datasync.api

import com.google.gson.GsonBuilder
import fr.geonature.datasync.api.error.BaseApiException
import fr.geonature.datasync.api.error.NetworkException
import fr.geonature.datasync.auth.ICookieManager
import okhttp3.Cookie
import okhttp3.CookieJar
import okhttp3.HttpUrl
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.internal.http2.ConnectionShutdownException
import okhttp3.logging.HttpLoggingInterceptor
import org.tinylog.Logger
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.SocketException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.util.concurrent.TimeUnit

/**
 * Helper function to create a compliant HTTP client with GeoNature APIs.
 *
 * @author S. Grimault
 */
fun <T> createServiceClient(
    baseUrl: String,
    cookieManager: ICookieManager,
    service: Class<T>,
    vararg interceptor: Interceptor,
): T {
    val logger = Logger.tag(service.name)

    return Retrofit
        .Builder()
        .baseUrl("${baseUrl.let { if (it.endsWith('/')) it.dropLast(1) else it }}/")
        .client(OkHttpClient
            .Builder()
            .cookieJar(object : CookieJar {
                override fun saveFromResponse(
                    url: HttpUrl,
                    cookies: List<Cookie>
                ) {
                    cookies
                        .firstOrNull()
                        ?.also {
                            cookieManager.cookie = it
                        }
                }

                override fun loadForRequest(url: HttpUrl): MutableList<Cookie> {
                    return cookieManager.cookie?.let {
                        mutableListOf(it)
                    }
                        ?: mutableListOf()
                }
            })
            .connectTimeout(
                120,
                TimeUnit.SECONDS
            )
            .readTimeout(
                120,
                TimeUnit.SECONDS
            )
            .writeTimeout(
                120,
                TimeUnit.SECONDS
            )
            .cache(null)
            .addInterceptor(HttpLoggingInterceptor { logger.info { it } }.apply {
                level = HttpLoggingInterceptor.Level.BASIC
                redactHeader("Authorization")
                redactHeader("Cookie")
            })
            // handle network/api errors globally through dedicated interceptor
            .addInterceptor {
                val request = it.request()

                runCatching { it.proceed(request) }.onSuccess { response ->
                    if (!response.isSuccessful) {
                        throw when (response.code) {
                            400 -> BaseApiException.BadRequestException(
                                response.message,
                                response,
                            )
                            401 -> BaseApiException.UnauthorizedException(
                                response.message,
                                response,
                            )
                            404 -> BaseApiException.NotFoundException(
                                response.message,
                                response,
                            )
                            500 -> BaseApiException.InternalServerException(
                                response.message,
                                response,
                            )
                            else -> BaseApiException.ApiException(
                                response.code,
                                response.message,
                                response,
                            )
                        }
                    }
                }
                    .getOrElse { throwable ->
                        throw when (throwable) {
                            is SocketException,
                            is SocketTimeoutException,
                            is UnknownHostException,
                            is ConnectionShutdownException,
                            -> NetworkException(throwable.message)
                            else -> throwable
                        }
                    }
            }
            .apply {
                interceptor.forEach {
                    addInterceptor(it)
                }
            }
            .build())
        .addConverterFactory(
            GsonConverterFactory.create(
                GsonBuilder()
                    .setDateFormat("yyyy-MM-dd HH:mm:ss")
                    .create()
            )
        )
        .build()
        .create(service)
}