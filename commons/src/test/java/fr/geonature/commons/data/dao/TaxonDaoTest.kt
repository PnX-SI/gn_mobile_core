package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.data.onCreateTaxaFtsCallback
import fr.geonature.commons.getPrivateProperty
import fr.geonature.commons.util.toDate
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertArrayEquals
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
            .addCallback(onCreateTaxaFtsCallback)
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
    fun `should find all taxa IDs`() =
        runTest {
            initializeTaxonomy()
            val expectedTaxa = initializeTaxa()

            val ids = taxonDao.findAllIds()

            assertEquals(
                expectedTaxa.map { it.id },
                ids
            )
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

    @Test
    fun `should create query to find taxa by name or description from simple query string`() = runTest {
        val query = taxonDao
            .QB()
            .whereNameOrDescription("frelon d'")
            .supportSQLiteQuery()

        assertEquals(
            """
            SELECT taxa."_id" AS taxa__id, taxa."name" AS taxa_name, taxa."kingdom" AS taxa_kingdom, taxa."group" AS taxa_group, taxa."name_common" AS taxa_name_common, taxa."description" AS taxa_description
            FROM taxa taxa
            WHERE ((taxa_name GLOB ? OR taxa_name_common GLOB ? OR taxa_description GLOB ?))
            """.trimIndent(),
            query.sql
        )
        assertArrayEquals(
            arrayOf(
                "*[fF][rR][eéèëêẽEÉÈËÊẼ][lL][oóòöôõõOÓÒÖÔÕ][nñNÑ] [dD]['']*",
                "*[fF][rR][eéèëêẽEÉÈËÊẼ][lL][oóòöôõõOÓÒÖÔÕ][nñNÑ] [dD]['']*",
                "*[fF][rR][eéèëêẽEÉÈËÊẼ][lL][oóòöôõõOÓÒÖÔÕ][nñNÑ] [dD]['']*"
            ),
            (query as SimpleSQLiteQuery).getPrivateProperty<SimpleSQLiteQuery, Array<out Any?>>("bindArgs")
        )
    }

    @Test
    fun `should build filter by name or description from normalized query string`() {
        val query = taxonDao
            .QB()
            .whereNameOrDescription("âne")
            .supportSQLiteQuery()

        assertEquals(
            """
            SELECT taxa."_id" AS taxa__id, taxa."name" AS taxa_name, taxa."kingdom" AS taxa_kingdom, taxa."group" AS taxa_group, taxa."name_common" AS taxa_name_common, taxa."description" AS taxa_description
            FROM taxa taxa
            WHERE ((taxa_name GLOB ? OR taxa_name_common GLOB ? OR taxa_description GLOB ?))
            """.trimIndent(),
            query.sql
        )
        assertArrayEquals(
            arrayOf(
                "*[aáàäâãAÁÀÄÂÃ][nñNÑ][eéèëêẽEÉÈËÊẼ]*",
                "*[aáàäâãAÁÀÄÂÃ][nñNÑ][eéèëêẽEÉÈËÊẼ]*",
                "*[aáàäâãAÁÀÄÂÃ][nñNÑ][eéèëêẽEÉÈËÊẼ]*"
            ),
            (query as SimpleSQLiteQuery).getPrivateProperty<SimpleSQLiteQuery, Array<out Any?>>("bindArgs")
        )
    }

    @Test
    fun `should create query to find taxa using simple full-text search`() =
        runTest {
            val query = taxonDao
                .QB()
                .whereNameOrDescriptionMatch("ab")
                .supportSQLiteQuery()

            assertEquals(
                """
                SELECT taxa."_id" AS taxa__id, taxa."name" AS taxa_name, taxa."kingdom" AS taxa_kingdom, taxa."group" AS taxa_group, taxa."name_common" AS taxa_name_common, taxa."description" AS taxa_description
                FROM taxa taxa
                JOIN taxa_fts AS taxa_fts ON taxa_fts."_id" = taxa__id
                WHERE (taxa_fts MATCH ?)
                """.trimIndent(),
                query.sql
            )
            assertArrayEquals(
                arrayOf("*ab*"),
                (query as SimpleSQLiteQuery).getPrivateProperty<SimpleSQLiteQuery, Array<out Any?>>("bindArgs")
            )
        }

    @Test
    fun `should create query to find taxa using full-text multi words search`() =
        runTest {
            val query = taxonDao
                .QB()
                .whereNameOrDescriptionMatch("ab cd")
                .supportSQLiteQuery()

            assertEquals(
                """
                SELECT taxa."_id" AS taxa__id, taxa."name" AS taxa_name, taxa."kingdom" AS taxa_kingdom, taxa."group" AS taxa_group, taxa."name_common" AS taxa_name_common, taxa."description" AS taxa_description
                FROM taxa taxa
                JOIN taxa_fts AS taxa_fts ON taxa_fts."_id" = taxa__id
                WHERE (taxa_fts MATCH ?)
                """.trimIndent(),
                query.sql
            )
            assertArrayEquals(
                arrayOf("*ab* *cd*"),
                (query as SimpleSQLiteQuery).getPrivateProperty<SimpleSQLiteQuery, Array<out Any?>>("bindArgs")
            )
        }

    @Test
    fun `should create query to find taxa using full-text complex search`() =
        runTest {
            val query = taxonDao
                .QB()
                .whereNameOrDescriptionMatch("^ab* NOT cd")
                .supportSQLiteQuery()

            assertEquals(
                """
                SELECT taxa."_id" AS taxa__id, taxa."name" AS taxa_name, taxa."kingdom" AS taxa_kingdom, taxa."group" AS taxa_group, taxa."name_common" AS taxa_name_common, taxa."description" AS taxa_description
                FROM taxa taxa
                JOIN taxa_fts AS taxa_fts ON taxa_fts."_id" = taxa__id
                WHERE (taxa_fts MATCH ?)
                """.trimIndent(),
                query.sql
            )
            assertArrayEquals(
                arrayOf("^ab* NOT cd"),
                (query as SimpleSQLiteQuery).getPrivateProperty<SimpleSQLiteQuery, Array<out Any?>>("bindArgs")
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