package fr.geonature.commons.data

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.Taxonomy.Companion.ANY
import fr.geonature.commons.data.Taxonomy.Companion.fromCursor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Taxonomy].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class TaxonomyTest {

    @Test
    fun testEquals() {
        assertEquals(Taxonomy("Animalia",
                              "Ascidies"),
                     Taxonomy("Animalia",
                              "Ascidies"))
    }

    @Test
    fun testSanitizeValues() {
        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("",
                              ""))

        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("all",
                              "all"))

        assertEquals(Taxonomy("Animalia",
                              ANY),
                     Taxonomy("Animalia",
                              "all"))

        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("Autre",
                              "Autre"))

        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("Autres",
                              "Autres"))

        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("autres",
                              "autre"))

        assertEquals(Taxonomy(ANY,
                              ANY),
                     Taxonomy("AUTRES",
                              "AUTRE"))

        assertEquals(Taxonomy("Animalia",
                              ANY),
                     Taxonomy("Animalia",
                              "autre"))
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        val cursor = mock(Cursor::class.java)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_KINGDOM)).thenReturn(0)
        `when`(cursor.getColumnIndexOrThrow(Taxonomy.COLUMN_GROUP)).thenReturn(1)
        `when`(cursor.getString(0)).thenReturn("Animalia")
        `when`(cursor.getString(1)).thenReturn("Ascidies")

        // when getting a Taxonomy instance from Cursor
        val taxonomy = fromCursor(cursor)

        // then
        assertNotNull(taxonomy)
        assertEquals(Taxonomy("Animalia",
                              "Ascidies"),
                     taxonomy)
    }

    @Test
    fun testParcelable() {
        // given a Taxonomy instance
        val taxonomy = Taxonomy("Animalia",
                                "Ascidies")

        // when we obtain a Parcel object to write the Taxonomy instance to it
        val parcel = Parcel.obtain()
        taxonomy.writeToParcel(parcel,
                               0)

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(taxonomy,
                     Taxonomy.CREATOR.createFromParcel(parcel))
    }
}