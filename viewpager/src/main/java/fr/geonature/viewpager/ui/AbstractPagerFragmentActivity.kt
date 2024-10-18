package fr.geonature.viewpager.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import fr.geonature.viewpager.BuildConfig
import fr.geonature.viewpager.R
import fr.geonature.viewpager.model.IPageFragment
import fr.geonature.viewpager.model.IPageWithValidationFragment
import fr.geonature.viewpager.model.PageFragmentViewModel

/**
 * Basic [ViewPager2] implementation as [AppCompatActivity].
 *
 * @author S. Grimault
 */
abstract class AbstractPagerFragmentActivity : AppCompatActivity(), OnPageFragmentListener {

    val pageFragmentViewModel: PageFragmentViewModel by viewModels()

    lateinit var viewPager: ViewPager2
    private lateinit var pagerIndicator: IPagerIndicator
    private lateinit var adapter: FragmentStateAdapter
    private lateinit var previousButton: Button
    lateinit var nextButton: Button

    private val navigationButtonListener = View.OnClickListener {
        when (it.id) {
            R.id.previousButton -> {
                goToPreviousPage()
            }
            R.id.nextButton -> {
                if (!onNextAction()) {
                    if (viewPager.currentItem < adapter.itemCount - 1) {
                        goToNextPage()
                    } else if (viewPager.currentItem == adapter.itemCount - 1) {
                        // the last page
                        performFinishAction()
                    }
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_pager)

        adapter = object : FragmentStateAdapter(this) {
            override fun getItemCount(): Int {
                return pageFragmentViewModel.size
            }

            override fun createFragment(position: Int): Fragment {
                return pageFragmentViewModel.getPageAtPosition(position) as Fragment
            }
        }

        viewPager = findViewById(R.id.pager)
        viewPager.adapter = adapter
        viewPager.offscreenPageLimit = 1
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                this@AbstractPagerFragmentActivity.onPageSelected(position)
            }
        })

        pagerIndicator = findViewById<UnderlinePagerIndicator>(R.id.indicator)
        pagerIndicator.setViewPager(viewPager)

        previousButton = findViewById(R.id.previousButton)
        nextButton = findViewById(R.id.nextButton)

        previousButton.isEnabled = viewPager.currentItem > 0
        previousButton.visibility = if (viewPager.currentItem > 0) View.VISIBLE else View.INVISIBLE
        previousButton.setOnClickListener(navigationButtonListener)

        nextButton.isEnabled = false
        nextButton.setOnClickListener(navigationButtonListener)

        pageFragmentViewModel.onChanges.observe(this) {
            // in conflict with removePage() if offscreenPageLimit is too high…
            if (viewPager.offscreenPageLimit > 1) adapter.notifyDataSetChanged()
            else it.dispatchUpdatesTo(adapter)
            
            pagerIndicator.notifyDataSetChanged()
            onPageSelected(viewPager.currentItem)
        }
    }

    override fun goToPreviousPage() {
        val currentItem = viewPager.currentItem

        if (currentItem > 0) {
            viewPager.setCurrentItem(
                currentItem - 1,
                true
            )
        }
    }

    override fun goToNextPage() {
        val currentItem = viewPager.currentItem

        if (currentItem < adapter.itemCount - 1) {
            val pageFragment = getPageFragment(currentItem)
            if (pageFragment != null || (pageFragment is IPageWithValidationFragment && (pageFragment as IPageWithValidationFragment).validate())) {
                if (BuildConfig.DEBUG) {
                    pageFragmentViewModel.pageFragments.keys
                        .elementAtOrNull(currentItem + 1)
                        ?.also {
                            Log.d(
                                TAG,
                                "goToNextPage: '${getString(it)}'"
                            )
                        }
                }

                viewPager.setCurrentItem(
                    currentItem + 1,
                    true
                )
            }
        }
    }

    override fun goToPage(position: Int) {
        viewPager.setCurrentItem(
            position,
            true
        )
    }

    override fun goToPageByKey(key: Int) {
        pageFragmentViewModel
            .getPagePosition(key)
            ?.also {
                viewPager.setCurrentItem(
                    it,
                    true
                )
            }
    }

    /**
     * Performs validation on the current page.
     */
    override fun validateCurrentPage() {
        getCurrentPageFragment()?.also { pageFragment ->
            nextButton.isEnabled = pageFragment
                .takeIf { it is IPageWithValidationFragment }
                ?.let { it as IPageWithValidationFragment }
                ?.validate()
                ?: true
        }
    }

    override fun addPage(vararg pageFragment: Pair<Int, IPageFragment>) {
        pageFragmentViewModel.add(*pageFragment)
    }

    override fun removePage(vararg key: Int) {
        pageFragmentViewModel.remove(*key)
    }

    /**
     * Gets the current [IPageFragment] instance at the current position of this pager.
     *
     * @return [IPageFragment] instance
     */
    fun getCurrentPageFragment(): IPageFragment? {
        return getPageFragment(viewPager.currentItem)
    }

    /**
     * The default title of this activity.
     */
    protected abstract fun getDefaultTitle(): CharSequence

    /**
     * Called on 'next' button is clicked.
     *
     * @return `false` to allow default action to be proceeded, `true` to apply custom action.
     */
    protected abstract fun onNextAction(): Boolean

    /**
     * Called on 'finish' button is clicked (the last page).
     */
    protected abstract fun performFinishAction()

    protected open fun onPageSelected(position: Int) {
        if (BuildConfig.DEBUG) {
            Log.d(TAG,
                "onPageSelected: $position, [${
                    pageFragmentViewModel.pageFragments.keys
                        .map { getString(it) }
                        .joinToString(", ") { "'$it'" }
                }]")
        }

        // sets default paging control
        viewPager.isUserInputEnabled = true

        // checks validation before switching to the next page
        if (position > 0 && (getPageFragment(position - 1)
                ?.takeIf { it is IPageWithValidationFragment }
                ?.let { it as IPageWithValidationFragment }
                ?.validate() == false)
        ) {
            Log.d(
                TAG,
                "redirect to previous page: ${position - 1} (not valid)"
            )

            goToPreviousPage()

            return
        }

        // updates title
        title = getDefaultTitle()
        supportActionBar?.subtitle = null

        // updates navigation buttons statuses

        previousButton.isEnabled = position > 0
        previousButton.visibility = if (position > 0) View.VISIBLE else View.INVISIBLE

        nextButton.setText(if (position < adapter.itemCount - 1) R.string.button_pager_next else R.string.button_pager_finish)
        nextButton.isEnabled = false

        getPageFragment(position)?.also { pageFragment ->
            setTitle(pageFragment.getResourceTitle())
            supportActionBar?.subtitle = pageFragment.getSubtitle()

            // disable or enable paging control for the current instance of IPageFragment
            viewPager.isUserInputEnabled = pageFragment.pagingEnabled()
        }

        validateCurrentPage()
    }

    /**
     * Gets the current [IPageFragment] instance at given position of this pager.
     *
     * @param position the position of [IPageFragment] to retrieve
     * @return [IPageFragment] instance
     */
    private fun getPageFragment(position: Int): IPageFragment? {
        val fragment = pageFragmentViewModel.getPageAtPosition(position)

        return if (fragment != null) {
            fragment
        } else {
            Log.w(
                TAG,
                "getPageFragment: no fragment found at position $position"
            )

            null
        }
    }

    companion object {
        private val TAG = AbstractPagerFragmentActivity::class.java.name
    }
}