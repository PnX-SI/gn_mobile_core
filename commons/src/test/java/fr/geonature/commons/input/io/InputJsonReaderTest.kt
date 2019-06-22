package fr.geonature.commons.input.io

import android.util.JsonReader
import fr.geonature.commons.FixtureHelper.getFixture
import fr.geonature.commons.MockitoKotlinHelper.any
import fr.geonature.commons.MockitoKotlinHelper.eq
import fr.geonature.commons.input.DummyInput
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.mockito.Mock
import org.mockito.Mockito.`when`
import org.mockito.Mockito.atMost
import org.mockito.Mockito.doReturn
import org.mockito.Mockito.never
import org.mockito.Mockito.spy
import org.mockito.Mockito.verify
import org.mockito.MockitoAnnotations.initMocks
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonReader].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonReaderTest {

    private lateinit var inputJsonReader: InputJsonReader<DummyInput>

    @Mock
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Before
    fun setUp() {
        initMocks(this)

        doReturn(DummyInput()).`when`(onInputJsonReaderListener)
            .createInput()

        inputJsonReader = spy(InputJsonReader(onInputJsonReaderListener))
    }

    @Test
    fun testReadInputFromInvalidJsonString() {
        // when read an invalid JSON as Input
        val input = inputJsonReader.read("")

        // then
        assertNull(input)
    }

    @Test
    fun testReadEmptyInput() {
        // given an input file to read
        val json = getFixture("input_empty.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        verify(onInputJsonReaderListener,
               never()).readAdditionalInputData(any(JsonReader::class.java),
                                                any(String::class.java),
                                                any(DummyInput::class.java))

        assertNotNull(input)
        assertEquals(1234L,
                     input?.id)
        assertEquals("dummy",
                     input?.module)
    }

    @Test
    fun testReadInput() {
        `when`(onInputJsonReaderListener.readAdditionalInputData(any(JsonReader::class.java),
                                                                 eq("date"),
                                                                 any(DummyInput::class.java))).then {
            assertEquals("2016-10-28",
                         (it.getArgument(0) as JsonReader).nextString())
        }

        // given an input file to read
        val json = getFixture("input_simple.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        verify(onInputJsonReaderListener,
               atMost(1)).readAdditionalInputData(any(JsonReader::class.java),
                                                  eq("date"),
                                                  any(DummyInput::class.java))

        assertNotNull(input)
    }
}