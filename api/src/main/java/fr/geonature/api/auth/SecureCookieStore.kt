package fr.geonature.api.auth

import android.content.Context
import android.util.Base64
import androidx.core.content.edit
import com.google.crypto.tink.Aead
import com.google.crypto.tink.KeyTemplates
import com.google.crypto.tink.RegistryConfiguration
import com.google.crypto.tink.aead.AeadConfig
import com.google.crypto.tink.integration.android.AndroidKeysetManager
import java.security.GeneralSecurityException

/**
 * Stores a secret string encrypted with AES-256-GCM (Tink AEAD).
 *
 * The Tink keyset is wrapped by a non-exportable Android Keystore master key.
 * Both [PREFERENCE_FILE_NAME] and [PREFERENCE_FILE_NAME_KEYSET] must be excluded from backups.
 * These names are duplicated as literals in `res/xml/gn_api_*.xml` (backup rules cannot use
 * resources): `BackupRulesTest` checks they stay in sync.
 *
 * @author S. Grimault
 */
class SecureCookieStore(
    context: Context,
    aeadProvider: () -> Aead = { createAead(context.applicationContext) }
) {
    private val preferences =
        context.applicationContext.getSharedPreferences(
            PREFERENCE_FILE_NAME,
            Context.MODE_PRIVATE
        )
    private val aead: Aead by lazy(aeadProvider)

    /**
     * Reads and decrypts the stored value.
     *
     * @return the decrypted value or `null` if nothing was stored or if it cannot be decrypted
     * (in this case, the stored value is discarded)
     */
    fun read(): String? {
        val encrypted = preferences.getString(
            KEY_VALUE,
            null
        ) ?: return null

        return try {
            String(
                aead.decrypt(
                    Base64.decode(
                        encrypted,
                        Base64.NO_WRAP
                    ),
                    ASSOCIATED_DATA
                ),
                Charsets.UTF_8
            )
        } catch (_: GeneralSecurityException) {
            clear()
            null
        } catch (_: IllegalArgumentException) {
            clear()
            null
        }
    }

    /**
     * Encrypts and stores the given value.
     */
    fun write(value: String) {
        val encrypted = Base64.encodeToString(
            aead.encrypt(
                value.toByteArray(Charsets.UTF_8),
                ASSOCIATED_DATA
            ),
            Base64.NO_WRAP
        )

        preferences.edit {
            putString(
                KEY_VALUE,
                encrypted
            )
        }
    }

    /**
     * Removes the stored value.
     */
    fun clear() {
        preferences.edit { remove(KEY_VALUE) }
    }

    companion object {
        const val PREFERENCE_FILE_NAME = "gn_secure_auth"
        const val PREFERENCE_FILE_NAME_KEYSET = "gn_secure_auth_keyset"

        private const val KEYSET_NAME = "gn_secure_auth_keyset_name"
        private const val MASTER_KEY_URI = "android-keystore://gn_secure_auth_master_key"
        private const val KEY_VALUE = "key_secure_cookie"
        private val ASSOCIATED_DATA = KEY_VALUE.toByteArray(Charsets.UTF_8)

        private fun createAead(context: Context): Aead {
            AeadConfig.register()

            return AndroidKeysetManager
                .Builder()
                .withSharedPref(
                    context,
                    KEYSET_NAME,
                    PREFERENCE_FILE_NAME_KEYSET
                )
                .withKeyTemplate(KeyTemplates.get("AES256_GCM"))
                .withMasterKeyUri(MASTER_KEY_URI)
                .build()
                .keysetHandle
                .getPrimitive(
                    RegistryConfiguration.get(),
                    Aead::class.java
                )
        }
    }
}

