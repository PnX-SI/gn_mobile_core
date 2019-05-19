package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import org.junit.Assert
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Taxon].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TaxonTest {

    fun testBuilder() {
        // given a taxon instance from its builder
        val taxon1 = Taxon.Builder()
            .id(1234)
            .name("taxon_01")
            .description("desc")
            .heritage(true)
            .build()

        // then
        Assert.assertNotNull(taxon1)
        Assert.assertEquals(Taxon(1234,
                                  "taxon_01",
                                  "desc",
                                  true),
                            taxon1)

        // given a taxon instance with default values from its builder
        val taxon2 = Taxon.Builder()
            .id(1235)
            .name("taxon_02")
            .build()

        // then
        Assert.assertNotNull(taxon2)
        Assert.assertEquals(Taxon(1235,
                                  "taxon_02",
                                  null,
                                  false),
                            taxon2)
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxon.COLUMN_ID)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(Taxon.COLUMN_NAME)).thenReturn(1)
        `when`(cursor.getColumnIndexOrThrow(Taxon.COLUMN_DESCRIPTION)).thenReturn(2)
        `when`(cursor.getColumnIndexOrThrow(Taxon.COLUMN_HERITAGE)).thenReturn(3)
        `when`(cursor.getLong(0)).thenReturn(1234)
        `when`(cursor.getString(1)).thenReturn("taxon_01")
        `when`(cursor.getString(2)).thenReturn("desc")
        `when`(cursor.getString(3)).thenReturn("True")

        // when getting Taxon instance from Cursor
        val taxon = Taxon.fromCursor(cursor)

        // then
        Assert.assertNotNull(taxon)
        Assert.assertEquals(Taxon(1234,
                                  "taxon_01",
                                  "desc",
                                  true),
                            taxon)
    }

    @Test
    fun testParcelable() {
        // given a Taxon
        val taxon = Taxon(1234,
                          "taxon_01",
                          "desc",
                          true)

        // when we obtain a Parcel object to write the Taxon instance to it
        val parcel = Parcel.obtain()
        taxon.writeToParcel(parcel,
                            0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        Assert.assertEquals(taxon,
                            Taxon.CREATOR.createFromParcel(parcel))
    }
}