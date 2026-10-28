package fr.geonature.auth.settings.domain

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

/**
 * Default settings for authentication module.
 *
 * @author S. Grimault
 */
@Parcelize
data class AuthSettings(

    /**
     * _GeoNature_ application ID in _UsersHub_.
     */
    val applicationId: Int = 0
) : Parcelable {

    class Builder {

        /**
         * GeoNature application ID in UsersHub.
         */
        private var applicationId: Int = 0

        /**
         * Makes a copy of given [AuthSettings].
         */
        fun from(authSettings: AuthSettings?) =
            apply {
                if (authSettings == null) return@apply

                applicationId = authSettings.applicationId
            }

        /**
         * Sets GeoNature application ID in UsersHub.
         */
        fun applicationId(applicationId: Int) =
            apply {
                this.applicationId = applicationId
            }

        /**
         * Builds a new instance of [AuthSettings].
         */
        fun build(): AuthSettings {
            return AuthSettings(applicationId)
        }
    }

}
