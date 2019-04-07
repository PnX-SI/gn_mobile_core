package fr.geonature.commons.util

import android.annotation.SuppressLint
import fr.geonature.commons.util.StringUtils.isEmpty
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

/**
 * ISO date helper.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object IsoDateUtils {
    @SuppressLint("SimpleDateFormat")
    fun toDate(str: String?): Date? {
        if (isEmpty(str)) {
            return null
        }

        val df = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'")
        df.timeZone = TimeZone.getTimeZone("UTC")

        return try {
            df.parse(str)
        }
        catch (pe: ParseException) {
            null
        }
    }
}