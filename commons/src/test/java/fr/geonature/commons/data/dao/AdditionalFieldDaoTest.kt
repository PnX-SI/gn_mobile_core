package fr.geonature.commons.data.dao

import android.content.Context
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.LocalDatabase
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldNomenclature
import fr.geonature.commons.data.entity.AdditionalFieldWithCodeObject
import fr.geonature.commons.data.entity.AdditionalFieldWithNomenclatureAndCodeObject
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
 * Unit tests about [AdditionalFieldDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
internal class AdditionalFieldDaoTest {

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
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should insert and find all additional fields with no dataset matching object code`() =
        runTest {
            initializeDataset()
            initializeNomenclatureTypes()

            val expectedAdditionalFields = initializeAdditionalFields()

            val additionalFieldsFromDb = additionalFieldDao.findAllByModuleAndCodeObject(
                "occtax",
                "OCCTAX_RELEVE"
            )

            assertEquals(expectedAdditionalFields
                .asSequence()
                .filter { it.datasetIds.isEmpty() }
                .filter { it.additionalField.fieldType != AdditionalField.FieldType.NOMENCLATURE }
                .filter { it.codeObjects.any { codeObject -> codeObject.key == "OCCTAX_RELEVE" } }
                .map {
                    it.copy(values = it.values.sortedBy { v -> v.value })
                }
                .sortedBy { it.additionalField.name }
                .associate {
                    AdditionalFieldWithCodeObject(it.additionalField,
                        it.codeObjects.first { codeObject ->
                            codeObject.key == "OCCTAX_RELEVE"
                        }) to it.values.sortedBy { v -> v.value }
                },
                additionalFieldsFromDb.toSortedMap(compareBy { it.additionalField.name })
            )
        }

    @Test
    fun `should insert and find all additional fields of type 'nomenclature' with no dataset matching object code`() =
        runTest {
            initializeDataset()
            initializeNomenclatureTypes()

            val expectedAdditionalFields = initializeAdditionalFields()

            val additionalFieldsFromDb =
                additionalFieldDao.findAllWithNomenclatureByModuleAndCodeObject(
                    "occtax",
                    "OCCTAX_RELEVE"
                )

            assertEquals(expectedAdditionalFields
                .asSequence()
                .filter { it.datasetIds.isEmpty() }
                .filter { it.additionalField.fieldType == AdditionalField.FieldType.NOMENCLATURE }
                .filter { it.codeObjects.any { codeObject -> codeObject.key == "OCCTAX_RELEVE" } }
                .sortedBy { it.additionalField.name }
                .flatMap { additionalFieldWithValues ->
                    additionalFieldWithValues.codeObjects.map { codeObject ->
                        AdditionalFieldWithNomenclatureAndCodeObject(
                            additionalField = additionalFieldWithValues.additionalField,
                            mnemonic = additionalFieldWithValues.nomenclatureTypeMnemonic!!,
                            codeObject = codeObject
                        )
                    }
                }
                .toList(),
                additionalFieldsFromDb.sortedBy { it.additionalField.name })
        }

    @Test
    fun `should insert and find all additional fields matching dataset and object code`() =
        runTest {
            initializeDataset()
            initializeNomenclatureTypes()

            val expectedAdditionalFields = initializeAdditionalFields()

            val additionalFieldsFromDb = additionalFieldDao.findAllByModuleAndDatasetAndCodeObject(
                "occtax",
                1L,
                "OCCTAX_RELEVE"
            )

            assertEquals(expectedAdditionalFields
                .asSequence()
                .filter { it.datasetIds.any { datasetId -> datasetId == 1L } }
                .filter { it.additionalField.fieldType != AdditionalField.FieldType.NOMENCLATURE }
                .filter { it.codeObjects.any { codeObject -> codeObject.key == "OCCTAX_RELEVE" } }
                .map {
                    it.copy(values = it.values.sortedBy { v -> v.value })
                }
                .sortedBy { it.additionalField.name }
                .associate {
                    AdditionalFieldWithCodeObject(it.additionalField,
                        it.codeObjects.first { codeObject ->
                            codeObject.key == "OCCTAX_RELEVE"
                        }) to it.values.sortedBy { v -> v.value }
                },
                additionalFieldsFromDb.toSortedMap(compareBy { it.additionalField.name })
            )
        }

    @Test
    fun `should insert and find all additional fields of type 'nomenclature' matching dataset and a list of object code`() =
        runTest {
            initializeDataset()
            initializeNomenclatureTypes()

            val expectedAdditionalFields = initializeAdditionalFields()

            val additionalFieldsFromDb =
                additionalFieldDao.findAllWithNomenclatureByModuleAndDatasetAndCodeObject(
                    "occtax",
                    1L,
                    "OCCTAX_RELEVE"
                )

            assertEquals(expectedAdditionalFields
                .asSequence()
                .filter { it.datasetIds.any { datasetId -> datasetId == 1L } }
                .filter { it.additionalField.fieldType == AdditionalField.FieldType.NOMENCLATURE }
                .filter {
                    it.codeObjects.any { codeObject ->
                        listOf(
                            "OCCTAX_RELEVE"
                        ).contains(codeObject.key)
                    }
                }
                .sortedBy { it.additionalField.name }
                .flatMap { additionalFieldWithValues ->
                    additionalFieldWithValues.codeObjects.map { codeObject ->
                        AdditionalFieldWithNomenclatureAndCodeObject(
                            additionalField = additionalFieldWithValues.additionalField,
                            mnemonic = additionalFieldWithValues.nomenclatureTypeMnemonic!!,
                            codeObject = codeObject
                        )
                    }
                }
                .toList(),
                additionalFieldsFromDb.sortedBy { it.additionalField.name })
        }

    @Test
    fun `should insert and find all additional fields matching dataset and a list of object code`() =
        runTest {}

    private fun initializeDataset(): List<Dataset> {
        return listOf(
            Dataset(
                id = 1,
                module = "occtax",
                name = "Contact aléatoire tous règnes confondus",
                description = "Observations aléatoires de la faune, de la flore ou de la fonge",
                active = true,
                createdAt = Date.from(Instant.parse("2016-10-28T08:15:00Z"))
                ,100
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
        return listOf(
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 1L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.TEXT,
                    name = "text_field",
                    label = "Text field"
                ),
                datasetIds = listOf(1L),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 1L,
                        key = "OCCTAX_RELEVE"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 2L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.CHECKBOX,
                    name = "checkbox_field",
                    label = "Checkbox field"
                ),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 2L,
                        key = "OCCTAX_RELEVE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 2L,
                        value = "true"
                    ),
                    FieldValue(
                        additionalFieldId = 2L,
                        value = "false"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 3L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.NOMENCLATURE,
                    name = "statut_bio_field",
                    label = "STATUT_BIO field"
                ),
                nomenclatureTypeMnemonic = "STATUT_BIO",
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 3L,
                        key = "OCCTAX_RELEVE"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 4L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.NOMENCLATURE,
                    name = "meth_obs_field",
                    label = "METH_OBS field"
                ),
                datasetIds = listOf(1L),
                nomenclatureTypeMnemonic = "METH_OBS",
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 4L,
                        key = "OCCTAX_RELEVE"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 5L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.SELECT,
                    name = "select_field",
                    label = "Select field"
                ),
                datasetIds = listOf(1L),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 5L,
                        key = "OCCTAX_OCCURENCE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 5L,
                        value = "value1"
                    ),
                    FieldValue(
                        additionalFieldId = 5L,
                        value = "value2"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 6L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.RADIO,
                    name = "radio_field",
                    label = "Radio field"
                ),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 6L,
                        key = "OCCTAX_OCCURENCE"
                    )
                ),
                values = listOf(
                    FieldValue(
                        additionalFieldId = 6L,
                        value = "value1",
                        label = "Value 1"
                    ),
                    FieldValue(
                        additionalFieldId = 6L,
                        value = "value2",
                        label = "Value 2"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 7L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.NOMENCLATURE,
                    name = "eta_bio_field",
                    label = "ETA_BIO field"
                ),
                nomenclatureTypeMnemonic = "ETA_BIO",
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 7L,
                        key = "OCCTAX_DENOMBREMENT"
                    )
                )
            ),
            AdditionalFieldWithValues(
                additionalField = AdditionalField(
                    id = 8L,
                    module = "occtax",
                    fieldType = AdditionalField.FieldType.NUMBER,
                    name = "number_field",
                    label = "Number field"
                ),
                codeObjects = listOf(
                    CodeObject(
                        additionalFieldId = 8L,
                        key = "OCCTAX_DENOMBREMENT"
                    )
                )
            )
        ).also { additionalFields ->
            additionalFieldDao.insert(*additionalFields
                .map { it.additionalField }
                .toTypedArray())
            additionalFieldDatasetDao.insert(*additionalFields
                .flatMap {
                    it.datasetIds.map { datasetId ->
                        AdditionalFieldDataset(
                            additionalFieldId = it.additionalField.id,
                            datasetId = datasetId,
                            module = "occtax"
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
    }
}