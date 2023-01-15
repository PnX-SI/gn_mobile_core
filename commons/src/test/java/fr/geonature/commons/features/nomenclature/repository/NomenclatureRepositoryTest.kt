package fr.geonature.commons.features.nomenclature.repository

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.NomenclatureWithType
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.features.nomenclature.data.INomenclatureLocalDataSource
import fr.geonature.commons.features.nomenclature.error.NomenclatureException
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
class NomenclatureRepositoryTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    @MockK
    private lateinit var nomenclatureLocalDataSource: INomenclatureLocalDataSource

    private lateinit var nomenclatureRepository: INomenclatureRepository

    @Before
    fun setUp() {
        init(this)

        nomenclatureRepository = NomenclatureRepositoryImpl(nomenclatureLocalDataSource)
    }

    @After
    fun tearDown() {
        unmockkAll()
    }

    @Test
    fun `should find all nomenclature types`() =
        runTest {
            coEvery {
                nomenclatureLocalDataSource.getAllNomenclatureTypes()
            } returns listOf(
                NomenclatureType(
                    id = 7,
                    mnemonic = "ETA_BIO",
                    defaultLabel = "Etat biologique de l'observation"
                ),
                NomenclatureType(
                    id = 14,
                    mnemonic = "METH_OBS",
                    defaultLabel = "Méthodes d'observation"
                ),
                NomenclatureType(
                    id = 13,
                    mnemonic = "STATUT_BIO",
                    defaultLabel = "Statut biologique"
                )
            )

            // when
            val result = nomenclatureRepository.getAllNomenclatureTypes()

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                listOf(
                    NomenclatureType(
                        id = 7,
                        mnemonic = "ETA_BIO",
                        defaultLabel = "Etat biologique de l'observation"
                    ),
                    NomenclatureType(
                        id = 14,
                        mnemonic = "METH_OBS",
                        defaultLabel = "Méthodes d'observation"
                    ),
                    NomenclatureType(
                        id = 13,
                        mnemonic = "STATUT_BIO",
                        defaultLabel = "Statut biologique"
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should return NoNomenclatureTypeFoundException if no nomenclature types was found`() =
        runTest {
            coEvery {
                nomenclatureLocalDataSource.getAllNomenclatureTypes()
            } answers { throw NomenclatureException.NoNomenclatureTypeFoundException }

            // when
            val result = nomenclatureRepository.getAllNomenclatureTypes()

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NomenclatureException.NoNomenclatureTypeFoundException)
        }

    @Test
    fun `should find all default nomenclature values`() =
        runTest {
            coEvery {
                nomenclatureLocalDataSource.getAllNomenclatureTypes()
            } returns listOf(
                NomenclatureType(
                    id = 7,
                    mnemonic = "ETA_BIO",
                    defaultLabel = "Etat biologique de l'observation"
                ),
                NomenclatureType(
                    id = 14,
                    mnemonic = "METH_OBS",
                    defaultLabel = "Méthodes d'observation"
                ),
                NomenclatureType(
                    id = 13,
                    mnemonic = "STATUT_BIO",
                    defaultLabel = "Statut biologique"
                )
            )

            coEvery {
                nomenclatureLocalDataSource.getAllDefaultNomenclatureValues()
            } returns listOf(
                Nomenclature(
                    id = 29,
                    code = "1",
                    hierarchy = "013.001",
                    defaultLabel = "Non renseigné",
                    typeId = 13
                )
            )

            // when
            val result = nomenclatureRepository.getAllDefaultNomenclatureValues()

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                listOf(
                    NomenclatureWithType(
                        id = 29,
                        code = "1",
                        hierarchy = "013.001",
                        defaultLabel = "Non renseigné",
                        typeId = 13,
                        type = NomenclatureType(
                            id = 13,
                            mnemonic = "STATUT_BIO",
                            defaultLabel = "Statut biologique"
                        )
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should get nomenclature values by type matching given taxonomy kingdom and group`() =
        runTest {
            coEvery {
                // given some nomenclature values from given type
                nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                    mnemonic = "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } returns listOf(
                Nomenclature(
                    id = 29,
                    code = "1",
                    hierarchy = "013.001",
                    defaultLabel = "Non renseigné",
                    typeId = 13
                ),
                Nomenclature(
                    id = 31,
                    code = "3",
                    hierarchy = "013.003",
                    defaultLabel = "Reproduction",
                    typeId = 13
                ),
                Nomenclature(
                    id = 32,
                    code = "4",
                    hierarchy = "013.004",
                    defaultLabel = "Hibernation",
                    typeId = 13
                )
            )

            // when
            val result = nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )

            // then
            assertTrue(result.isSuccess)
            assertEquals(
                listOf(
                    Nomenclature(
                        id = 29,
                        code = "1",
                        hierarchy = "013.001",
                        defaultLabel = "Non renseigné",
                        typeId = 13
                    ),
                    Nomenclature(
                        id = 31,
                        code = "3",
                        hierarchy = "013.003",
                        defaultLabel = "Reproduction",
                        typeId = 13
                    ),
                    Nomenclature(
                        id = 32,
                        code = "4",
                        hierarchy = "013.004",
                        defaultLabel = "Hibernation",
                        typeId = 13
                    )
                ),
                result.getOrThrow()
            )
        }

    @Test
    fun `should return NoNomenclatureValuesFoundException if no nomenclature values was found from given type`() =
        runTest {
            // given no nomenclature values from given type
            coEvery {
                nomenclatureLocalDataSource.getNomenclatureValuesByTypeAndTaxonomy(
                    "STATUT_BIO",
                    Taxonomy(
                        kingdom = "Animalia",
                        group = "Oiseaux"
                    )
                )
            } answers { throw NomenclatureException.NoNomenclatureValuesFoundException(firstArg()) }

            // when
            val result = nomenclatureRepository.getNomenclatureValuesByTypeAndTaxonomy(
                mnemonic = "STATUT_BIO",
                Taxonomy(
                    kingdom = "Animalia",
                    group = "Oiseaux"
                )
            )

            // then
            assertTrue(result.isFailure)
            assertTrue(result.exceptionOrNull() is NomenclatureException.NoNomenclatureValuesFoundException)
        }
}