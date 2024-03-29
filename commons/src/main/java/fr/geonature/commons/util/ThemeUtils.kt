package fr.geonature.commons.util

import android.content.Context
import androidx.annotation.AttrRes
import androidx.annotation.ColorInt
import fr.geonature.commons.R

/**
 * Helper class about application theme.
 *
 * @author S. Grimault
 */
object ThemeUtils {

    @ColorInt
    fun getPrimaryColor(context: Context): Int {
        return getColor(
            context,
            R.attr.colorPrimary
        )
    }

    @ColorInt
    fun getPrimaryDarkColor(context: Context): Int {
        return getColor(
            context,
            R.attr.colorPrimaryDark
        )
    }

    @ColorInt
    fun getAccentColor(context: Context): Int {
        return getColor(
            context,
            R.attr.colorAccent
        )
    }

    @ColorInt
    fun getErrorColor(context: Context): Int {
        return getColor(
            context,
            R.attr.colorError
        )
    }

    @ColorInt
    fun getColor(
        context: Context,
        @AttrRes colorAttribute: Int
    ): Int {
        val typedArray = context.theme.obtainStyledAttributes(intArrayOf(colorAttribute))
        val color = typedArray.getColor(
            0,
            0
        )

        typedArray.recycle()

        return color
    }
}
