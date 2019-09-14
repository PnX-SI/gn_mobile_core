package fr.geonature.commons.data

import android.content.Context
import android.net.Uri
import android.net.Uri.withAppendedPath
import fr.geonature.commons.R

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
     * Check if 'READ' permission is granted for content provider.
     */
    val checkReadPermission: (Context, String?) -> Boolean = { context, permission -> context.getString(R.string.permission_read) == permission }

    /**
     * Build resource [Uri].
     */
    fun buildUri(resource: String,
                 vararg path: String): Uri {

        val baseUri = Uri.parse("content://$AUTHORITY/$resource")

        return if (path.isEmpty()) baseUri
        else withAppendedPath(baseUri,
                              path.asSequence().filter { it.isNotBlank() }.joinToString("/"))
    }
}