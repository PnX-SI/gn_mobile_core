package fr.geonature.commons.util

import fr.geonature.commons.util.StringUtils.isEmpty
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * Unit test for [StringUtils].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class StringUtilsTest {

    @Test
    fun testIsEmpty() {
        assertTrue(isEmpty(null))
        assertTrue(isEmpty(""))
        assertFalse(isEmpty(" "))
        assertFalse(isEmpty("_"))
    }
}