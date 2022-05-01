package fr.geonature.commons.util

import android.content.pm.PackageManager
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat

/**
 * Helper class about Android permissions.
 *
 * @author S. Grimault
 */
object PermissionUtils {

    /**
     * Requests a set of permissions from `Activity`.
     *
     * @param fromActivity the current `Activity`
     * @param permissions a set of permissions to request
     * @param isGranted called when permissions were granted or not
     * @param shouldShowRequestPermissionRationale called if we want to show UI with rationale
     * before requesting a permission
     */
    fun requestPermissions(
        fromActivity: AppCompatActivity,
        permissions: List<String>,
        isGranted: (result: Map<String, Boolean>) -> Unit,
        shouldShowRequestPermissionRationale: ((callback: () -> Unit) -> Unit)? = null
    ) {
        val requestPermissionLauncher =
            fromActivity.registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) {
                isGranted(
                    mapOf(
                        Pair(
                            permissions.first(),
                            it
                        )
                    )
                )
            }

        val requestPermissionsLauncher =
            fromActivity.registerForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { isGranted(it) }

        val checkSelfPermissions = permissions.asSequence()
            .map {
                Pair(
                    it,
                    ActivityCompat.checkSelfPermission(
                        fromActivity,
                        it
                    ) == PackageManager.PERMISSION_GRANTED
                )
            }
            .toMap()

        when {
            // all permissions were granted
            checkSelfPermissions.values.all { it } -> isGranted(
                permissions.asSequence()
                    .map {
                        Pair(
                            it,
                            true
                        )
                    }
                    .toMap()
            )
            // show request permission rationale only for one non granted permission
            shouldShowRequestPermissionRationale != null && permissions.size == 1 && ActivityCompat.shouldShowRequestPermissionRationale(
                fromActivity,
                permissions.first()
            ) -> shouldShowRequestPermissionRationale {
                requestPermissionLauncher.launch(permissions.first())
            }
            // ask for one permission
            permissions.size == 1 -> requestPermissionLauncher.launch(permissions.first())
            // ask for several permissions
            else -> requestPermissionsLauncher.launch(
                checkSelfPermissions.asSequence()
                    .filter { !it.value }
                    .map { it.key }
                    .toList()
                    .toTypedArray()
            )
        }
    }
}
