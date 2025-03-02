package fr.geonature.commons.features.inputObservers.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.features.inputObservers.error.InputObserverException
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

/**
 * Unit tests about [IInputObserverLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class InputObserverLocalDataSourceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var inputObserverDao: InputObserverDao
    private lateinit var inputObserverLocalDataSource: IInputObserverLocalDataSource

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
        inputObserverDao = db.inputObserverDao()

        inputObserverLocalDataSource = InputObserverLocalDataSourceImpl(inputObserverDao)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should find all input observers matching given IDs`() =
        runTest {
            val expectedInputObservers = initializeInputObservers()
            val inputObservers = inputObserverLocalDataSource.findInputObserversByIds(
                2L,
                4L
            )

            assertEquals(expectedInputObservers.filter {
                longArrayOf(
                    2L,
                    4L
                ).any { id -> it.id == id }
            },
                inputObservers.sortedBy { it.id })
        }

    @Test
    fun `should throw NoInputObserversFoundException if no input observers was found from given IDs`() =
        runTest {
            initializeInputObservers()

            val exception =
                runCatching { inputObserverLocalDataSource.findInputObserversByIds(8L) }.exceptionOrNull()

            assertTrue(exception is InputObserverException.NoInputObserversFoundException)
            assertEquals(
                listOf(8L),
                (exception as InputObserverException.NoInputObserversFoundException).id
            )
        }

    private fun initializeInputObservers(): List<InputObserver> {
        return listOf(
            InputObserver(
                id = 1L,
                lastname = "Craig",
                firstname = "Daisy"
            ),
            InputObserver(
                id = 2L,
                lastname = "Li",
                firstname = "Andy"
            ),
            InputObserver(
                id = 3L,
                lastname = "Abbott",
                firstname = "Bruno"
            ),
            InputObserver(
                id = 4L,
                lastname = "Jenkins",
                firstname = "Noor"
            )
        ).also {
            inputObserverDao.insert(*it.toTypedArray())
        }
    }
}