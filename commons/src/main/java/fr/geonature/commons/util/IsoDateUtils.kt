package fr.geonature.commons.util

import android.annotation.SuppressLint
import fr.geonature.commons.util.StringUtils.isEmpty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.TimeZone

/**
 * ISO date helper.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object IsoDateUtils {

    @SuppressLint("SimpleDateFormat")
    private val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")

    init {
        sdf.timeZone = TimeZone.getTimeZone("UTC")
    }

    @SuppressLint("SimpleDateFormat")
    fun toDate(str: String?): Date? {
        if (isEmpty(str)) return null

        return try {
            sdf.parse(str)
        }
        catch (pe: ParseException) {
            null
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun toIsoDateString(date: Date?): String? {
        if (date == null) return null

        return sdf.format(date)
    }
}