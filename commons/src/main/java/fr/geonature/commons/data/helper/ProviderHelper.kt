package fr.geonature.commons.data.helper

import android.net.Uri
import android.net.Uri.withAppendedPath

/**
 * Helper about content provider.
 *
 * @author S. Grimault
 */
object ProviderHelper {

    /**
     * Build resource [Uri].
     */
    fun buildUri(
        authority: String,
        resource: String,
        vararg path: String
    ): Uri {

        val baseUri = Uri.parse("content://$authority/$resource")

        return if (path.isEmpty()) baseUri
        else withAppendedPath(baseUri,
            path
                .asSequence()
                .filter { it.isNotBlank() }
                .joinToString("/"))
    }
}
