package fr.geonature.commons.util

import android.util.JsonReader
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit test for `JsonReaderHelper`.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class JsonReaderHelperTest {

    @Test
    fun `should read string value from JSON property`() {
        val jsonReader = JsonReader(StringReader("{\"key\":\"value\"}"))
        var value: String? = null

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "key" -> value = jsonReader.nextStringOrNull()
            }
        }

        jsonReader.endObject()

        assertEquals(
            "value",
            value
        )
    }

    @Test
    fun `should read null value from JSON property with null value`() {
        val jsonReader = JsonReader(StringReader("{\"key\":null}"))
        var value: String? = "no_such_value"

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "key" -> value = jsonReader.nextStringOrNull()
            }
        }

        jsonReader.endObject()

        assertNull(value)
    }

    @Test
    fun `should read null value from JSON property with no string value`() {
        val jsonReader = JsonReader(StringReader("{\"key\":42}"))
        var value: String? = "no_such_value"

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "key" -> value = jsonReader.nextStringOrNull()
            }
        }

        jsonReader.endObject()

        assertNull(value)
    }
}