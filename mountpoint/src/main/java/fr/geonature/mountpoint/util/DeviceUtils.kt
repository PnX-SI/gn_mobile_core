package fr.geonature.mountpoint.util

import android.os.Build
import androidx.annotation.ChecksSdkIntAtLeast

/**
 * Class helper about devices and Android versions used.
 *
 * @author S. Grimault
 */

object DeviceUtils {

    @ChecksSdkIntAtLeast(api = Build.VERSION_CODES.LOLLIPOP)
    val isPostLollipop = Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP
}
