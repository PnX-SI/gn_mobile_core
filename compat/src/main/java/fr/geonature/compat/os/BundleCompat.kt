package fr.geonature.compat.os

import android.os.Build
import android.os.Build.VERSION_CODES
import android.os.Bundle

/**
 * Utility functions about Bundle.
 *
 * @author S. Grimault
 */

/**
 * Returns the value associated with the given key or `null` if:
 * * No mapping of the desired type exists for the given key.
 * * A `null` value is explicitly associated with the key.
 * * The object is not of type [T].
 *
 * @param key a String
 * @param T The type of the object expected
 * @return a Parcelable value, or `null`
 */
@Suppress("DEPRECATION")
inline fun <reified T> Bundle.getParcelableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelable(
        key,
        T::class.java,
    )
    else -> getParcelable(key) as? T?
}

/**
 * Returns the value associated with the given key or `null` if:
 * * No mapping of the desired type exists for the given key.
 * * A `null` value is explicitly associated with the key.
 * * The object is not of type [T].
 *
 * @param key a String
 * @param T The type of the object expected
 * @return a Parcelable array value, or `null`
 */
inline fun <reified T> Bundle.getParcelableArrayCompat(key: String): Array<T>? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getParcelableArray(
        key,
        T::class.java,
    )
    else -> @Suppress("DEPRECATION")
    getParcelableArray(key)?.filterIsInstance<T>()
        ?.toTypedArray()
}

/**
 * Returns the value associated with the given key or `null` if:
 * * No mapping of the desired type exists for the given key.
 * * A `null` value is explicitly associated with the key.
 * * The object is not of type [T].
 *
 * @param key a String
 * @param T The type of the object expected
 * @return a Serializable value, or `null`
 */
@Suppress("DEPRECATION")
inline fun <reified T : java.io.Serializable> Bundle.getSerializableCompat(key: String): T? = when {
    Build.VERSION.SDK_INT >= VERSION_CODES.TIRAMISU -> getSerializable(
        key,
        T::class.java,
    )
    else -> getSerializable(key) as? T?
}
