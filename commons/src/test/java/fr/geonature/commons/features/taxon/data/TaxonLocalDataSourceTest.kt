package fr.geonature.commons.features.taxon.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.TaxonAreaDao
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.TaxonWithArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.taxon.error.TaxonException
import fr.geonature.commons.util.toDate
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
 * Unit tests about [ITaxonLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class TaxonLocalDataSourceTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var taxonomyDao: TaxonomyDao
    private lateinit var taxonDao: TaxonDao
    private lateinit var taxonAreaDao: TaxonAreaDao
    private lateinit var taxonLocalDataSource: ITaxonLocalDataSource

    @Before
    fun setUp() {
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
        taxonLocalDataSource = TaxonLocalDataSourceImpl(taxonDao)
    }

    @After
    @Throws(IOException::class)
    fun tearDown() {
        db.close()
    }

    @Test
    fun `should find taxon matching given ID`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()

            val taxon = taxonLocalDataSource.findTaxonById(84L)

            assertEquals(
                expectedTaxa.first { it.id == 84L },
                taxon
            )
        }

    @Test
    fun `should throw NoTaxonFoundException if no taxon was found from given ID`() =
        runTest {
            initializeTaxonomy()
            initializeTaxa()

            val exception = runCatching { taxonLocalDataSource.findTaxonById(8L) }.exceptionOrNull()

            assertTrue(exception is TaxonException.NoTaxonFoundException)
            assertEquals(
                8L,
                (exception as TaxonException.NoTaxonFoundException).id
            )
        }

    @Test
    fun `should find taxa matching given IDs`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()

            val taxa = taxonLocalDataSource.findTaxaByIds(
                84L,
                324L,
                8L
            )

            assertEquals(
                expectedTaxa,
                taxa
            )
        }

    @Test
    fun `should find taxon matching given area`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()
            val expectedTaxaWithArea = initializeTaxaArea()

            val taxonWithArea = taxonLocalDataSource.findTaxonByIdWithArea(
                84L,
                123L
            )

            assertEquals(
                TaxonWithArea(expectedTaxa.first { it.id == 84L }).apply {
                    taxonArea =
                        expectedTaxaWithArea.first { it.taxonId == 84L && it.areaId == 123L }
                },
                taxonWithArea,
            )
        }

    @Test
    fun `should throw NoTaxonFoundException if no taxon was found from given ID for given area`() =
        runTest {
            initializeTaxonomy()
            initializeTaxa()

            val exception = runCatching {
                taxonLocalDataSource.findTaxonByIdWithArea(
                    8L,
                    123L
                )
            }.exceptionOrNull()

            assertTrue(exception is TaxonException.NoTaxonFoundException)
            assertEquals(
                8L,
                (exception as TaxonException.NoTaxonFoundException).id
            )
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
                "Salamandra atra atra (Laurenti, 1768)"
            ),
            Taxon(
                324L,
                "Rana alpina",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Amphibiens"
                ),
                "Grenouille rousse (La)",
                "Rana temporaria Linnaeus, 1758"
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
                324,
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