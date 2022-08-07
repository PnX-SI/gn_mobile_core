package fr.geonature.commons.data.helper

import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.EntityHelper.normalize
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests about [EntityHelper].
 *
 * @author S. Grimault
 */
class EntityHelperTest {

    @Test
    fun `should normalize query string`() {
        assertEquals(
            "*[aáàäâãAÁÀÄÂÃ][nñNÑ][eéèëêẽEÉÈËÊẼ]*",
            normalize("âne")
        )
        assertEquals(
            "*[aáàäâãAÁÀÄÂÃ][sS]*",
            normalize("as")
        )
    }

    @Test
    fun testColumn() {
        assertEquals(
            Pair(
                "\"column\"",
                "column"
            ),
            column("column")
        )
        assertEquals(
            Pair(
                "table.\"column\"",
                "table_column"
            ),
            column(
                "column",
                "table"
            )
        )
    }
}
