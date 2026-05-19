package fr.geonature.datasync.api.model

import com.google.gson.annotations.SerializedName

data class AuthMobileKeycloakRequest(
    @SerializedName("provider_id")
    val providerId: String,
    @SerializedName("code")
    val code: String,
    @SerializedName("code_verifier")
    val codeVerifier: String,
    @SerializedName("redirect_uri")
    val redirectUri: String,
    @SerializedName("id_application")
    val applicationId: Int
)
