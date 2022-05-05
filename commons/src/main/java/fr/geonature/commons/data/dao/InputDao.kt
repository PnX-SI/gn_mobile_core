package fr.geonature.commons.data.dao

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import fr.geonature.commons.data.helper.ProviderHelper.buildUri
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import org.json.JSONObject
import org.tinylog.Logger
import java.io.BufferedWriter
import java.io.File
import java.io.FileWriter

/**
 * Data access object for [AbstractInput].
 *
 * @author S. Grimault
 */
class InputDao(private val context: Context) {

    /**
     * Exports `ContentValues` as [AbstractInput] to `JSON` file.
     *
     * @param authority content provider authority on which to export input
     * @param values a set of column_name/value pairs as [AbstractInput] to save
     *
     * @return The URI to the `JSON` file
     */
    fun exportInput(
        authority: String,
        values: ContentValues
    ): Uri {
        val file = getExportedInput(
            values.getAsString("packageName"),
            values.getAsLong("id")
        )

        val asJson = runCatching { JSONObject(values.getAsString("data")) }.getOrNull()
            ?: throw IllegalArgumentException("Invalid ContentValues $values")

        BufferedWriter(FileWriter(file)).run {
            write(asJson.toString())
            flush()
            close()
        }

        val exportedInputUri = buildUri(
            authority,
            "inputs",
            values.getAsString("packageName"),
            values
                .getAsLong("id")
                .toString()
        )

        Logger.info { "input '${values.getAsLong("id")}' exported (URI: $exportedInputUri)" }

        return exportedInputUri
    }

    fun getExportedInput(
        packageId: String,
        inputId: Long
    ): File {
        return File(
            FileUtils
                .getInputsFolder(
                    context,
                    packageId
                )
                .also { it.mkdirs() },
            "input_${inputId}.json"
        )
    }

    fun countInputsToSynchronize(packageId: String): Number {
        return FileUtils
            .getInputsFolder(
                context,
                packageId
            )
            .walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.nameWithoutExtension.startsWith("input") }
            .filter { it.canRead() }
            .count()
    }
}