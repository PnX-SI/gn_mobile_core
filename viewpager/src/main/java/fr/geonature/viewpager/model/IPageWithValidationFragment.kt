package fr.geonature.viewpager.model

/**
 * `Fragment` page with a validation control step.
 *
 * @author S. Grimault
 */
interface IPageWithValidationFragment : IPageFragment {

    /**
     * Validate the current page.
     *
     * @return `true` if this view is validated, `false` otherwise
     */
    fun validate(): Boolean
}