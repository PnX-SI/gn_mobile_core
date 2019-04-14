package fr.geonature.viewpager.ui

import androidx.annotation.StringRes

/**
 * `Fragment` with a validation control step.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IValidateFragment {

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
     * Enables or not the paging control.
     *
     * @return `true` if the paging control is enabled.
     */
    fun pagingEnabled(): Boolean

    /**
     * Validate the current view.
     *
     * @return `true` if this view is validated, `false` otherwise
     */
    fun validate(): Boolean

    /**
     * Updates the current view.
     */
    fun refreshView()
}
