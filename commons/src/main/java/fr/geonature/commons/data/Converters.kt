package fr.geonature.commons.data

import androidx.room.TypeConverter
import java.util.Date

/**
 * Type converters.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object Converters {

    /**
     * Converts timestamp to Date.
     */
    @TypeConverter
    @JvmStatic
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    /**
     * Converts Date to timestamp.
     */
    @TypeConverter
    @JvmStatic
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }
}