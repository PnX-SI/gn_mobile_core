package fr.geonature.commons.util

import android.content.Context
import android.view.View
import android.view.inputmethod.InputMethodManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment

/**
 * Utility methods for manipulating the onscreen keyboard.
 *
 * @author S. Grimault
 */
object KeyboardUtils {

    /**
     * Hides the soft keyboard from given Activity.
     */
    fun hideKeyboard(activity: AppCompatActivity) {
        activity.currentFocus?.also {
            hideSoftKeyboard(it)
        }
    }

    /**
     * Hides the soft keyboard from given Fragment.
     */
    fun hideKeyboard(fragment: Fragment) {
        fragment.view?.rootView?.also {
            hideSoftKeyboard(it)
        }
    }

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
