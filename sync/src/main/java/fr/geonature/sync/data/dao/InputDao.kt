package fr.geonature.sync.data.dao

import android.content.ContentValues
import android.content.Context
import android.net.Uri
import android.util.Log
import fr.geonature.commons.data.helper.Provider.buildUri
import fr.geonature.commons.input.AbstractInput
import fr.geonature.commons.util.getInputsFolder
import fr.geonature.mountpoint.util.FileUtils
import org.json.JSONObject
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
     * @param values a set of column_name/value pairs as [AbstractInput] to save
     *
     * @return The URI to the `JSON` file
     */
    fun exportInput(values: ContentValues): Uri {
        val file = getExportedInput(
            values.getAsString("packageName"),
            values.getAsLong("id")
        )

        val asJson = kotlin
            .runCatching { JSONObject(values.getAsString("data")) }
            .getOrNull()
            ?: throw IllegalArgumentException("Invalid ContentValues $values")

        BufferedWriter(FileWriter(file)).run {
            write(asJson.toString())
            flush()
            close()
        }

        val exportedInputUri = buildUri(
            "inputs",
            values.getAsString("packageName"),
            values
                .getAsLong("id")
                .toString()
        )

        Log.i(
            TAG,
            "input '${values.getAsLong("id")}' exported (URI: $exportedInputUri)"
        )

        return exportedInputUri
    }

    fun getExportedInput(
        packageId: String,
        inputId: Long
    ): File {
        return File(
            FileUtils.getInputsFolder(context).also { it.mkdirs() },
            "input_${packageId.substringAfterLast(".")}_${inputId}.json"
        )
    }

    fun countInputsToSynchronize(packageId: String): Number {
        return FileUtils
            .getInputsFolder(context)
            .walkTopDown()
            .filter { it.isFile && it.extension == "json" }
            .filter { it.nameWithoutExtension.startsWith("input") }
            .filter { it.nameWithoutExtension.contains(packageId.substringAfterLast(".")) }
            .filter { it.canRead() }
            .count()
    }

    companion object {
        private val TAG = InputDao::class.java.name
    }
}