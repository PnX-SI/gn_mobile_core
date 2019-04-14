package fr.geonature.viewpager.helper

import android.content.Context
import android.text.TextUtils
import android.util.Log
import androidx.preference.PreferenceManager
import fr.geonature.viewpager.model.Pager

/**
 * Manage [Pager] information.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class PagerHelper(private val context: Context) {

    private val pagerJsonReader: PagerJsonReader = PagerJsonReader()
    private val pagerJsonWriter: PagerJsonWriter = PagerJsonWriter()

    fun load(pagerId: Long): Pager {
        // read input as JSON from shared preferences
        val json = PreferenceManager.getDefaultSharedPreferences(context)
            .getString(getPagerPreferenceKey(pagerId),
                       null)

        return if (TextUtils.isEmpty(json)) {
            Pager(pagerId)
        }
        else pagerJsonReader.read(json) ?: return Pager(pagerId)
    }

    fun save(pager: Pager) {
        val pagerAsJson = pagerJsonWriter.write(pager)

        if (TextUtils.isEmpty(pagerAsJson)) {
            Log.w(TAG,
                  "failed to save pager metadata $pager")
        }

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(getPagerPreferenceKey(pager.id),
                       pagerAsJson)
            .apply()
    }

    fun delete(pagerId: Long) {
        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .remove(getPagerPreferenceKey(pagerId))
            .apply()
    }

    private fun getPagerPreferenceKey(pagerId: Long): String? {
        return "KEY_PAGER_$pagerId"
    }

    companion object {

        private val TAG = PagerHelper::class.java.name
    }
}
