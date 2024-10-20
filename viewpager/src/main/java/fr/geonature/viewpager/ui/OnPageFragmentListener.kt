package fr.geonature.viewpager.ui

import fr.geonature.viewpager.model.IPageFragment

/**
 * Callback used within pages to control [AbstractPagerFragmentActivity] inner view pager.
 *
 * @author S. Grimault
 */
interface OnPageFragmentListener {

    /**
     * Go to the previous page and perform a smooth animation from the current item to the next item.
     */
    fun goToPreviousPage()

    /**
     * Go to the next page and perform a smooth animation from the current item to the previous item.
     */
    fun goToNextPage()

    /**
     * Set the currently selected page and perform a smooth animation from the current item to the
     * new item.
     */
    fun goToPage(position: Int)

    /**
     * Set the currently selected page by its key and perform a smooth animation from the current
     * item to the new item.
     */
    fun goToPageByKey(key: Int)

    /**
     * Perform validation of the current selected page.
     */
    fun validateCurrentPage()

    /**
     * Adds new pages at the end of the view pager.
     */
    fun addPage(vararg pageFragment: Pair<Int, IPageFragment>)

    /**
     * Removes existing pages from keys.
     */
    fun removePage(vararg key: Int)
}