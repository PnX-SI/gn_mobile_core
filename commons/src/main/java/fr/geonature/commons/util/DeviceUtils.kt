package fr.geonature.commons.util

import android.annotation.SuppressLint
import android.os.Build

/**
 * Class helper about devices and Android versions used.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object DeviceUtils {
    @SuppressLint("ObsoleteSdkInt")
    val isPostKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT
    val isPostLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
    val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N
    val isPostOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O
    val isPostPie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}
