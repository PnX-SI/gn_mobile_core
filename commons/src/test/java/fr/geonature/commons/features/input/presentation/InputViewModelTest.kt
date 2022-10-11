package fr.geonature.commons.features.input.presentation

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.error.Failure
import fr.geonature.commons.features.input.domain.AbstractInput
import fr.geonature.commons.features.input.domain.DummyInput
import fr.geonature.commons.features.input.error.InputFailure
import fr.geonature.commons.features.input.usecase.DeleteInputUseCase
import fr.geonature.commons.features.input.usecase.ExportInputUseCase
import fr.geonature.commons.features.input.usecase.ReadInputsUseCase
import fr.geonature.commons.features.input.usecase.SaveInputUseCase
import fr.geonature.commons.fp.Either
import fr.geonature.commons.settings.DummyAppSettings
import io.mockk.MockKAnnotations.init
import io.mockk.clearAllMocks
import io.mockk.coEvery
import io.mockk.confirmVerified
import io.mockk.every
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import io.mockk.verifyOrder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [InputViewModel].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class InputViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @RelaxedMockK
    private lateinit var readInputsUseCase: ReadInputsUseCase<DummyInput, DummyAppSettings>

    @RelaxedMockK
    private lateinit var saveInputUseCase: SaveInputUseCase<DummyInput, DummyAppSettings>

    @RelaxedMockK
    private lateinit var deleteInputUseCase: DeleteInputUseCase<DummyInput, DummyAppSettings>

    @RelaxedMockK
    private lateinit var exportInputUseCase: ExportInputUseCase<DummyInput, DummyAppSettings>

    @RelaxedMockK
    private lateinit var failureObserver: Observer<Failure>

    @RelaxedMockK
    private lateinit var inputsObserver: Observer<List<DummyInput>>

    @RelaxedMockK
    private lateinit var inputObserver: Observer<DummyInput>

    private lateinit var inputViewModel: InputViewModel<DummyInput, DummyAppSettings>

    @Before
    fun setUp() {
        init(this)

        inputViewModel = InputViewModel(
            readInputsUseCase,
            saveInputUseCase,
            deleteInputUseCase,
            exportInputUseCase
        )

        inputViewModel.failure.observeForever(failureObserver)
        inputViewModel.inputs.observeForever(inputsObserver)
        inputViewModel.input.observeForever(inputObserver)

        every {
            readInputsUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            saveInputUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            deleteInputUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
        every {
            exportInputUseCase(
                any(),
                any(),
                any()
            )
        } answers {
            callOriginal()
        }
    }

    @After
    fun tearDown() {
        clearAllMocks()
    }

    @Test
    fun `should return an empty list when reading undefined inputs`() =
        runTest {
            // given an empty list from use case
            val expectedInputs = listOf<DummyInput>()
            coEvery {
                readInputsUseCase.run(any())
            } returns Either.Right(expectedInputs)

            // when reading non existing inputs
            inputViewModel.readInputs()
            advanceUntilIdle()

            // then
            verify(atLeast = 1) { inputsObserver.onChanged(expectedInputs) }
            confirmVerified(inputsObserver)
        }

    @Test
    fun `should read existing inputs`() =
        runTest {
            // given some inputs from use case
            val expectedInputs = listOf(
                DummyInput().apply { id = 1234 },
                DummyInput().apply { id = 1235 },
                DummyInput().apply { id = 1236 },
            )
            coEvery {
                readInputsUseCase.run(any())
            } returns Either.Right(expectedInputs)

            // when reading these inputs from repository
            inputViewModel.readInputs()
            advanceUntilIdle()

            // then
            verify(atLeast = 1) { inputsObserver.onChanged(expectedInputs) }
            confirmVerified(inputsObserver)
        }

    @Test
    fun `should save input`() =
        runTest {
            // given some input to save
            val inputToSave = DummyInput().apply {
                id = 1234
                status = AbstractInput.Status.TO_SYNC
            }
            coEvery {
                saveInputUseCase.run(any())
            } returns Either.Right(inputToSave.apply { status = AbstractInput.Status.DRAFT })

            // when saving input
            inputViewModel.saveInput(inputToSave)
            advanceUntilIdle()

            // then
            verify(atLeast = 1) {
                inputObserver.onChanged(inputToSave.apply {
                    status = AbstractInput.Status.DRAFT
                })
            }
            verify(atLeast = 1) {
                inputsObserver.onChanged(listOf(inputToSave.apply {
                    status = AbstractInput.Status.DRAFT
                }))
            }
            confirmVerified(
                inputObserver,
                inputsObserver
            )
        }

    @Test
    fun `should delete existing input`() =
        runTest {
            // given some existing inputs
            val expectedInputs = listOf(
                DummyInput().apply { id = 1234 },
                DummyInput().apply { id = 1235 },
                DummyInput().apply { id = 1236 },
            )
            coEvery {
                readInputsUseCase.run(any())
            } returns Either.Right(expectedInputs)
            coEvery {
                deleteInputUseCase.run(any())
            } returns Either.Right(Unit)

            // when reading these inputs from repository
            inputViewModel.readInputs()
            advanceUntilIdle()

            // and deleting given input
            inputViewModel.deleteInput(expectedInputs.first { it.id == 1235L })
            advanceUntilIdle()

            // then
            verifyOrder {
                inputsObserver.onChanged(expectedInputs)
                inputsObserver.onChanged(expectedInputs.filter { it.id != 1235L })
            }
            confirmVerified(inputsObserver)
        }

    @Test
    fun `should export existing input`() =
        runTest {
            // given some input to export
            val inputToExport = DummyInput().apply { id = 1234 }
            coEvery {
                exportInputUseCase.run(any())
            } returns Either.Right(inputToExport.apply { status = AbstractInput.Status.TO_SYNC })

            var exported = false
            // when exporting input
            inputViewModel.exportInput(inputToExport) { exported = true }
            advanceUntilIdle()

            // then
            assertTrue(exported)
            verify(atLeast = 1) {
                inputObserver.onChanged(inputToExport.apply {
                    status = AbstractInput.Status.TO_SYNC
                })
            }
            verify(atLeast = 1) {
                inputsObserver.onChanged(listOf(inputToExport.apply {
                    status = AbstractInput.Status.TO_SYNC
                }))
            }
            confirmVerified(
                inputObserver,
                inputsObserver
            )
        }

    @Test
    fun `should return NotFoundFailure if trying to export undefined input`() =
        runTest {
            // given a non existing input
            val inputToExport = DummyInput().apply { id = 1234 }
            coEvery {
                exportInputUseCase.run(any())
            } returns Either.Left(InputFailure.NotFoundFailure("no input found with ID '${inputToExport.id}'"))

            // when exporting input
            inputViewModel.exportInput(inputToExport)
            advanceUntilIdle()

            // then
            verify(atLeast = 1) {
                failureObserver.onChanged(InputFailure.NotFoundFailure("no input found with ID '${inputToExport.id}'"))
            }
            verify(inverse = true) { inputObserver.onChanged(any()) }
            verify(inverse = true) { inputsObserver.onChanged(any()) }
            confirmVerified(
                failureObserver,
                inputObserver,
                inputsObserver
            )
        }
}