package fr.geonature.commons.features.input.io

import fr.geonature.commons.FixtureHelper
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import fr.geonature.commons.settings.DummyAppSettings
import io.mockk.MockKAnnotations.init
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<DummyInput, DummyAppSettings>

    @RelaxedMockK
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        inputJsonWriter = InputJsonWriter(onInputJsonWriterListener)
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
        verify {
            onInputJsonWriterListener.writeAdditionalInputData(
                any(),
                any(),
                null
            )
        }

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
        verify {
            onInputJsonWriterListener.writeAdditionalInputData(
                any(),
                any(),
                settings
            )
        }

        assertEquals(
            FixtureHelper.getFixture("input_empty.json"),
            json
        )
    }

    @Test
    fun `should write input with status`() {
        assertEquals(
            """{"id":1234,"module":"dummy","status":"draft"}""",
            inputJsonWriter.write(DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.DRAFT
            })
        )
        assertEquals(
            """{"id":1234,"module":"dummy","status":"to_sync"}""",
            inputJsonWriter.write(DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.TO_SYNC
            })
        )
    }
}
