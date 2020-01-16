package fr.geonature.viewpager.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager.widget.ViewPager
import fr.geonature.viewpager.BuildConfig
import fr.geonature.viewpager.R
import fr.geonature.viewpager.pager.Pager
import fr.geonature.viewpager.pager.PagerManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.ArrayList

/**
 * Basic [ViewPager] implementation as [AppCompatActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractPagerFragmentActivity : AppCompatActivity(),
    View.OnClickListener,
    ViewPager.OnPageChangeListener {

    lateinit var pagerManager: PagerManager
    lateinit var adapter: SimpleFragmentPagerAdapter
    lateinit var viewPager: EnablePagingViewPager
    lateinit var previousButton: Button
    lateinit var nextButton: Button

    internal var pager: Pager? = null
    internal var restorePager = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_pager)

        pagerManager = PagerManager(application)

        adapter = SimpleFragmentPagerAdapter(
            this,
            supportFragmentManager
        )
        viewPager = findViewById(R.id.pager)
        viewPager.adapter = adapter

        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        GlobalScope.launch(Dispatchers.Main) {
            if (intent.hasExtra(EXTRA_PAGER_ID)) {
                pager = pagerManager.load(
                    intent.getLongExtra(
                        EXTRA_PAGER_ID,
                        0L
                    )
                )

                if (BuildConfig.DEBUG) {
                    Log.d(
                        TAG,
                        "onCreate, pager loaded: $pager"
                    )
                }
            }

            if (pager == null) {
                pager = if (savedInstanceState == null) Pager()
                else savedInstanceState.getParcelable(KEY_PAGER)
            }

            if (pager == null) {
                pager = Pager()
            }

            val pager = pager ?: return@launch

            val indicator = findViewById<UnderlinePageIndicator>(R.id.indicator)
            indicator.setViewPager(viewPager)
            viewPager.addOnPageChangeListener(this@AbstractPagerFragmentActivity)

            if (savedInstanceState == null && pager.size == 0) {
                pager.size = pagerFragments.size
                pager.position = viewPager.currentItem
            }

            for (i in 0 until pagerFragments.size) {
                var fragment = getPageFragment(i)

                if (fragment == null) {
                    // no fragment found through getSupportFragmentManager() so try to find it through getPagerFragments()
                    fragment = ArrayList(pagerFragments.values)[i]
                }

                if (fragment == null) {
                    Log.w(
                        TAG,
                        "onPostCreate: no fragment found at position $i"
                    )
                } else {
                    adapter.fragments[fragment.getResourceTitle()] = (fragment as Fragment?)!!
                }
            }

            adapter.notifyDataSetChanged()
            viewPager.post {
                restorePager = true
                viewPager.currentItem = pager.position

                if (pager.position == 0) {
                    onPageSelected(viewPager.currentItem)
                }
            }

            title = adapter.getPageTitle(viewPager.currentItem)

            previousButton.isEnabled = viewPager.currentItem > 0
            previousButton.visibility =
                if (viewPager.currentItem > 0) View.VISIBLE else View.INVISIBLE
            previousButton.setOnClickListener(this@AbstractPagerFragmentActivity)

            nextButton.isEnabled = false
            nextButton.setOnClickListener(this@AbstractPagerFragmentActivity)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelable(
            KEY_PAGER,
            pager
        )

        super.onSaveInstanceState(outState)
    }

    override fun onResume() {
        super.onResume()

        val fragment = getPageFragment(viewPager.currentItem)

        nextButton.isEnabled = fragment == null || fragment.validate()
        nextButton.setText(if (viewPager.currentItem < adapter.count - 1) R.string.button_pager_next else R.string.button_pager_finish)

        // refreshes the current view if needed
        if (fragment != null) {
            fragment.refreshView()

            // disable or enable paging control for the current instance of IValidateFragment
            viewPager.setPagingEnabled(fragment.pagingEnabled())
        }
    }

    override fun onPause() {
        super.onPause()

        val pager = pager ?: return

        if (pager.id != 0L) {
            GlobalScope.launch(Dispatchers.Main) {
                pagerManager.save(pager)
            }
        }
    }

    protected abstract val pagerFragments: Map<Int, IValidateFragment>
    protected abstract fun performFinishAction()

    override fun onClick(v: View) {
        when (v.id) {
            R.id.previousButton -> {
                if (viewPager.currentItem > 0) {
                    viewPager.setCurrentItem(
                        viewPager.currentItem - 1,
                        true
                    )
                }
            }
            R.id.nextButton -> {
                if (viewPager.currentItem < adapter.count - 1) {
                    viewPager.setCurrentItem(
                        viewPager.currentItem + 1,
                        true
                    )
                } else if (viewPager.currentItem == adapter.count - 1) {
                    // the last page
                    performFinishAction()
                }
            }
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        // nothing to do ...
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
        // nothing to do ...
    }

    override fun onPageSelected(position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "onPageSelected: $position"
            )
        }

        // sets default paging control
        viewPager.setPagingEnabled(true)

        // checks validation before switching to the next page
        val fragmentAtPreviousPosition = getPageFragment(position - 1)

        if (position > 0 && !(fragmentAtPreviousPosition == null || fragmentAtPreviousPosition.validate())) {
            viewPager.setCurrentItem(
                position - 1,
                true
            )
            return
        }

        // updates title
        title = adapter.getPageTitle(position)
        supportActionBar?.subtitle = adapter.getPageSubtitle(position)

        val fragmentAtPosition = getPageFragment(position)

        // refreshes the current view if needed
        if (fragmentAtPosition != null) {
            fragmentAtPosition.refreshView()

            // disable or enable paging control for the current instance of IValidateFragment
            viewPager.setPagingEnabled(fragmentAtPosition.pagingEnabled())
        }

        // updates navigation buttons statuses

        previousButton.isEnabled = position > 0
        previousButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE

        nextButton.setText(if (position < adapter.count - 1) R.string.button_pager_next else R.string.button_pager_finish)
        nextButton.isEnabled = fragmentAtPosition == null || fragmentAtPosition.validate()

        pager?.position = position
    }

    fun validateCurrentPage() {
        val currentItem = viewPager.currentItem

        if (currentItem < adapter.count) {
            val fragment = getPageFragment(currentItem)
            nextButton.isEnabled = fragment == null || fragment.validate()
        }
    }

    open fun goToPreviousPage() {
        val currentItem = viewPager.currentItem

        if (currentItem > 0) {
            viewPager.setCurrentItem(
                currentItem - 1,
                true
            )
        }
    }

    fun goToNextPage() {
        val currentItem = viewPager.currentItem

        if (currentItem < adapter.count - 1) {
            val fragment = getPageFragment(currentItem)

            if (fragment != null && fragment.validate()) {
                if (BuildConfig.DEBUG) {
                    Log.d(
                        TAG,
                        "goToNextPage: " + fragment.getResourceTitle()
                    )
                }

                viewPager.setCurrentItem(
                    currentItem + 1,
                    true
                )
            }
        }
    }

    fun goToPage(position: Int) {
        viewPager.setCurrentItem(
            position,
            true
        )
    }

    fun goToPageByKey(key: Int) {
        val fragment = adapter.fragments[key]

        if (fragment is IValidateFragment) {
            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "goToPageByKey: key '$key'"
                )
            }

            viewPager.setCurrentItem(
                ArrayList(adapter.fragments.values).lastIndexOf(fragment),
                true
            )
        } else {
            Log.w(
                TAG,
                "goToPageByKey: key '$key' undefined"
            )
        }
    }

    fun goToFirstPage() {
        viewPager.setCurrentItem(
            0,
            true
        )
    }

    fun goToLastPage() {
        viewPager.setCurrentItem(
            adapter.count - 1,
            true
        )
    }

    /**
     * Gets the current [IValidateFragment] instance at the current position of this pager.
     *
     * @return [IValidateFragment] instance
     */
    fun getCurrentPageFragment(): IValidateFragment? {
        val currentItem = viewPager.currentItem
        var pageFragment = getPageFragment(currentItem)

        if (pageFragment == null) {
            // no fragment found through getSupportFragmentManager() so try to find it through getPagerFragments()
            pageFragment = ArrayList(pagerFragments.values)[currentItem]
        }

        if (pageFragment == null) {
            Log.w(
                TAG,
                "getCurrentPageFragment: no fragment found at position $currentItem"
            )
        }

        return pageFragment
    }

    /**
     * Gets the current [IValidateFragment] instance at the current position of this pager.
     *
     * @param position the position of [IValidateFragment] to retrieve
     * @return [IValidateFragment] instance
     */
    internal fun getPageFragment(position: Int?): IValidateFragment? {
        val currentItem = viewPager.currentItem

        val fragment =
            supportFragmentManager.findFragmentByTag(
                "android:switcher:" + R.id.pager + ":" + (position
                    ?: currentItem)
            )

        return if (fragment != null && fragment is IValidateFragment) {
            fragment
        } else {
            Log.w(
                TAG,
                "getPageFragment: no fragment found through getSupportFragmentManager() at position " + (position
                    ?: currentItem)
            )

            null
        }
    }

    /**
     * Gets the current [IValidateFragment] instance for a given key of this pager.
     *
     * @param key the key of [IValidateFragment] to retrieve
     * @return [IValidateFragment] instance
     * @see AbstractPagerFragmentActivity.getPageFragment
     */
    internal fun getPageFragmentByKey(key: Int?): IValidateFragment? {
        return getPageFragment(ArrayList(adapter.fragments.keys).indexOf(key))
    }

    companion object {

        private val TAG = AbstractPagerFragmentActivity::class.java.name

        const val EXTRA_PAGER_ID = "extra_pager_id"
        protected const val KEY_PAGER = "key_pager"
    }
}
