package fr.geonature.commons.features.input.io

import android.util.JsonReader
import fr.geonature.commons.FixtureHelper.getFixture
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import io.mockk.MockKAnnotations.init
import io.mockk.every
import io.mockk.impl.annotations.MockK
import io.mockk.verify
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [InputJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class InputJsonReaderTest {

    private lateinit var inputJsonReader: InputJsonReader<DummyInput>

    @MockK
    private lateinit var onInputJsonReaderListener: InputJsonReader.OnInputJsonReaderListener<DummyInput>

    @Before
    fun setUp() {
        init(this)

        every { onInputJsonReaderListener.createInput() } returns DummyInput()

        inputJsonReader = InputJsonReader(onInputJsonReaderListener)
    }

    @Test
    fun `should read input from invalid JSON string`() {
        // when read an invalid JSON as Input
        val input = inputJsonReader.read("")

        // then
        assertNull(input)
    }

    @Test
    fun `should read empty input`() {
        // given an input file to read
        val json = getFixture("input_empty.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        verify(inverse = true) {
            onInputJsonReaderListener.readAdditionalInputData(
                any(),
                any(),
                any()
            )
        }

        assertNotNull(input)
        assertEquals(
            1234L,
            input?.id
        )
        assertEquals(
            "dummy",
            input?.module
        )
    }

    @Test
    fun `should read input`() {
        every {
            onInputJsonReaderListener.readAdditionalInputData(
                any(),
                "date",
                any()
            )
        } answers {
            assertEquals(
                "2016-10-28",
                (firstArg() as JsonReader).nextString()
            )
        }

        // given an input file to read
        val json = getFixture("input_simple.json")

        // when parsing this file as Input
        val input = inputJsonReader.read(json)

        // then
        verify(atMost = 1) {
            onInputJsonReaderListener.readAdditionalInputData(
                any(),
                "date",
                any()
            )
        }

        assertNotNull(input)
    }

    @Test
    fun `should read input with valid status`() {
        assertEquals(
            AbstractInput.Status.DRAFT,
            inputJsonReader.read(
                """
                {
                    "id": 1234,
                    "module": "dummy",
                    "status": "draft"
                }
        """.trimIndent()
            )?.status
        )
        assertEquals(
            AbstractInput.Status.DRAFT,
            inputJsonReader.read(
                """
                {
                    "id": 1234,
                    "module": "dummy",
                    "status": "DRAFT"
                }
        """.trimIndent()
            )?.status
        )

        assertEquals(
            AbstractInput.Status.TO_SYNC,
            inputJsonReader.read(
                """
            {
                "id": 1234,
                "module": "dummy",
                "status": "to_sync"
            }
        """.trimIndent()
            )?.status
        )
        assertEquals(
            AbstractInput.Status.TO_SYNC,
            inputJsonReader.read(
                """
            {
                "id": 1234,
                "module": "dummy",
                "status": "TO_SYNC"
            }
        """.trimIndent()
            )?.status
        )
    }

    @Test
    fun `should read input with invalid status`() {
        assertEquals(
            AbstractInput.Status.DRAFT,
            inputJsonReader.read(
                """
                {
                    "id": 1234,
                    "module": "dummy",
                    "status": "no_such_status"
                }
        """.trimIndent()
            )?.status
        )

        assertEquals(
            AbstractInput.Status.DRAFT,
            inputJsonReader.read(
                """
                {
                    "id": 1234,
                    "module": "dummy",
                    "status": null
                }
        """.trimIndent()
            )?.status
        )

        assertEquals(
            AbstractInput.Status.DRAFT,
            inputJsonReader.read(
                """
                {
                    "id": 1234,
                    "module": "dummy"
                }
        """.trimIndent()
            )?.status
        )
    }
}
