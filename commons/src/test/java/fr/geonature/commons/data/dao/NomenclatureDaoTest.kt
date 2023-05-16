package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxonomy
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
 * Unit tests about [NomenclatureDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class NomenclatureDaoTest {
    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var taxonomyDao: TaxonomyDao
    private lateinit var nomenclatureTypeDao: NomenclatureTypeDao
    private lateinit var nomenclatureTaxonomyDao: NomenclatureTaxonomyDao
    private lateinit var nomenclatureDao: NomenclatureDao
    private lateinit var defaultNomenclatureDao: DefaultNomenclatureDao

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
        nomenclatureTypeDao = db.nomenclatureTypeDao()
        nomenclatureTaxonomyDao = db.nomenclatureTaxonomyDao()
        nomenclatureDao = db.nomenclatureDao()
        defaultNomenclatureDao = db.defaultNomenclatureDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should insert and find all default nomenclature values`() =
        runTest {
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedDefaultNomenclatureValues = initializeDefaultNomenclatureValues()

            val nomenclatureTypesWithDefaultValuesFromDb =
                nomenclatureDao.findAllDefaultNomenclatureValues("occtax")

            assertEquals(
                expectedNomenclatures.filter { expectedDefaultNomenclatureValues.any { defaultNomenclature -> defaultNomenclature.nomenclatureId == it.id } },
                nomenclatureTypesWithDefaultValuesFromDb
            )
        }

    @Test
    fun `should insert and find nomenclatures by type`() =
        runTest {
            val expectedNomenclatureTypes = initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()

            val nomenclaturesForEtaBio = nomenclatureDao.findAllByNomenclatureType("ETA_BIO")

            assertEquals(expectedNomenclatures
                .filter { it.typeId == expectedNomenclatureTypes.find { nomenclatureType -> nomenclatureType.mnemonic == "ETA_BIO" }?.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForEtaBio
            )
        }

    @Test
    fun `should return empty list if no nomenclature was found from given type`() =
        runTest {
            initializeNomenclatureTypes()
            initializeNomenclaturesByTypes()

            val nomenclaturesForMethObs = nomenclatureDao.findAllByNomenclatureType("METH_OBS")

            assertTrue(nomenclaturesForMethObs.isEmpty())
        }

    @Test
    fun `should insert and find nomenclatures by type with no taxonomy`() =
        runTest {
            initializeTaxonomy()
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

            val nomenclaturesForStatutBioAndNoTaxonomy =
                nomenclatureDao.findAllByNomenclatureTypeAndByTaxonomy(mnemonic = "STATUT_BIO")

            assertEquals(expectedNomenclatureTaxonomy
                .filter { it.taxonomy == Taxonomy(kingdom = Taxonomy.ANY) }
                .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
                .distinctBy { it.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForStatutBioAndNoTaxonomy
            )
        }

    @Test
    fun `should insert and find nomenclatures by type with any kingdom taxonomy`() =
        runTest {
            initializeTaxonomy()
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

            val nomenclaturesForStatutBioAndAnyKingdomTaxonomy =
                nomenclatureDao.findAllByNomenclatureTypeAndByTaxonomy(
                    mnemonic = "STATUT_BIO",
                    kingdom = Taxonomy.ANY
                )

            assertEquals(expectedNomenclatureTaxonomy
                .filter { it.taxonomy == Taxonomy(kingdom = Taxonomy.ANY) }
                .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
                .distinctBy { it.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForStatutBioAndAnyKingdomTaxonomy
            )
        }

    @Test
    fun `should insert and find nomenclatures by type with any taxonomy`() =
        runTest {
            initializeTaxonomy()
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

            val nomenclaturesForStatutBioAndAnyTaxonomy =
                nomenclatureDao.findAllByNomenclatureTypeAndByTaxonomy(
                    mnemonic = "STATUT_BIO",
                    kingdom = Taxonomy.ANY,
                    group = Taxonomy.ANY
                )

            assertEquals(expectedNomenclatureTaxonomy
                .filter { it.taxonomy == Taxonomy(kingdom = Taxonomy.ANY) }
                .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
                .distinctBy { it.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForStatutBioAndAnyTaxonomy
            )
        }

    @Test
    fun `should insert and find nomenclatures by type matching given taxonomy kingdom`() =
        runTest {
            initializeTaxonomy()
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

            val nomenclaturesForStatutBioAndAnyTaxonomy =
                nomenclatureDao.findAllByNomenclatureTypeAndByTaxonomy(
                    mnemonic = "STATUT_BIO",
                    kingdom = "Animalia"
                )

            assertEquals(expectedNomenclatureTaxonomy
                .filter {
                    listOf(
                        Taxonomy(kingdom = Taxonomy.ANY),
                        Taxonomy(kingdom = "Animalia")
                    ).contains(it.taxonomy)
                }
                .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
                .distinctBy { it.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForStatutBioAndAnyTaxonomy
            )
        }

    @Test
    fun `should insert and find nomenclatures by type matching given taxonomy kingdom and group`() =
        runTest {
            initializeTaxonomy()
            initializeNomenclatureTypes()
            val expectedNomenclatures = initializeNomenclaturesByTypes()
            val expectedNomenclatureTaxonomy = initializeNomenclaturesTaxonomy()

            val nomenclaturesForStatutBioAndAnyTaxonomy =
                nomenclatureDao.findAllByNomenclatureTypeAndByTaxonomy(
                    mnemonic = "STATUT_BIO",
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )

            assertEquals(expectedNomenclatureTaxonomy
                .filter {
                    listOf(
                        Taxonomy(kingdom = Taxonomy.ANY),
                        Taxonomy(kingdom = "Animalia"),
                        Taxonomy(
                            kingdom = "Animalia",
                            group = "Oiseaux"
                        )
                    ).contains(it.taxonomy)
                }
                .mapNotNull { expectedNomenclatures.find { nomenclature -> nomenclature.id == it.nomenclatureId } }
                .distinctBy { it.id }
                .sortedBy { it.defaultLabel },
                nomenclaturesForStatutBioAndAnyTaxonomy
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

    private fun initializeNomenclaturesByTypes(): List<Nomenclature> {
        val expectedNomenclatures = listOf(
            Nomenclature(
                id = 29,
                code = "0",
                hierarchy = "013.000",
                defaultLabel = "Inconnu",
                typeId = 13
            ),
            Nomenclature(
                id = 30,
                code = "1",
                hierarchy = "013.001",
                defaultLabel = "Non renseigné",
                typeId = 13
            ),
            Nomenclature(
                id = 31,
                code = "2",
                hierarchy = "013.002",
                defaultLabel = "Non déterminé",
                typeId = 13
            ),
            Nomenclature(
                id = 32,
                code = "3",
                hierarchy = "013.003",
                defaultLabel = "Reproduction",
                typeId = 13
            ),
            Nomenclature(
                id = 33,
                code = "4",
                hierarchy = "013.004",
                defaultLabel = "Hibernation",
                typeId = 13
            ),
            Nomenclature(
                id = 34,
                code = "5",
                hierarchy = "013.005",
                defaultLabel = "Estivation",
                typeId = 13
            ),
            Nomenclature(
                id = 35,
                code = "9",
                hierarchy = "013.009",
                defaultLabel = "Pas de reproduction",
                typeId = 13
            ),
            Nomenclature(
                id = 36,
                code = "13",
                hierarchy = "013.013",
                defaultLabel = "Végétatif",
                typeId = 13
            ),
            Nomenclature(
                id = 157,
                code = "1",
                hierarchy = "007.001",
                defaultLabel = "Non renseigné",
                typeId = 7
            ),
            Nomenclature(
                id = 158,
                code = "2",
                hierarchy = "007.002",
                defaultLabel = "Observé vivant",
                typeId = 7
            ),
            Nomenclature(
                id = 159,
                code = "3",
                hierarchy = "007.003",
                defaultLabel = "Trouvé mort",
                typeId = 7
            ),
            Nomenclature(
                id = 160,
                code = "4",
                hierarchy = "007.004",
                defaultLabel = "Indice de présence",
                typeId = 7
            ),
            Nomenclature(
                id = 161,
                code = "5",
                hierarchy = "007.005",
                defaultLabel = "Issu d'élevage",
                typeId = 7
            )
        )

        nomenclatureDao.insert(*expectedNomenclatures.toTypedArray())

        return expectedNomenclatures
    }

    private fun initializeNomenclaturesTaxonomy(): List<NomenclatureTaxonomy> {
        val expectedNomenclatureTaxonomy = listOf(
            NomenclatureTaxonomy(
                nomenclatureId = 29,
                taxonomy = Taxonomy(kingdom = Taxonomy.ANY)
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 30,
                taxonomy = Taxonomy(kingdom = Taxonomy.ANY)
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 31,
                taxonomy = Taxonomy(kingdom = Taxonomy.ANY)
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(kingdom = "Animalia")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(kingdom = "Fungi")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 32,
                taxonomy = Taxonomy(kingdom = "Plantae")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Amphibiens"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Mammifères"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 33,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Reptiles"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 34,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Mammifères"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 34,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 34,
                taxonomy = Taxonomy(
                    kingdom = "Animalia",
                    group = "Reptiles"
                )
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 35,
                taxonomy = Taxonomy(kingdom = "Animalia")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 35,
                taxonomy = Taxonomy(kingdom = "Fungi")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 35,
                taxonomy = Taxonomy(kingdom = "Plantae")
            ),
            NomenclatureTaxonomy(
                nomenclatureId = 36,
                taxonomy = Taxonomy(kingdom = "Plantae")
            )
        )

        nomenclatureTaxonomyDao.insert(*expectedNomenclatureTaxonomy.toTypedArray())

        return expectedNomenclatureTaxonomy
    }

    private fun initializeDefaultNomenclatureValues(): List<DefaultNomenclature> {
        val expectedDefaultNomenclatureValues = listOf(
            DefaultNomenclature(
                "occtax",
                29
            )
        )

        defaultNomenclatureDao.insert(*expectedDefaultNomenclatureValues.toTypedArray())

        return expectedDefaultNomenclatureValues
    }
}