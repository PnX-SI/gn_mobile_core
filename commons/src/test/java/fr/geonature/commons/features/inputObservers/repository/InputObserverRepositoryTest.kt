package fr.geonature.commons.features.inputObservers.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.data.IInputObserverLocalDataSource
import fr.geonature.commons.features.inputObservers.error.InputObserverException
import fr.geonature.commons.features.nomenclature.repository.INomenclatureRepository
import io.mockk.MockKAnnotations.init
import io.mockk.coEvery
import io.mockk.impl.annotations.MockK
import io.mockk.unmockkAll
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit tests about [INomenclatureRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class InputObserverRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var inputObserverLocalDataSource: IInputObserverLocalDataSource

    private lateinit var inputObserverRepository: IInputObserverRepository

    @Before
    fun setUp() {
        init(this)

        inputObserverRepository = InputObserverRepositoryImpl(inputObserverLocalDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should find all input observers matching given IDs`() =
        runTest {
            coEvery {
                inputObserverLocalDataSource.findInputObserversByIds(
                    2L,
                    4L
                )
            } returns listOf(
                InputObserver(
                    id = 2L,
                    lastname = "Li",
                    firstname = "Andy"
                ),
                InputObserver(
                    id = 4L,
                    lastname = "Jenkins",
                    firstname = "Noor"
                )
            )

            // when
            val result = inputObserverRepository.findInputObserversByIds(
                2L,
                4L
            )

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                listOf(
                    InputObserver(
                        id = 2L,
                        lastname = "Li",
                        firstname = "Andy"
                    ),
                    InputObserver(
                        id = 4L,
                        lastname = "Jenkins",
                        firstname = "Noor"
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should return NoInputObserversFoundException if no input observers was found`() =
        runTest {
            coEvery {
                inputObserverLocalDataSource.findInputObserversByIds(8L)
            } answers { throw InputObserverException.NoInputObserversFoundException(listOf(8L)) }

            // when
            val result = inputObserverRepository.findInputObserversByIds(8L)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is InputObserverException.NoInputObserversFoundException)
        }
}