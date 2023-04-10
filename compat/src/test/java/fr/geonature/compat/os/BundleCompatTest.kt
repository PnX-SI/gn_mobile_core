package fr.geonature.compat.os

import android.os.Build
import android.os.Bundle
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

/**
 * Unit tests about [Bundle] compat methods.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class BundleCompatTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get parcelable extra from bundle (lollipop)`() {
        val expectedParcelable = DummyParcelable("value")

        val bundle = Bundle().apply {
            putParcelable(
                "dummy",
                expectedParcelable
            )
        }

        assertEquals(
            expectedParcelable,
            bundle.getParcelableCompat<DummyParcelable>("dummy")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get parcelable extra from bundle (tiramisu)`() {
        val expectedParcelable = DummyParcelable("value")

        val bundle = Bundle().apply {
            putParcelable(
                "dummy",
                expectedParcelable
            )
        }

        assertEquals(
            expectedParcelable,
            bundle.getParcelableCompat<DummyParcelable>("dummy")
        )
    }

    @Test
    fun `should return null from undefined parcelable extra from bundle`() {
        assertNull(Bundle().getParcelableCompat<DummyParcelable>("no_such_property"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get parcelable array extra from bundle (lollipop)`() {
        val expectedParcelables = arrayOf(DummyParcelable("value"))

        val bundle = Bundle().apply {
            putParcelableArray(
                "dummies",
                expectedParcelables
            )
        }

        assertArrayEquals(
            expectedParcelables,
            bundle.getParcelableArrayCompat<DummyParcelable>("dummies")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get parcelable array extra from bundle (tiramisu)`() {
        val expectedParcelables = arrayOf(DummyParcelable("value"))

        val bundle = Bundle().apply {
            putParcelableArray(
                "dummies",
                expectedParcelables
            )
        }

        assertArrayEquals(
            expectedParcelables,
            bundle.getParcelableArrayCompat<DummyParcelable>("dummies")
        )
    }

    @Test
    fun `should return null from undefined parcelable array extra from bundle`() {
        assertNull(Bundle().getParcelableArrayCompat<DummyParcelable>("no_such_property"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get serializable extra from bundle (lollipop)`() {
        val expectedDate = Date()

        val bundle = Bundle().apply {
            putSerializable(
                "date",
                expectedDate
            )
        }

        assertEquals(
            expectedDate,
            bundle.getSerializableCompat<Date>("date")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get serializable extra from bundle (tiramisu)`() {
        val expectedDate = Date()

        val bundle = Bundle().apply {
            putSerializable(
                "date",
                expectedDate
            )
        }

        assertEquals(
            expectedDate,
            bundle.getSerializableCompat<Date>("date")
        )
    }

    @Test
    fun `should return null from undefined serializable extra from bundle`() {
        assertNull(Bundle().getSerializableCompat<Date>("no_such_property"))
    }

    @Parcelize
    data class DummyParcelable(val property: String) : Parcelable
}