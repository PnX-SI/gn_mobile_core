package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcel
import fr.geonature.commons.data.entity.Taxonomy.Companion.ANY
import fr.geonature.commons.data.entity.Taxonomy.Companion.defaultProjection
import fr.geonature.commons.data.entity.Taxonomy.Companion.fromCursor
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [Taxonomy].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class TaxonomyTest {

    @MockK
    private lateinit var cursor: Cursor

    @Before
    fun setUp() {
        init(this)

        every { cursor.isClosed } returns false
    }

    @Test
    fun testEquals() {
        assertEquals(
            Taxonomy(
                "Animalia",
                "Ascidies"
            ),
            Taxonomy(
                "Animalia",
                "Ascidies"
            )
        )
    }

    @Test
    fun testSanitizeValues() {
        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "",
                ""
            )
        )

        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "all",
                "all"
            )
        )

        assertEquals(
            Taxonomy(
                "Animalia",
                ANY
            ),
            Taxonomy(
                "Animalia",
                "all"
            )
        )

        assertEquals(
            Taxonomy(
                "Animalia",
                ANY
            ),
            Taxonomy(
                "Animalia"
            )
        )

        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "Autre",
                "Autre"
            )
        )

        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "Autres",
                "Autres"
            )
        )

        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "autres",
                "autre"
            )
        )

        assertEquals(
            Taxonomy(
                ANY,
                ANY
            ),
            Taxonomy(
                "AUTRES",
                "AUTRE"
            )
        )

        assertEquals(
            Taxonomy(
                "Animalia",
                ANY
            ),
            Taxonomy(
                "Animalia",
                "autre"
            )
        )

        assertEquals(
            Taxonomy(
                "Animalia",
                ANY
            ),
            Taxonomy("Animalia")
        )
    }

    @Test
    fun testCreateFromCursor() {
        // given a mocked Cursor
        defaultProjection().forEachIndexed { index, c ->
            every { cursor.getColumnIndexOrThrow(c.second) } returns index
        }
        every { cursor.getString(0) } returns "Animalia"
        every { cursor.getString(1) } returns "Ascidies"

        // when getting a Taxonomy instance from Cursor
        val taxonomy = fromCursor(cursor)

        // then
        assertNotNull(taxonomy)
        assertEquals(
            Taxonomy(
                "Animalia",
                "Ascidies"
            ),
            taxonomy
        )
    }

    @Test
    fun testParcelable() {
        // given a Taxonomy instance
        val taxonomy = Taxonomy(
            "Animalia",
            "Ascidies"
        )

        // when we obtain a Parcel object to write the Taxonomy instance to it
        val parcel = Parcel.obtain()
        taxonomy.writeToParcel(
            parcel,
            0
        )

        // reset the parcel for reading
        parcel.setDataPosition(0)

        // then
        assertEquals(
            taxonomy,
            Taxonomy.CREATOR.createFromParcel(parcel)
        )
    }

    @Test
    fun testDefaultProjection() {
        assertArrayEquals(
            arrayOf(
                Pair(
                    "${Taxonomy.TABLE_NAME}.\"${Taxonomy.COLUMN_KINGDOM}\"",
                    "${Taxonomy.TABLE_NAME}_${Taxonomy.COLUMN_KINGDOM}"
                ),
                Pair(
                    "${Taxonomy.TABLE_NAME}.\"${Taxonomy.COLUMN_GROUP}\"",
                    "${Taxonomy.TABLE_NAME}_${Taxonomy.COLUMN_GROUP}"
                )
            ),
            defaultProjection()
        )
    }

    @Test
    fun testIsAny() {
        assertFalse(
            Taxonomy(
                "Animalia",
                "Ascidies"
            ).isAny()
        )
        assertFalse(
            Taxonomy(
                "Animalia"
            ).isAny()
        )
        assertTrue(
            Taxonomy(
                ""
            ).isAny()
        )
    }
}
