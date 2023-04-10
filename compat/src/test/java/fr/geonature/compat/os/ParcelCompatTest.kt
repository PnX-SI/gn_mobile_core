package fr.geonature.compat.os

import android.os.Build
import android.os.Parcel
import android.os.Parcelable
import kotlinx.parcelize.Parcelize
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.util.Date

/**
 * Unit tests about [Parcel] compat methods.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class ParcelCompatTest {

    @Test
    @Config(sdk = [Build.VERSION_CODES.LOLLIPOP])
    fun `should create DummyParcelable from Parcelable (lollipop)`() {
        // given a DummyParcelable instance
        val parcelable = DummyParcelable(DummyPropertyParcelable("some_property"), Date())

        // when we obtain a Parcel object to write the DummyPropertyParcelable instance to it
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            parcelable,
            DummyParcelable.createFromParcel(parcel)
        )
    }

    @Test
    @Config(sdk = [Build.VERSION_CODES.TIRAMISU])
    fun `should create DummyParcelable from Parcelable (tiramisu)`() {
        // given a DummyParcelable instance
        val parcelable = DummyParcelable(DummyPropertyParcelable("some_property"), Date())

        // when we obtain a Parcel object to write the DummyPropertyParcelable instance to it
        val parcel = Parcel.obtain()
        parcelable.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            parcelable,
            DummyParcelable.createFromParcel(parcel)
        )
    }

    data class DummyParcelable(
        val property: DummyPropertyParcelable,
        val date: Date
    ) : Parcelable {
        private constructor(source: Parcel) : this(
            source.readParcelableCompat()!!,
            source.readSerializableCompat()!!
        )

        override fun describeContents(): Int {
            return 0
        }

        override fun writeToParcel(
            dest: Parcel,
            flags: Int
        ) {
            dest.apply {
                writeParcelable(
                    property,
                    flags
                )
                writeSerializable(date)
            }
        }

        companion object CREATOR : Parcelable.Creator<DummyParcelable> {
            override fun createFromParcel(parcel: Parcel): DummyParcelable {
                return DummyParcelable(parcel)
            }

            override fun newArray(size: Int): Array<DummyParcelable?> {
                return arrayOfNulls(size)
            }
        }
    }

    @Parcelize
    data class DummyPropertyParcelable(val property: String) : Parcelable
}