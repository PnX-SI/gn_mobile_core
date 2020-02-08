package fr.geonature.sync

import android.app.Application
import android.util.Log
import fr.geonature.mountpoint.util.MountPointUtils.getExternalStorage
import fr.geonature.mountpoint.util.MountPointUtils.getInternalStorage

/**
 * Base class to maintain global application state.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(
            TAG,
            "internal storage: " + getInternalStorage()
        )
        Log.i(
            TAG,
            "external storage: " + getExternalStorage(this)
        )
    }

    companion object {
        private val TAG = MainApplication::class.java.name
    }
}
