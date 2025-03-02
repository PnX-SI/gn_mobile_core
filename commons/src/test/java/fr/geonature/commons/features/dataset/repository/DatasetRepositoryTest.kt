package fr.geonature.commons.features.dataset.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.data.IDatasetLocalDataSource
import fr.geonature.commons.features.dataset.error.DatasetException
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
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [IDatasetRepository].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
class DatasetRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var datasetLocalDataSource: IDatasetLocalDataSource

    private lateinit var datasetRepository: IDatasetRepository

    @Before
    fun setUp() {
        init(this)

        datasetRepository = DatasetRepositoryImpl(datasetLocalDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should find dataset matching given ID`() =
        runTest {
            coEvery {
                datasetLocalDataSource.findDatasetById(7L)
            } returns Dataset(
                id = 7,
                name = "Ablettes du PNE",
                description = "Observations d'ablettes par le PNE",
                active = true,
                createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                null,
                100
            )

            // when
            val result = datasetRepository.getDatasetById(7L)

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                Dataset(
                    id = 7,
                    name = "Ablettes du PNE",
                    description = "Observations d'ablettes par le PNE",
                    active = true,
                    createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                    null,
                    100
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should return NoDatasetFoundException if no dataset was found`() =
        runTest {
            coEvery {
                datasetLocalDataSource.findDatasetById(8L)
            } answers { throw DatasetException.NoDatasetFoundException(8L) }

            // when
            val result = datasetRepository.getDatasetById(8L)

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is DatasetException.NoDatasetFoundException)
        }
}