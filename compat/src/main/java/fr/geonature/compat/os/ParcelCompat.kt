package fr.geonature.compat.os

import android.os.Build
import android.os.Parcel

/**
 * Utility functions about Parcel.
 *
 * @author S. Grimault
 */

/**
 * Read and return a new Parcelable from the parcel.
 *
 * @param T The type of the object expected
 * @return the newly created Parcelable, or `null` if a `null` object has been written
 */
@Suppress("DEPRECATION")
inline fun <reified T> Parcel.readParcelableCompat(): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> readParcelable(
            T::class.java.classLoader,
            T::class.java
        )
        else -> readParcelable(T::class.java.classLoader) as? T?
    }

/**
 * Read and return a new Serializable object from the parcel.
 *
 * @param T The type of the object expected
 * @return the Serializable object, or `null` if the Serializable name wasn't found in the parcel
 */
@Suppress("DEPRECATION")
inline fun <reified T : java.io.Serializable> Parcel.readSerializableCompat(): T? =
    when {
        Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> readSerializable(
            T::class.java.classLoader,
            T::class.java,
        )
        else -> readSerializable() as T?
    }