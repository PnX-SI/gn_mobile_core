package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.NomenclatureType
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
 * Unit tests about [NomenclatureTypeDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NomenclatureTypeDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var nomenclatureTypeDao: NomenclatureTypeDao

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
        nomenclatureTypeDao = db.nomenclatureTypeDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should insert and find nomenclature types`() =
        runTest {
            val expectedNomenclatureTypes = initializeNomenclatureTypes()
            val nomenclatureTypesFromDb = nomenclatureTypeDao.findAll()

            assertEquals(
                expectedNomenclatureTypes.sortedBy { it.defaultLabel },
                nomenclatureTypesFromDb
            )
        }

    private fun initializeNomenclatureTypes(): List<NomenclatureType> {
        val expectedNomenclatureTypes = listOf(
            NomenclatureType(
                id = 7,
                mnemonic = "ETA_BIO",
                defaultLabel = "Etat biologique de l'observation"
            ),
            NomenclatureType(
                id = 13,
                mnemonic = "STATUT_BIO",
                defaultLabel = "Statut biologique"
            ),
            NomenclatureType(
                id = 14,
                mnemonic = "METH_OBS",
                defaultLabel = "Méthodes d'observation"
            )
        )

        nomenclatureTypeDao.insert(*expectedNomenclatureTypes.toTypedArray())

        return expectedNomenclatureTypes
    }
}