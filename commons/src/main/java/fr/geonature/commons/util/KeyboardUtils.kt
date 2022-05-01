package fr.geonature.commons.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager

/**
 * Utility methods for manipulating the onscreen keyboard.
 *
 * @author S. Grimault
 */
object KeyboardUtils {

    /**
     * Hides the soft keyboard from given View.
     */
    fun hideSoftKeyboard(view: View) {
        (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.hideSoftInputFromWindow(
            view.windowToken,
            0
        )
    }

    /**
     * Shows the soft keyboard from given View.
     */
    fun showSoftKeyboard(view: View) {
        (view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager?)?.also {
            view.requestFocus()
            view.postDelayed(
                {
                    it.showSoftInput(
                        view,
                        0
                    )
                },
                100
            )
        }
    }
}
