package fr.geonature.commons.data.helper

import android.net.Uri
import android.net.Uri.withAppendedPath

/**
 * Base content provider.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object Provider {

    /**
     * The default authority used by content provider.
     */
    const val AUTHORITY = "fr.geonature.sync.provider"

    /**
     * Build resource [Uri].
     */
    fun buildUri(
        resource: String,
        vararg path: String
    ): Uri {

        val baseUri = Uri.parse("content://$AUTHORITY/$resource")

        return if (path.isEmpty()) baseUri
        else withAppendedPath(baseUri,
            path
                .asSequence()
                .filter { it.isNotBlank() }
                .joinToString("/"))
    }
}
