package fr.geonature.viewpager.pager

import android.app.Application
import android.text.TextUtils
import android.util.Log
import androidx.preference.PreferenceManager
import fr.geonature.viewpager.pager.io.PagerJsonReader
import fr.geonature.viewpager.pager.io.PagerJsonWriter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Manage [Pager] information.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PagerManager(application: Application) {

    private val preferenceManager = PreferenceManager.getDefaultSharedPreferences(application)
    private val pagerJsonReader: PagerJsonReader = PagerJsonReader()
    private val pagerJsonWriter: PagerJsonWriter = PagerJsonWriter()

    /**
     * Reads [Pager] from given ID or create a new one if not found.
     *
     * @param pagerId The [Pager] ID to read. If omitted, create a new one.
     *
     * @return [Pager]
     */
    suspend fun load(pagerId: Long? = null): Pager = withContext(Dispatchers.IO) {
        // read input as JSON from shared preferences
        val json = if (pagerId == null) null
        else preferenceManager.getString(buildPagerPreferenceKey(pagerId),
                                         null)

        if (TextUtils.isEmpty(json)) {
            Pager()
        }
        else pagerJsonReader.read(json) ?: Pager()
    }

    /**
     * Saves the given [Pager].
     *
     * @return `true` if the given [Pager] has been successfully saved, `false` otherwise
     */
    suspend fun save(pager: Pager): Boolean = withContext(Dispatchers.IO) {
        val pagerAsJson = pagerJsonWriter.write(pager)

        if (TextUtils.isEmpty(pagerAsJson)) {
            Log.w(TAG,
                  "failed to save pager metadata $pager")
        }

        preferenceManager.edit()
            .putString(buildPagerPreferenceKey(pager.id),
                       pagerAsJson)
            .apply()

        preferenceManager.contains(buildPagerPreferenceKey(pager.id))
    }

    /**
     * Deletes [Pager] from given ID.
     *
     * @param pagerId the [Pager] ID to delete
     *
     * @return `true` if the given [Pager] has been successfully deleted, `false` otherwise
     */
    suspend fun delete(pagerId: Long): Boolean = withContext(Dispatchers.IO) {
        preferenceManager.edit()
            .remove(buildPagerPreferenceKey(pagerId))
            .apply()

        !preferenceManager.contains(buildPagerPreferenceKey(pagerId))
    }

    private fun buildPagerPreferenceKey(pagerId: Long): String? {
        return "KEY_PAGER_$pagerId"
    }

    companion object {

        private val TAG = PagerManager::class.java.name
    }
}