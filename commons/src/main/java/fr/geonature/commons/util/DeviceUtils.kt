package fr.geonature.commons.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Class helper about devices and Android versions used.
 *
 * @author S. Grimault
 */
object DeviceUtils {
    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.N)
    val isPostNougat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.N

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.O)
    val isPostOreo = Build.VERSION.SDK_INT >= Build.VERSION_CODES.O

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.P)
    val isPostPie = Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
}
