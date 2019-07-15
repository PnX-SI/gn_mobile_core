package fr.geonature.sync

import android.app.Application
import android.util.Log

import fr.geonature.commons.util.MountPointUtils

/**
 * Base class to maintain global application state.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        Log.i(TAG,
              "internal storage: " + MountPointUtils.getInternalStorage())
        Log.i(TAG,
              "external storage: " + MountPointUtils.getExternalStorage(this))
    }

    companion object {
        private val TAG = MainApplication::class.java.name
    }
}
