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
    private val sdfDateTime = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
    @SuppressLint("SimpleDateFormat")
    private val sdfDate = SimpleDateFormat("yyyy-MM-dd")

    init {
        sdfDateTime.timeZone = TimeZone.getTimeZone("UTC")
        sdfDate.timeZone = TimeZone.getTimeZone("UTC")
    }

    @SuppressLint("SimpleDateFormat")
    fun toDate(str: String?): Date? {
        if (isEmpty(str)) return null

        return try {
            sdfDateTime.parse(str)
        }
        catch (pe: ParseException) {
            try {
                sdfDate.parse(str)
            }
            catch (pe: ParseException) {
                return null
            }
        }
    }

    @SuppressLint("SimpleDateFormat")
    fun toIsoDateString(date: Date?): String? {
        if (date == null) return null

        return sdfDateTime.format(date)
    }
}