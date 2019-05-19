package fr.geonature.commons.input.io

import android.util.JsonReader
import android.util.JsonWriter
import fr.geonature.commons.MockitoKotlinHelper.any
import fr.geonature.commons.input.DummyInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito
import org.mockito.Mockito.`when`
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.spy
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonWriter].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonWriterTest {

    private lateinit var inputJsonWriter: InputJsonWriter
    private lateinit var inputJsonReader: InputJsonReader

    @Mock
    private lateinit var onInputJsonWriterListener: InputJsonWriter.OnInputJsonWriterListener

    @Mock
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyInput()).`when`(onInputJsonReaderListener)
            .createInput()

        inputJsonWriter = spy(InputJsonWriter(onInputJsonWriterListener))
        inputJsonReader = spy(InputJsonReader(onInputJsonReaderListener))
    }

    @Test
    fun testWriteEmptyInput() {
        `when`(onInputJsonReaderListener.readAdditionalInputData(any(JsonReader::class.java),
                                                                 any(String::class.java),
                                                                 any(DummyInput::class.java))).then {
            (it.getArgument(0) as JsonReader).skipValue()
        }

        // given an empty Input
        val input = DummyInput().apply { id = 1234 }

        // when write this Input as JSON string
        val json = inputJsonWriter.write(input)

        // then
        assertNotNull(json)

        // then
        Mockito.verify(onInputJsonWriterListener,
                       Mockito.atMost(1))
            .writeAdditionalInputData(any(JsonWriter::class.java),
                                      any(DummyInput::class.java))

        val inputFromJson = inputJsonReader.read(json)

        assertNotNull(inputFromJson)
        assertEquals(input.id,
                     inputFromJson?.id)
        assertEquals(input.module,
                     inputFromJson?.module)
    }
}