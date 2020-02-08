package fr.geonature.mountpoint.util

import android.annotation.SuppressLint
import android.os.Build

/**
 * Class helper about devices and Android versions used.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object DeviceUtils {
    val isPostKitKat: Boolean
        @SuppressLint("ObsoleteSdkInt")
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT

    val isPostLollipop: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}
