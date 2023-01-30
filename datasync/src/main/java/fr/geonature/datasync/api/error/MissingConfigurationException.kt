package fr.geonature.datasync.api.error

/**
 * Base exception about invalid or missing configuration about _GeoNature_ services.
 *
 * @author S. Grimault
 */
sealed class MissingConfigurationException(
    message: String? = null,
    cause: Throwable? = null
) : IllegalStateException(
    message,
    cause
) {
    /**
     * Handles missing _GeoNature_ base URL.
     */
    object MissingGeoNatureBaseURLException :
        MissingConfigurationException("missing GeoNature base URL")

    /**
     * Handles missing _TaxHub_ base URL.
     */
    object MissingTaxHubBaseURLException : MissingConfigurationException("missing TaxHub base URL")
}

