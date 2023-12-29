package fr.geonature.commons.features.nomenclature.data

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.dao.AdditionalFieldDao
import fr.geonature.commons.data.dao.AdditionalFieldDatasetDao
import fr.geonature.commons.data.dao.AdditionalFieldNomenclatureDao
import fr.geonature.commons.data.dao.CodeObjectDao
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.dao.FieldValueDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldNomenclature
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.FieldValue
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
import java.time.Instant
import java.util.Date

/**
 * Unit tests about [INomenclatureLocalDataSource].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class AdditionalFieldLocalDataSourceTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: LocalDatabase
    private lateinit var datasetDao: DatasetDao
    private lateinit var nomenclatureTypeDao: NomenclatureTypeDao
    private lateinit var additionalFieldDao: AdditionalFieldDao
    private lateinit var additionalFieldDatasetDao: AdditionalFieldDatasetDao
    private lateinit var additionalFieldNomenclatureDao: AdditionalFieldNomenclatureDao
    private lateinit var codeObjectDao: CodeObjectDao
    private lateinit var fieldValueDao: FieldValueDao
    private lateinit var additionalFieldLocalDataSource: IAdditionalFieldLocalDataSource

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
        nomenclatureTypeDao = db.nomenclatureTypeDao()
        additionalFieldDao = db.additionalFieldDao()
        additionalFieldDatasetDao = db.additionalFieldDatasetDao()
        additionalFieldNomenclatureDao = db.additionalFieldNomenclatureDao()
        codeObjectDao = db.codeObjectDao()
        fieldValueDao = db.fieldValueDao()

        additionalFieldLocalDataSource = AdditionalFieldLocalDataSourceImpl(db)
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should find all additional fields matching object code`() =
        runTest {
            initializeDataset()
            initializeNomenclatureTypes()
            val expectedAdditionalFields = initializeAdditionalFields()

            val additionalFieldsFromDb = additionalFieldLocalDataSource.getAdditionalFields(
                1L,
                "OCCTAX_RELEVE",
                "OCCTAX_OCCURENCE"
            )

            assertEquals(
                expectedAdditionalFields
                    .filter { additionalField ->
                        listOf(
                            1L,
                            2L,
                            3L
                        ).any { it == additionalField.additionalField.id }
                    }
                    .map {
                        it.copy(
                            codeObjects = it.codeObjects.sortedBy { codeObject -> codeObject.key },
                            values = it.values.sortedBy { v -> v.value },
                        )
                    }
                    .sortedBy { it.additionalField.name },
                additionalFieldsFromDb
                    .map {
                        it.copy(
                            codeObjects = it.codeObjects.sortedBy { codeObject -> codeObject.key },
                            values = it.values.sortedBy { v -> v.value },
                        )
                    }
                    .sortedBy { it.additionalField.name },
            )
        }

    @Test
    fun `should add new additional fields and remove all existing ones`() =
        runTest {
            val expectedDataset = initializeDataset()
            val expectedNomenclatureTypes = initializeNomenclatureTypes()
            initializeAdditionalFields()

            val expectedAdditionalFields = listOf(
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 7L,
                        fieldType = AdditionalField.FieldType.RADIO,
                        name = "radio_field_new",
                        label = "New radio field"
                    ),
                    datasetIds = listOf(17L),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 7L,
                            key = "OCCTAX_OCCURENCE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 7L,
                            value = "false"
                        ),
                        FieldValue(
                            additionalFieldId = 7L,
                            value = "true"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 6L,
                        fieldType = AdditionalField.FieldType.NOMENCLATURE,
                        name = "statut_bio_field",
                        label = "STATUT_BIO field"
                    ),
                    datasetIds = listOf(17L),
                    nomenclatureTypeMnemonic = "STATUT_BIO",
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 6L,
                            key = "OCCTAX_DENOMBREMENT"
                        )
                    ),
                )
            )

            additionalFieldLocalDataSource.updateAdditionalFields(*expectedAdditionalFields.toTypedArray())

            val additionalFieldsFromDb = additionalFieldLocalDataSource.getAdditionalFields(
                17L,
                "OCCTAX_DENOMBREMENT"
            )

            assertEquals(expectedAdditionalFields
                .filter { additionalField ->
                    listOf(
                        6L
                    ).any { it == additionalField.additionalField.id }
                }
                .sortedBy { it.additionalField.name },
                additionalFieldsFromDb
                    .map {
                        it.copy(
                            codeObjects = it.codeObjects.sortedBy { codeObject -> codeObject.key },
                            values = it.values.sortedBy { v -> v.value },
                        )
                    }
                    .sortedBy { it.additionalField.name })
            assertEquals(
                expectedDataset,
                datasetDao.findAll()
            )
            assertEquals(
                expectedNomenclatureTypes,
                nomenclatureTypeDao.findAll()
            )
        }

    private fun initializeDataset(): List<Dataset> {
        return listOf(
            Dataset(
                id = 1,
                name = "Contact aléatoire tous règnes confondus",
                description = "Observations aléatoires de la faune, de la flore ou de la fonge",
                active = true,
                createdAt = Date.from(Instant.parse("2016-10-28T08:15:00Z")),
                updatedAt = null,
                100
            ),
            Dataset(
                id = 17,
                name = "Jeu de données personnel de Auger Ariane",
                description = "Jeu de données personnel de Auger Ariane",
                active = true,
                createdAt = Date.from(Instant.parse("2020-03-28T10:00:00Z")),
                updatedAt = null,
                100
            ),
            Dataset(
                id = 30,
                name = "Observation opportuniste aléatoire tout règne confondu",
                description = "Observation opportuniste aléatoire tout règne confondu",
                active = true,
                createdAt = Date.from(Instant.parse("2022-11-19T12:00:00Z")),
                updatedAt = null,
                100
            )
        ).also {
            datasetDao.insert(*it.toTypedArray())
        }
    }

    private fun initializeNomenclatureTypes(): List<NomenclatureType> {
        return listOf(
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
        ).also {
            nomenclatureTypeDao.insert(*it.toTypedArray())
        }
    }

    private fun initializeAdditionalFields(): List<AdditionalFieldWithValues> {
        val additionalFields = listOf(
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 1,
                    fieldType = AdditionalField.FieldType.TEXT,
                    name = "text_field",
                    label = "Text field"
                ),
                datasetIds = listOf(1L),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 1,
                        key = "OCCTAX_RELEVE"
                    )
                ),
                values = emptyList()
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 2,
                    fieldType = AdditionalField.FieldType.CHECKBOX,
                    name = "checkbox_field",
                    label = "Checkbox field"
                ),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 2,
                        key = "OCCTAX_RELEVE"
                    ),
                    CodeObject(
                        additionalFieldId = 2,
                        key = "OCCTAX_OCCURENCE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 2,
                        value = "true"
                    ),
                    FieldValue(
                        additionalFieldId = 2,
                        value = "false"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 3,
                    fieldType = AdditionalField.FieldType.SELECT,
                    name = "select_field",
                    label = "Select field"
                ),
                datasetIds = listOf(1L),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 3,
                        key = "OCCTAX_OCCURENCE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 3,
                        value = "value1"
                    ),
                    FieldValue(
                        additionalFieldId = 3,
                        value = "value2"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 4,
                    fieldType = AdditionalField.FieldType.RADIO,
                    name = "radio_field",
                    label = "Radio field"
                ),
                datasetIds = listOf(17L),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 4,
                        key = "OCCTAX_OCCURENCE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 4,
                        value = "value1",
                        label = "Value 1"
                    ),
                    FieldValue(
                        additionalFieldId = 4,
                        value = "value2",
                        label = "Value 2"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 5L,
                    fieldType = AdditionalField.FieldType.NOMENCLATURE,
                    name = "eta_bio_field",
                    label = "ETA_BIO field"
                ),
                nomenclatureTypeMnemonic = "ETA_BIO",
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 5L,
                        key = "OCCTAX_DENOMBREMENT"
                    )
                )
            ),
        ).also { additionalFields ->
            additionalFieldDao.insert(*additionalFields
                .map { it.additionalField }
                .toTypedArray())
            additionalFieldDatasetDao.insert(*additionalFields
                .flatMap {
                    it.datasetIds.map { datasetId ->
                        AdditionalFieldDataset(
                            additionalFieldId = it.additionalField.id,
                            datasetId = datasetId
                        )
                    }
                }
                .toTypedArray())
            additionalFieldNomenclatureDao.insert(*additionalFields
                .mapNotNull {
                    it.nomenclatureTypeMnemonic?.let { mnemonic ->
                        AdditionalFieldNomenclature(
                            additionalFieldId = it.additionalField.id,
                            nomenclatureTypeMnemonic = mnemonic,
                        )
                    }
                }
                .toTypedArray())
            codeObjectDao.insert(*additionalFields
                .flatMap { it.codeObjects }
                .toTypedArray())
            fieldValueDao.insert(*additionalFields
                .flatMap { it.values }
                .toTypedArray())
        }

        return additionalFields
    }
}