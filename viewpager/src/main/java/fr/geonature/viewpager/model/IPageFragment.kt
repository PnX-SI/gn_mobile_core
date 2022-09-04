package fr.geonature.viewpager.model

import androidx.annotation.StringRes

/**
 * Describes a `Fragment` page.
 *
 * @author S. Grimault
 */
interface IPageFragment {

    /**
     * Returns the view title.
     *
     * **Note:** Must be a constant value (e.g. `R.string.my_value`).
     *
     * @return view title as resource ID
     */
    @StringRes
    fun getResourceTitle(): Int

    /**
     * Returns the view subtitle.
     *
     * @return view subtitle
     */
    fun getSubtitle(): CharSequence?

    /**
     * Returns if user initiated scrolling between pages is enabled. Enabled by default.
     *
     * @return `true` if user can scroll the view pager, `false` otherwise
     */
    fun pagingEnabled(): Boolean
}