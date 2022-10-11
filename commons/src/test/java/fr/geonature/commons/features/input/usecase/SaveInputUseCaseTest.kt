package fr.geonature.commons.features.input.usecase

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.features.input.error.InputFailure
import fr.geonature.commons.features.input.repository.IInputRepository
import fr.geonature.commons.fp.Either
import fr.geonature.commons.fp.identity
import fr.geonature.commons.fp.orNull
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import fr.geonature.commons.settings.DummyAppSettings
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [SaveInputUseCase].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class SaveInputUseCaseTest {

    @get:Rule
    val rule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var inputRepository: IInputRepository<DummyInput, DummyAppSettings>

    private lateinit var saveInputUseCase: SaveInputUseCase<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        saveInputUseCase = SaveInputUseCase(inputRepository)
    }

    @Test
    fun `should save input`() =
        runTest {
            coEvery { inputRepository.saveInput(any()) } answers { Either.Right(firstArg()) }

            // when saving input
            val inputToSave = DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.TO_SYNC
            }
            val result = saveInputUseCase.run(SaveInputUseCase.Params(inputToSave))

            // then
            assertTrue(result.isRight)
            assertEquals(
                inputToSave.apply { status = AbstractInput.Status.DRAFT },
                result.orNull()
            )
        }

    @Test
    fun `should return IOFailure if failed to save input`() =
        runTest {
            coEvery { inputRepository.saveInput(any()) } answers { Either.Left(InputFailure.IOFailure("I/O error while writing input with ID '${firstArg<DummyInput>().id}'")) }

            // when saving input
            val inputToSave = DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.TO_SYNC
            }
            val result = saveInputUseCase.run(SaveInputUseCase.Params(inputToSave))

            // then
            assertTrue(result.isLeft)
            assertTrue(result.fold(::identity) {} is InputFailure.IOFailure)
        }
}