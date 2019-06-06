package fr.geonature.viewpager.ui

import android.content.Context
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import java.util.ArrayList

/**
 * Basic implementation of [FragmentPagerAdapter] used by [AbstractPagerFragmentActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class SimpleFragmentPagerAdapter internal constructor(private val context: Context,
                                                      fm: FragmentManager) : FragmentPagerAdapter(fm) {

    val fragments: MutableMap<Int, Fragment> = LinkedHashMap()

    override fun getItem(position: Int): Fragment {
        return ArrayList(fragments.values)[position]
    }

    override fun getCount(): Int {
        return fragments.size
    }

    override fun getPageTitle(position: Int): CharSequence? {
        val fragment = getItem(position)

        return if (fragment is IValidateFragment) {
            context.getText(fragment.getResourceTitle())
        }
        else {
            null
        }
    }
}
