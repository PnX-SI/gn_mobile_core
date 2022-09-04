package fr.geonature.viewpager.ui

import androidx.viewpager2.widget.ViewPager2

/**
 * A [IPagerIndicator] is responsible to show a visual indicator on the total views number and the
 * current visible view.
 *
 * @author S. Grimault
 */
interface IPagerIndicator {

    /**
     * Bind the indicator to a [ViewPager2].
     *
     * @param viewPager the [ViewPager2] to bind
     */
    fun setViewPager(viewPager: ViewPager2)

    /**
     * Bind the indicator to a [ViewPager2].
     *
     * @param viewPager the [ViewPager2] to bind
     * @param initialPosition the current position of the [ViewPager2]
     */
    fun setViewPager(
        viewPager: ViewPager2,
        initialPosition: Int
    )

    /**
     * Set the current page of both the [ViewPager2] and indicator.
     *
     * This **must** be used if you need to set the page before
     * the views are drawn on screen (e.g., default start page).
     *
     * @param item the current item position of the [ViewPager2]
     */
    fun setCurrentItem(item: Int)

    /**
     * Notify the indicator that the [ViewPager2] list has changed.
     */
    fun notifyDataSetChanged()
}
