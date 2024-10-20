package fr.geonature.commons.util

import android.util.JsonReader
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import java.io.StringReader

/**
 * Unit test for `JsonHelper`.
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class JsonHelperTest {

    @Test
    fun `should returns a map representation from json string`() {
        assertEquals(
            mapOf(
                "key_string" to "value",
                "key_number" to 8,
                "key_array" to listOf(
                    1,
                    2,
                    3
                ),
                "key_object_empty" to mapOf<String, Any>(),
                "key_object" to mapOf(
                    "key_string" to "value",
                    "key_null" to null,
                    "key_array" to listOf(
                        1,
                        2,
                        3,
                        null,
                        mapOf("key_string" to "value")
                    )
                )
            ),
            JSONObject(
                """{
                "key_string": "value",
                "key_number": 8,
                "key_array": [1, 2, 3],
                "key_object_empty": {},
                "key_object": {
                    "key_string": "value",
                    "key_null": null,
                    "key_array": [
                        1,
                        2,
                        3,
                        null,
                        {
                             "key_string": "value"
                        }
                    ]
                }
            }""".trimIndent()
            ).toMap()
        )
    }

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

    @Test
    fun `should read boolean value from JSON property`() {
        val jsonReader = JsonReader(StringReader("{\"key\":true}"))
        var value: Boolean? = null

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "key" -> value = jsonReader.nextBooleanOrElse { false }
            }
        }

        jsonReader.endObject()

        assertNotNull(value)
        assertTrue(value!!)
    }

    @Test
    fun `should read non invalid boolean value from JSON property`() {
        val jsonReader = JsonReader(StringReader("{\"key\":\"no_such_value\"}"))
        var value: Boolean? = null

        jsonReader.beginObject()

        while (jsonReader.hasNext()) {
            when (jsonReader.nextName()) {
                "key" -> value = jsonReader.nextBooleanOrElse { false }
            }
        }

        jsonReader.endObject()

        assertNotNull(value)
        assertFalse(value!!)
    }
}