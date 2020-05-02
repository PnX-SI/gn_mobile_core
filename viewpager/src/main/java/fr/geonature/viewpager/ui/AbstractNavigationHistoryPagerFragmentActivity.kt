package fr.geonature.viewpager.ui

import android.os.Bundle
import android.os.PersistableBundle
import android.util.Log
import android.view.View
import androidx.viewpager.widget.ViewPager
import fr.geonature.viewpager.BuildConfig
import fr.geonature.viewpager.R

/**
 * [ViewPager] implementation as [AbstractPagerFragmentActivity] with navigation history support.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractNavigationHistoryPagerFragmentActivity : AbstractPagerFragmentActivity() {

    private var scrollState = 0
    private var positionOffset = 0f
    private var historyPrevious = false

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)

        historyPrevious =
            savedInstanceState != null && savedInstanceState.getBoolean(
                KEY_HISTORY_PREVIOUS,
                false
            )

        val pager = pager ?: return

        previousButton.isEnabled = !pager.history.isEmpty()
        previousButton.visibility = if (!pager.history.isEmpty()) View.VISIBLE
        else View.INVISIBLE
    }

    override fun onSaveInstanceState(
        outState: Bundle,
        outPersistentState: PersistableBundle
    ) {
        outState.putBoolean(
            KEY_HISTORY_PREVIOUS,
            historyPrevious
        )

        super.onSaveInstanceState(
            outState,
            outPersistentState
        )
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.previousButton -> goToPreviousPage()
            R.id.nextButton -> {
                if (viewPager.currentItem < (adapter.count - 1)) {
                    historyPrevious = false
                    viewPager.setCurrentItem(
                        viewPager.currentItem + 1,
                        true
                    )
                } else if (viewPager.currentItem == (adapter.count - 1)) {
                    // the last page
                    performFinishAction()
                }
            }
        }
    }

    override fun onPageScrolled(
        position: Int,
        positionOffset: Float,
        positionOffsetPixels: Int
    ) {
        if ((positionOffset > 0.0f) && (scrollState == ViewPager.SCROLL_STATE_DRAGGING)) {
            historyPrevious = this.positionOffset > positionOffset
            this.positionOffset = positionOffset
        }
    }

    override fun onPageScrollStateChanged(state: Int) {
        super.onPageScrollStateChanged(state)

        // only if the pager is currently being dragged by the user
        if (state != ViewPager.SCROLL_STATE_SETTLING) {
            scrollState = state
        }
    }

    override fun onPageSelected(position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "onPageSelected, position: $position, previous: $historyPrevious"
            )
        }

        val pager = pager ?: return

        // sets default paging control
        viewPager.setPagingEnabled(true)

        if (historyPrevious) {
            if (pager.history.isEmpty()) {
                historyPrevious = false
            } else {
                // go back in the navigation history
                val fragment = getPageFragment(position)

                if (fragment == null) {
                    goToPreviousPage()

                    return
                }

                if (fragment.getResourceTitle() == pager.history.last) {
                    pager.history.pollLast()

                    if (!fragment.validate()) {
                        goToPreviousPage()

                        return
                    }
                } else {
                    goToPreviousPage()

                    return
                }
            }
        } else {
            val fragment = getPageFragment(pager.position)

            if (fragment != null && (pager.history.isEmpty() || pager.history.last != fragment.getResourceTitle()) && !restorePager) {
                pager.history.addLast(fragment.getResourceTitle())
            }

            // checks validation before switching to the next page
            if (!pager.history.isEmpty() && !restorePager) {
                val getLastFragmentInHistory = getPageFragmentByKey(pager.history.last)

                if (position > 0 && !(getLastFragmentInHistory == null || getLastFragmentInHistory.validate())) {
                    if (BuildConfig.DEBUG) {
                        Log.d(
                            TAG,
                            "onPageSelected: previous fragment " + getLastFragmentInHistory.javaClass.name + " is not valid"
                        )
                    }

                    goToPreviousPage()

                    return
                }
            }
        }

        historyPrevious = false
        restorePager = false

        val fragment = getPageFragment(position)

        // updates title
        title = adapter.getPageTitle(position)
        supportActionBar?.subtitle = adapter.getPageSubtitle(position)

        // refreshes the current view if needed
        if (fragment != null) {
            fragment.refreshView()

            // disable or enable paging control for the current instance of IValidateFragment
            viewPager.setPagingEnabled(fragment.pagingEnabled())
        }

        // updates navigation buttons statuses
        if (fragment != null && fragment is IValidateWithNavigationControlFragment) {
            // disable or enable paging control for the current instance of IValidateWithNavigationControlFragment
            viewPager.setPagingPreviousEnabled(fragment.getPagingToPreviousEnabled())
            viewPager.setPagingNextEnabled(fragment.getPagingToForwardEnabled())

            if (fragment.getPagingToPreviousEnabled()) {
                previousButton.isEnabled = position > 0
                previousButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE
            } else {
                previousButton.visibility = View.INVISIBLE
            }

            if (fragment.getPagingToForwardEnabled()) {
                nextButton.isEnabled = fragment.validate()
                nextButton.visibility = View.VISIBLE
            } else {
                nextButton.visibility = View.INVISIBLE
            }
        } else {
            previousButton.isEnabled = position > 0
            previousButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE

            nextButton.setText(if (position < adapter.count - 1) R.string.button_pager_next else R.string.button_pager_finish)
            nextButton.isEnabled = fragment == null || fragment.validate()
            nextButton.visibility = View.VISIBLE
        }

        pager.position = position

        if (BuildConfig.DEBUG) {
            Log.d(
                TAG,
                "onPageSelected: $pager"
            )
        }
    }

    override fun goToPreviousPage() {
        val pager = pager ?: return

        if ((viewPager.currentItem > 0) && (!pager.history.isEmpty())) {
            historyPrevious = true
            goToPageByKey(pager.history.last)
        }
    }

    /**
     * Go to the last given page in history.
     *
     * @param key the page key
     */
    fun goBackInHistory(key: Int) {
        val pager = pager ?: return

        while (!pager.history.isEmpty() && key != pager.history.last) {
            pager.history.pollLast()
        }

        goToPreviousPage()
    }

    /**
     * Gets the number of pages for a given key saved in history.
     *
     * @param key the page key
     *
     * @return the number of pages in history for a given page key
     *
     * @see IValidateFragment.resourceTitle
     */
    fun countPagesInHistory(key: Int): Int {
        var count = 0
        val pager = pager ?: return count

        for (pageKey in pager.history) {
            if (pageKey == key) {
                count++
            }
        }

        return count
    }

    companion object {

        private val TAG = AbstractNavigationHistoryPagerFragmentActivity::class.java.name

        private const val KEY_HISTORY_PREVIOUS = "key_history_previous"
    }
}
