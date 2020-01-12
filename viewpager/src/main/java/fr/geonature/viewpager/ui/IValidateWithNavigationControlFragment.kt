package fr.geonature.viewpager.ui

/**
 * Adding navigation control to [IValidateFragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface IValidateWithNavigationControlFragment : IValidateFragment {

    /**
     * Enables or not the paging control to go to the previous page
     *
     * @return `true` if the paging control is enabled or not for going to the previous page.
     */
    fun getPagingToPreviousEnabled(): Boolean

    /**
     * Enables or not the paging control to go to the next page
     *
     * @return `true` if the paging control is enabled or not for going to the next page.
     */
    fun getPagingToForwardEnabled(): Boolean
}
