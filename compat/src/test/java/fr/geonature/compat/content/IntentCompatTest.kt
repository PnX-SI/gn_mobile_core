package fr.geonature.compat.content

import android.content.Intent
import android.os.Build
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
 * Unit tests about [Intent] compat methods.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class IntentHelperTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get parcelable extra from intent (lollipop)`() {
        val expectedParcelable = DummyParcelable("value")

        val intent = Intent().apply {
            putExtra(
                "dummy",
                expectedParcelable
            )
        }

        assertEquals(
            expectedParcelable,
            intent.getParcelableExtraCompat<DummyParcelable>("dummy")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get parcelable extra from intent (tiramisu)`() {
        val expectedParcelable = DummyParcelable("value")

        val intent = Intent().apply {
            putExtra(
                "dummy",
                expectedParcelable
            )
        }

        assertEquals(
            expectedParcelable,
            intent.getParcelableExtraCompat<DummyParcelable>("dummy")
        )
    }

    @Test
    fun `should return null from undefined parcelable extra from intent`() {
        assertNull(Intent().getParcelableExtraCompat<DummyParcelable>("no_such_property"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get parcelable array extra from intent (lollipop)`() {
        val expectedParcelables = arrayOf(DummyParcelable("value"))

        val intent = Intent().apply {
            putExtra(
                "dummies",
                expectedParcelables
            )
        }

        assertArrayEquals(
            expectedParcelables,
            intent.getParcelableArrayExtraCompat<DummyParcelable>("dummies")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get parcelable array extra from intent (tiramisu)`() {
        val expectedParcelables = arrayOf(DummyParcelable("value"))

        val intent = Intent().apply {
            putExtra(
                "dummies",
                expectedParcelables
            )
        }

        assertArrayEquals(
            expectedParcelables,
            intent.getParcelableArrayExtraCompat<DummyParcelable>("dummies")
        )
    }

    @Test
    fun `should return null from undefined parcelable array extra from intent`() {
        assertNull(Intent().getParcelableArrayExtraCompat<DummyParcelable>("no_such_property"))
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should get serializable extra from intent (lollipop)`() {
        val expectedDate = Date()

        val intent = Intent().apply {
            putExtra(
                "date",
                expectedDate
            )
        }

        assertEquals(
            expectedDate,
            intent.getSerializableExtraCompat<Date>("date")
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should get serializable extra from intent (tiramisu)`() {
        val expectedDate = Date()

        val intent = Intent().apply {
            putExtra(
                "date",
                expectedDate
            )
        }

        assertEquals(
            expectedDate,
            intent.getSerializableExtraCompat<Date>("date")
        )
    }

    @Test
    fun `should return null from undefined serializable extra from intent`() {
        assertNull(Intent().getSerializableExtraCompat<Date>("no_such_property"))
    }

    @Parcelize
    data class DummyParcelable(val property: String) : Parcelable
}