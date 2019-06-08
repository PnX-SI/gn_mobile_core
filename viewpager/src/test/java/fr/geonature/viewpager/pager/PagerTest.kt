package fr.geonature.viewpager.pager

import android.os.Parcel
import org.junit.Assert.assertEquals
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit test for [Pager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PagerTest {

    @Test
    fun testParcelable() {

        // given a pager metadata
        val pager = Pager(1234L)
        pager.size = 5
        pager.position = 3
        pager.history.add(1)
        pager.history.add(4)
        pager.history.add(3)
        pager.history.add(2)

        // when we obtain a Parcel object to write the Pager instance to it
        val parcel = Parcel.obtain()
        pager.writeToParcel(parcel,
                            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(pager,
                     Pager.createFromParcel(parcel))
    }
}