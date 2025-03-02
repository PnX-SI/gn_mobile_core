package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.InputObserver
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Unit tests about [InputObserverDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class InputObserverDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var inputObserverDao: InputObserverDao

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
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should insert and find all input observers matching given IDs`() =
        runTest {
            val expectedInputObservers = initializeInputObservers()

            val inputObserversFromDb = inputObserverDao.findByIds(
                2L,
                4L
            )

            assertEquals(
                expectedInputObservers.filter {
                    longArrayOf(
                        2L,
                        4L
                    ).any { id -> it.id == id }
                },
                inputObserversFromDb.sortedBy { it.id }
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