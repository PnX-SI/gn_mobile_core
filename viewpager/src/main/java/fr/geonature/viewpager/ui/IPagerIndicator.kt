package fr.geonature.viewpager.ui

import androidx.viewpager.widget.ViewPager

/**
 * A [IPagerIndicator] is responsible to show a visual indicator on the total views number and the
 * current visible view.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IPagerIndicator : ViewPager.OnPageChangeListener {

    /**
     * Bind the indicator to a [ViewPager].
     *
     * @param viewPager the [ViewPager] to bind
     */
    fun setViewPager(viewPager: ViewPager)

    /**
     * Bind the indicator to a [ViewPager].
     *
     * @param viewPager       the [ViewPager] to bind
     * @param initialPosition the current position of the [ViewPager]
     */
    fun setViewPager(viewPager: ViewPager,
                     initialPosition: Int)

    /**
     * Set the current page of both the [ViewPager] and indicator.
     *
     * This **must** be used if you need to set the page before
     * the views are drawn on screen (e.g., default start page).
     *
     * @param item the current item position of the [ViewPager]
     */
    fun setCurrentItem(item: Int)

    /**
     * Notify the indicator that the [ViewPager] list has changed.
     */
    fun notifyDataSetChanged()
}
