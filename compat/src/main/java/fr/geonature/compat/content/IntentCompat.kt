package fr.geonature.compat.content

import android.content.Intent
import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Parcelable

/**
 * Utility functions about Intent.
 *
 * @author S. Grimault
 */

/**
 * Retrieve extended data from the intent.
 *
 * @param key The name of the desired item.
 * @param T The type of the object expected.
 *
 * @return the value of an item previously added with `putExtra()`,
 * or `null` if no [Parcelable] value was found.
 *
 * @see Intent.putExtra(String, Parcelable)
 */
@Suppress("DEPRECATION")
inline fun <reified T> Intent.getParcelableExtraCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableExtra(
        key,
        T::class.java
    )
    else -> getParcelableExtra(key) as? T?
}

/**
 * Retrieve extended data from the intent.
 *
 * @param key The name of the desired item.
 * @param T The type of the items inside the array.
 *
 * @return the value of an item previously added with `putExtra()`,
 * or `null` if no [Parcelable] array values was found.
 *
 * @see Intent.putExtra(String, Parcelable[])
 */
inline fun <reified T> Intent.getParcelableArrayExtraCompat(key: String): Array<T>? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableArrayExtra(
        key,
        T::class.java
    )
    else -> @Suppress("DEPRECATION")
    getParcelableArrayExtra(key)
        ?.filterIsInstance<T>()
        ?.toTypedArray()
}

/**
 * Retrieve extended data from the intent.
 *
 * @param key The name of the desired item.
 * @param T The type of the object expected.
 *
 * @return the value of an item previously added with `putExtra()`,
 * or `null` if no [Serializable] value was found.
 *
 * @see Intent.putExtra(String, Serializable)
 */
@Suppress("DEPRECATION")
inline fun <reified T : java.io.Serializable> Intent.getSerializableExtraCompat(key: String): T? =
    when {
        Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getSerializableExtra(
            key,
            T::class.java
        )
        else -> getSerializableExtra(key) as? T?
    }
