package fr.geonature.commons.util

import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.view.View
import androidx.annotation.NonNull
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.google.android.material.snackbar.BaseTransientBottomBar
import com.google.android.material.snackbar.Snackbar

/**
 * Helper class about Android permissions.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
object PermissionUtils {

    /**
     * Checks that all given permissions have been granted by verifying that each entry in the
     * given array is of the value [PackageManager.PERMISSION_GRANTED].
     *
     * @see Activity.onRequestPermissionsResult
     */
    fun checkPermissions(grantResults: IntArray): Boolean {
        // At least one result must be checked.
        if (grantResults.isEmpty()) {
            return false
        }

        // Verify that each required permission has been granted, otherwise return false.
        for (result in grantResults) {
            if (result != PackageManager.PERMISSION_GRANTED) {
                return false
            }
        }

        return true
    }

    /**
     * Determines whether the user have been granted a set of permissions.
     *
     * @param context the current `Context`.
     * @param permissions a set of permissions being checked
     */
    fun checkSelfPermissions(
        context: Context,
        vararg permissions: String
    ): Boolean {
        var granted = true
        val iterator = permissions.iterator()

        while (iterator.hasNext() && granted) {
            granted = ActivityCompat.checkSelfPermission(
                context,
                iterator.next()
            ) == PackageManager.PERMISSION_GRANTED
        }

        return granted
    }

    /**
     * Determines whether the user have been granted a set of permissions.
     *
     * @param context the current `Context`.
     * @param onCheckSelfPermissionListener the callback to use to notify if these permissions was
     * granted or not
     * @param permissions a set of permissions being checked
     */
    fun checkSelfPermissions(
        context: Context,
        onCheckSelfPermissionListener: OnCheckSelfPermissionListener,
        @NonNull
        vararg permissions: String
    ) {
        if (checkSelfPermissions(
                context,
                *permissions
            )
        ) {
            onCheckSelfPermissionListener.onPermissionsGranted()
        } else {
            onCheckSelfPermissionListener.onRequestPermissions(*permissions)
        }
    }

    /**
     * Requests a set of permissions from `Activity`.
     *
     * If a permission has been denied previously, a `Snackbar` will prompt the user to grant
     * the permission, otherwise it is requested directly.
     *
     * @param activity the current `Activity`
     * @param snackbarParentView the parent view on which to display the `Snackbar`
     * @param snackbarMessageResourceId the message resource ID to display
     * @param requestCode application specific request code to match with a result
     * reported to `ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])`.
     * @param permissions a set of permissions to request
     */
    fun requestPermissions(
        activity: Activity,
        snackbarParentView: View,
        snackbarMessageResourceId: Int,
        requestCode: Int,
        vararg permissions: String
    ) {
        var shouldShowRequestPermissions = false
        val iterator = permissions.iterator()

        while (iterator.hasNext() && !shouldShowRequestPermissions) {
            shouldShowRequestPermissions = ActivityCompat.shouldShowRequestPermissionRationale(
                activity,
                iterator.next()
            )
        }

        if (shouldShowRequestPermissions) {
            Snackbar.make(
                snackbarParentView,
                snackbarMessageResourceId,
                BaseTransientBottomBar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    ActivityCompat.requestPermissions(
                        activity,
                        permissions,
                        requestCode
                    )
                }
                .show()
        } else {
            ActivityCompat.requestPermissions(
                activity,
                permissions,
                requestCode
            )
        }
    }

    /**
     * Requests a set of permissions from a `Fragment`.
     *
     * If a permission has been denied previously, a `Snackbar` will prompt the user to grant
     * the permission, otherwise it is requested directly.
     *
     * @param fragment the current `Fragment`
     * @param snackbarParentView the parent view on which to display the `Snackbar`
     * @param snackbarMessageResourceId the message resource ID to display
     * @param requestCode application specific request code to match with a result
     * reported to `ActivityCompat.OnRequestPermissionsResultCallback#onRequestPermissionsResult(int, String[], int[])`.
     * @param permissions a set of permissions to request
     */
    fun requestPermissions(
        fragment: Fragment,
        snackbarParentView: View,
        snackbarMessageResourceId: Int,
        requestCode: Int,
        vararg permissions: String
    ) {
        var shouldShowRequestPermissions = false
        val iterator = permissions.iterator()

        while (iterator.hasNext() && !shouldShowRequestPermissions) {
            shouldShowRequestPermissions =
                fragment.shouldShowRequestPermissionRationale(iterator.next())
        }

        if (shouldShowRequestPermissions) {
            Snackbar.make(
                snackbarParentView,
                snackbarMessageResourceId,
                BaseTransientBottomBar.LENGTH_INDEFINITE
            )
                .setAction(android.R.string.ok) {
                    fragment.requestPermissions(
                        permissions,
                        requestCode
                    )
                }
                .show()
        } else {
            fragment.requestPermissions(
                permissions,
                requestCode
            )
        }
    }

    /**
     * Callback about [PermissionUtils.checkSelfPermissions].
     *
     * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
     */
    interface OnCheckSelfPermissionListener {
        fun onPermissionsGranted()
        fun onRequestPermissions(vararg permissions: String)
    }
}
