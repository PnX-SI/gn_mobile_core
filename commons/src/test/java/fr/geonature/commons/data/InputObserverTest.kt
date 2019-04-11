package fr.geonature.commons.data

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputObserver].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputObserverTest {

    @Test
    fun testEquals() {
        assertEquals(
            InputObserver(1234, "lastname", "firstname"),
            InputObserver(1234, "lastname", "firstname")
        )
    }

    @Test
    fun testParcelable() {
        // given InputObserver
        val inputObserver = InputObserver(1234, "lastname", "firstname")

        // when we obtain a Parcel object to write the InputObserver instance to it
        val parcel = Parcel.obtain()
        inputObserver.writeToParcel(parcel, 0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(inputObserver, InputObserver.CREATOR.createFromParcel(parcel))
    }
}