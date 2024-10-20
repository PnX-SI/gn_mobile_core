package fr.geonature.datasync.api.error

import java.io.IOException

/**
 * Network error exception.
 *
 * @author S. Grimault
 */
data class NetworkException(override val message: String? = null) : IOException(message)
