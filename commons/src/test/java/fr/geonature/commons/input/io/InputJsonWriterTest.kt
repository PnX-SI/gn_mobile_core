package fr.geonature.commons.input.io

import android.util.JsonWriter
import fr.geonature.commons.FixtureHelper
import fr.geonature.commons.MockitoKotlinHelper.any
import fr.geonature.commons.input.DummyInput
import fr.geonature.commons.settings.DummyAppSettings
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.ArgumentMatchers.eq
import org.mockito.Mock
import org.mockito.Mockito.isNull
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<DummyInput, DummyAppSettings>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        openMocks(this)

        inputJsonWriter = spy(InputJsonWriter(onInputJsonWriterListener))
    }

    @Test
    fun `should write empty input`() {
        // given an empty Input
        val input = DummyInput().apply { id = 1234 }

        // when write this Input as JSON string
        val json = inputJsonWriter
            .setIndent("  ")
            .write(input)

        // then
        assertNotNull(json)

        // then
        verify(onInputJsonWriterListener,).writeAdditionalInputData(
            any(JsonWriter::class.java),
            any(DummyInput::class.java),
            isNull()
        )

        assertEquals(
            FixtureHelper.getFixture("input_empty.json"),
            json
        )
    }

    @Test
    fun `should write empty input with additional settings`() {
        // given an empty Input
        val input = DummyInput().apply { id = 1234 }
        // and settings
        val settings = DummyAppSettings(attribute = "some_attribute")

        // when write this Input as JSON string
        val json = inputJsonWriter
            .setIndent("  ")
            .write(
                input,
                settings
            )

        // then
        assertNotNull(json)

        // then
        verify(onInputJsonWriterListener).writeAdditionalInputData(
            any(JsonWriter::class.java),
            any(DummyInput::class.java),
            eq(settings)
        )

        assertEquals(
            FixtureHelper.getFixture("input_empty.json"),
            json
        )
    }
}
