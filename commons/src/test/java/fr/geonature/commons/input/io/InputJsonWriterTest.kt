package fr.geonature.commons.input.io

import android.util.JsonWriter
import fr.geonature.commons.FixtureHelper
import fr.geonature.commons.MockitoKotlinHelper.any
import fr.geonature.commons.input.DummyInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.atMost
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.openMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter<DummyInput>

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener<DummyInput>

    @Before
    fun setUp() {
        openMocks(this)

        inputJsonWriter = spy(InputJsonWriter(onInputJsonWriterListener))
    }

    @Test
    fun testWriteEmptyInput() {
        // given an empty Input
        val input = DummyInput().apply { id = 1234 }

        // when write this Input as JSON string
        val json = inputJsonWriter.setIndent("  ")
            .write(input)

        // then
        assertNotNull(json)

        // then
        verify(
            onInputJsonWriterListener,
            atMost(1)
        ).writeAdditionalInputData(
            any(JsonWriter::class.java),
            any(DummyInput::class.java)
        )

        assertEquals(
            FixtureHelper.getFixture("input_empty.json"),
            json
        )
    }
}
