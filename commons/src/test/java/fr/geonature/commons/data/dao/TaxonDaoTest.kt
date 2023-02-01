package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.toDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Unit tests about [NomenclatureDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TaxonDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var taxonomyDao: TaxonomyDao
    private lateinit var taxonDao: TaxonDao
    private lateinit var taxonAreaDao: TaxonAreaDao

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
        taxonomyDao = db.taxonomyDao()
        taxonDao = db.taxonDao()
        taxonAreaDao = db.taxonAreaDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should insert and find taxon matching given ID`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()

            val taxonFromDb = taxonDao.findById(84L)

            assertEquals(
                expectedTaxa.first { it.id == 84L },
                taxonFromDb
            )
        }

    @Test
    fun `should return null if no taxon was found from given ID`() =
        runTest {
            initializeTaxonomy()
            initializeTaxa()

            val noSuchTaxonFromDb = taxonDao.findById(123L)

            assertNull(noSuchTaxonFromDb)
        }

    @Test
    fun `should insert and find taxa matching given IDs`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()

            val taxaFromDb = taxonDao.findByIds(
                84L,
                324L,
                8L
            )

            assertEquals(
                expectedTaxa,
                taxaFromDb
            )
        }

    @Test
    fun `should insert and find taxon matching given area`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()
            val expectedTaxaWithArea = initializeTaxaArea()

            val taxonWithAreaFromDb = taxonDao.findByIdMatchingArea(
                taxonId = 84L,
                areaId = 123L
            )

            assertEquals(
                mapOf(expectedTaxa.first { it.id == 84L } to expectedTaxaWithArea.first { it.taxonId == 84L && it.areaId == 123L }),
                taxonWithAreaFromDb,
            )
        }

    @Test
    fun `should insert and find taxon with no area matching given area`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()
            initializeTaxaArea()

            val taxonWithNoAreaFromDb = taxonDao.findByIdMatchingArea(
                taxonId = 84L,
                areaId = 130L
            )

            assertEquals(
                mapOf(expectedTaxa.first { it.id == 84L } to null),
                taxonWithNoAreaFromDb,
            )
        }

    @Test
    fun `should return an empty map if no taxon was found from given ID`() =
        runTest {
            initializeTaxonomy()
            initializeTaxa()
            initializeTaxaArea()

            val noSuchTaxonFromDb = taxonDao.findByIdMatchingArea(
                taxonId = 8L,
                areaId = 123L
            )

            assertEquals(
                emptyMap<Taxon, TaxonArea>(),
                noSuchTaxonFromDb,
            )
        }

    @Test
    fun `should delete an existing taxon from given ID`() =
        runTest {
            initializeTaxonomy()
            initializeTaxa()
            initializeTaxaArea()

            taxonDao.deleteById(84L)
            val noSuchTaxonFromDb = taxonDao.findById(84L)

            assertNull(noSuchTaxonFromDb)
        }

    private fun initializeTaxonomy(): List<Taxonomy> {
        val expectedTaxonomy = listOf(
            Taxonomy(kingdom = Taxonomy.ANY),
            Taxonomy(kingdom = "Animalia"),
            Taxonomy(
                kingdom = "Animalia",
                group = "Amphibiens"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Mammifères"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Oiseaux"
            ),
            Taxonomy(
                kingdom = "Animalia",
                group = "Reptiles"
            ),
            Taxonomy(kingdom = "Fungi"),
            Taxonomy(kingdom = "Plantae")
        )

        taxonomyDao.insert(*expectedTaxonomy.toTypedArray())

        return expectedTaxonomy
    }

    private fun initializeTaxa(): List<Taxon> {
        val expectedTaxa = listOf(
            Taxon(
                84L,
                "Salamandra fusca",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Amphibiens"
                ),
                null,
                "Salamandra atra atra (Laurenti, 1768)",
                "ES - 84"
            ),
            Taxon(
                324L,
                "Rana alpina",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Amphibiens"
                ),
                "Grenouille rousse (La)",
                "Rana temporaria Linnaeus, 1758",
                "ES - 324"
            )
        )

        taxonDao.insert(*expectedTaxa.toTypedArray())

        return expectedTaxa
    }

    private fun initializeTaxaArea(): List<TaxonArea> {
        val expectedTaxaArea = listOf(
            TaxonArea(
                84L,
                123L,
                "FF0000",
                3,
                toDate("2016-10-28T08:15:00Z")
            ),
            TaxonArea(
                324L,
                130L,
                "FFFF00",
                1,
                toDate("2016-10-29T08:15:00Z")
            )
        )

        taxonAreaDao.insert(*expectedTaxaArea.toTypedArray())

        return expectedTaxaArea
    }
}