package fr.geonature.commons.data.helper

import fr.geonature.commons.data.helper.EntityHelper.column
import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests about [EntityHelper].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class EntityHelperTest {

    @Test
    fun testColumn() {
        assertEquals(Pair("\"column\"",
                          "\"column\""),
                     column("column"))
        assertEquals(Pair("table.\"column\"",
                          "\"table_column\""),
                     column("column",
                            "table"))
    }
}