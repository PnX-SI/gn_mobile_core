package fr.geonature.commons.data.helper

import org.junit.Assert.assertEquals
import org.junit.Test

/**
 * Unit tests about `SQLStringHelper`.
 *
 * @author S. Grimault
 */
class SQLStringHelperTest {

    @Test
    fun `should normalize query string`() {
        assertEquals(
            "*[aáàäâãAÁÀÄÂÃ][nñNÑ][eéèëêẽEÉÈËÊẼ]*",
            "âne".sqlNormalize()
        )
        assertEquals(
            "*[fF][rR][eéèëêẽEÉÈËÊẼ][lL][oóòöôõõOÓÒÖÔÕ][nñNÑ] [dD][''][eéèëêẽEÉÈËÊẼ][uúùüûũUÚÙÜÛŨ][rR][oóòöôõõOÓÒÖÔÕ][pP][eéèëêẽEÉÈËÊẼ]*",
            "Frelon d'Europe".sqlNormalize()
        )
    }

    @Test
    fun `should SQL escape query string`() {
        assertEquals(
            "Frelon d''Europe",
            "Frelon d'Europe".sqlEscape()
        )
        assertEquals(
            "Frelon d''''Europe",
            "Frelon d''Europe".sqlEscape()
        )
    }
}