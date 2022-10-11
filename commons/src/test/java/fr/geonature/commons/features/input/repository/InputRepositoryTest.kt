package fr.geonature.commons.features.input.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.features.input.data.IInputLocalDataSource
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import fr.geonature.commons.features.input.error.InputException
import fr.geonature.commons.features.input.error.InputFailure
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.settings.DummyAppSettings
import io.mockk.MockKAnnotations.init
import io.mockk.Runs
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.impl.annotations.MockK
import io.mockk.just
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [IInputRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class InputRepositoryTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var inputLocalDataSource: IInputLocalDataSource<DummyInput, DummyAppSettings>

    private lateinit var inputRepository: IInputRepository<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        inputRepository = InputRepositoryImpl(inputLocalDataSource)
    }

    @Test
    fun `should return an empty list when reading undefined inputs`() =
        runTest {
            // given an empty list from data source
            coEvery { inputLocalDataSource.readInputs() } returns emptyList()

            // when reading non existing inputs
            val result = inputRepository.readInputs()

            // then
            assertTrue(result.isRight)
            assertTrue(
                result
                    .orNull()
                    ?.isEmpty() == true
            )
        }

    @Test
    fun `should read existing inputs`() =
        runTest {
            // given some inputs from data source
            val expectedInputs = listOf(
                DummyInput().apply { id = 1234 },
                DummyInput().apply { id = 1235 },
                DummyInput().apply { id = 1236 },
            )
            coEvery { inputLocalDataSource.readInputs() } returns expectedInputs

            // when reading these inputs from repository
            val result = inputRepository.readInputs()

            // then
            assertTrue(result.isRight)
            assertArrayEquals(expectedInputs
                .map { it.id }
                .toTypedArray(),
                result
                    .orNull()
                    ?.map { it.id }
                    ?.toTypedArray())
        }

    @Test
    fun `should read existing input`() =
        runTest {
            val expectedInput = DummyInput().apply { id = 1234 }
            coEvery { inputLocalDataSource.readInput(expectedInput.id) } returns expectedInput

            // when reading existing input from repository
            val result = inputRepository.readInput(expectedInput.id)

            // then
            assertTrue(result.isRight)
            assertEquals(
                expectedInput,
                result.orNull()
            )
        }

    @Test
    fun `should return NotFoundFailure if trying to read undefined input`() =
        runTest {
            coEvery { inputLocalDataSource.readInput(any()) } answers { throw InputException.NotFoundException(firstArg()) }

            // when reading a non existing input from repository
            val result = inputRepository.readInput(1234)

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.NotFoundFailure)
        }

    @Test
    fun `should return IOFailure if failed to read existing input`() =
        runTest {
            coEvery { inputLocalDataSource.readInput(any()) } answers { throw InputException.ReadException(firstArg()) }

            // when reading a non existing input from repository
            val result = inputRepository.readInput(1234)

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.IOFailure)
        }

    @Test
    fun `should save input`() =
        runTest {
            coEvery { inputLocalDataSource.saveInput(any()) } answers { firstArg() }

            // when saving input
            val inputToSave = DummyInput().apply { id = 1234 }
            val result = inputRepository.saveInput(inputToSave)

            // then
            assertTrue(result.isRight)
            coVerify { inputLocalDataSource.saveInput(inputToSave) }
            assertEquals(
                inputToSave,
                result.orNull()
            )
        }

    @Test
    fun `should return IOFailure if failed to save input`() =
        runTest {
            coEvery { inputLocalDataSource.saveInput(any()) } answers { throw InputException.ReadException(firstArg<DummyInput>().id) }

            // when saving input
            val result = inputRepository.saveInput(DummyInput().apply { id = 1234 })

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.IOFailure)
        }

    @Test
    fun `should delete existing input`() =
        runTest {
            coEvery { inputLocalDataSource.deleteInput(any()) } just Runs

            // when deleting existing input from repository
            val result = inputRepository.deleteInput(1234)

            // then
            assertTrue(result.isRight)
            coVerify { inputLocalDataSource.deleteInput(1234) }
        }

    @Test
    fun `should return IOFailure if failed to delete input`() =
        runTest {
            coEvery { inputLocalDataSource.deleteInput(any()) } answers { throw InputException.WriteException(firstArg()) }

            // when saving input
            val result = inputRepository.deleteInput(1234)

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.IOFailure)
        }

    @Test
    fun `should export existing input from given ID`() =
        runTest {
            val exportedInput = DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.TO_SYNC
            }
            coEvery { inputLocalDataSource.exportInput(any<Long>()) } answers { exportedInput }

            // when exporting input
            val inputToExport = DummyInput().apply { id = 1234 }
            val result = inputRepository.exportInput(inputToExport.id)

            // then
            assertTrue(result.isRight)
            coVerify { inputLocalDataSource.exportInput(inputToExport.id) }
            assertEquals(
                exportedInput,
                result.orNull()
            )
        }

    @Test
    fun `should export existing input`() =
        runTest {
            coEvery { inputLocalDataSource.exportInput(any<DummyInput>()) } answers {
                firstArg<DummyInput>().apply { status = AbstractInput.Status.TO_SYNC }
            }

            // when exporting input
            val inputToExport = DummyInput().apply { id = 1234 }
            val result = inputRepository.exportInput(inputToExport)

            // then
            assertTrue(result.isRight)
            coVerify { inputLocalDataSource.exportInput(inputToExport) }
            assertEquals(
                inputToExport.apply { status = AbstractInput.Status.TO_SYNC },
                result.orNull()
            )
        }

    @Test
    fun `should return NotFoundFailure if trying to export undefined input`() =
        runTest {
            coEvery { inputLocalDataSource.exportInput(any<Long>()) } answers { throw InputException.NotFoundException(firstArg()) }

            // when exporting input
            val result = inputRepository.exportInput(1234)

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.NotFoundFailure)
        }
}