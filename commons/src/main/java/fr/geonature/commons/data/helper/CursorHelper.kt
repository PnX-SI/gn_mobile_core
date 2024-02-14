package fr.geonature.commons.data.helper

import android.database.Cursor
import androidx.core.database.getDoubleOrNull
import androidx.core.database.getFloatOrNull
import androidx.core.database.getIntOrNull
import androidx.core.database.getLongOrNull
import androidx.core.database.getShortOrNull
import fr.geonature.commons.data.helper.Converters.fromTimestamp
import java.util.Date

/**
 * Utilities function about Cursor.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */

/**
 * Returns the value of the requested column name.
 *
 * @param columnName the name of the target column.
 *
 * @return the value of that column or the default value as fallback if given
 */
inline fun <reified T> Cursor.get(
    columnName: String,
    defaultValue: T? = null
): T? {
    val columnIndex =
        if (defaultValue == null) getColumnIndexOrThrow(columnName) else getColumnIndex(columnName)

    return when (T::class) {
        ByteArray::class -> if (columnIndex > -1) getBlob(columnIndex)?.let { byteArrayOf(*it) } as T? else defaultValue
        String::class -> if (columnIndex > -1) getString(columnIndex) as T? else defaultValue
        Short::class -> if (columnIndex > -1) getShortOrNull(columnIndex) as T? else defaultValue
        Int::class -> if (columnIndex > -1) getIntOrNull(columnIndex) as T? else defaultValue
        Long::class -> if (columnIndex > -1) getLongOrNull(columnIndex) as T? else defaultValue
        Float::class -> if (columnIndex > -1) getFloatOrNull(columnIndex) as T? else defaultValue
        Double::class -> if (columnIndex > -1) getDoubleOrNull(columnIndex) as T? else defaultValue
        Boolean::class -> if (columnIndex > -1) getIntOrNull(columnIndex)?.let { it != 0 } as T? else defaultValue
        Date::class -> if (columnIndex > -1) (getLongOrNull(columnIndex)?.let { if (it == 0L) null else fromTimestamp(it) }
            ?: defaultValue) as T? else defaultValue

        else -> throw IllegalArgumentException("Unsupported type ${T::class.java}")
    }.run {
        this
            ?: defaultValue
    }
}
