package fr.geonature.commons.features.dataset.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.features.dataset.error.DatasetException
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [IDatasetLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class DatasetLocalDataSourceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var datasetDao: DatasetDao
    private lateinit var datasetLocalDataSource: IDatasetLocalDataSource

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(
                context,
                LocalDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        datasetDao = db.datasetDao()

        datasetLocalDataSource = DatasetLocalDataSourceImpl(datasetDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should find dataset matching given ID`() =
        runTest {
            val expectedDataset = initializeDataset()

            val dataset = datasetLocalDataSource.findDatasetById(17L)

            assertEquals(
                expectedDataset.first { it.id == 17L },
                dataset
            )
        }

    @Test
    fun `should throw NoDatasetFoundException if no dataset was found from given ID`() =
        runTest {
            initializeDataset()

            val exception =
                runCatching { datasetLocalDataSource.findDatasetById(8L) }.exceptionOrNull()

            assertTrue(exception is DatasetException.NoDatasetFoundException)
            assertEquals(
                8L,
                (exception as DatasetException.NoDatasetFoundException).id
            )
        }

    private fun initializeDataset(): List<Dataset> {
        return listOf(
            Dataset(
                id = 1,
                module = "occtax",
                name = "Contact aléatoire tous règnes confondus",
                description = "Observations aléatoires de la faune, de la flore ou de la fonge",
                active = true,
                createdAt = Date.from(Instant.parse("2016-10-28T08:15:00Z")),
                100
            ),
            Dataset(
                id = 17,
                module = "occtax",
                name = "Jeu de données personnel de Auger Ariane",
                description = "Jeu de données personnel de Auger Ariane",
                active = true,
                createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                100
            ),
            Dataset(
                id = 30,
                module = "occtax",
                name = "Observation opportuniste aléatoire tout règne confondu",
                description = "Observation opportuniste aléatoire tout règne confondu",
                active = true,
                createdAt = Date.from(Instant.parse("2022-11-19T12:00:00Z")),
                100
            )
        ).also {
            datasetDao.insert(*it.toTypedArray())
        }
    }
}