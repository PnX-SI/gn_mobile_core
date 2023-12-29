package fr.geonature.datasync.sync.io

import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.datasync.FixtureHelper.getFixture
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AdditionalFieldJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
internal class AdditionalFieldJsonReaderTest {

    private lateinit var additionalFieldJsonReader: AdditionalFieldJsonReader

    @Before
    fun setUp() {
        additionalFieldJsonReader = AdditionalFieldJsonReader()
    }

    @Test
    fun `should read from invalid JSON string`() {
        assertTrue(
            additionalFieldJsonReader
                .read("")
                .isEmpty()
        )
        assertTrue(
            additionalFieldJsonReader
                .read(" ")
                .isEmpty()
        )
    }

    @Test
    fun `should read additional fields with values`() {
        // given additional fields to read
        val json = getFixture("additional_fields_geonature.json")

        // when parsing this JSON
        val additionalFieldWithValues = additionalFieldJsonReader.read(json)

        // then
        assertEquals(
            listOf(
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 2,
                        fieldType = AdditionalField.FieldType.TEXT,
                        name = "test_add",
                        label = "Test champs"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 2,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = emptyList()
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 6,
                        fieldType = AdditionalField.FieldType.SELECT,
                        name = "exemple select",
                        label = "exemple select"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 6,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 6,
                            value = "test1"
                        ),
                        FieldValue(
                            additionalFieldId = 6,
                            value = "test2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 9,
                        fieldType = AdditionalField.FieldType.RADIO,
                        name = "test_radio",
                        label = "Test radio"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 9,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 9,
                            value = "Oui"
                        ),
                        FieldValue(
                            additionalFieldId = 9,
                            value = "Non"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 10,
                        fieldType = AdditionalField.FieldType.CHECKBOX,
                        name = "test_checkbox",
                        label = "Test checkbox"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 10,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 10,
                            value = "check1"
                        ),
                        FieldValue(
                            additionalFieldId = 10,
                            value = "check2"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 8,
                        fieldType = AdditionalField.FieldType.NOMENCLATURE,
                        name = "test nomenclature",
                        label = "Test nomenclature"
                    ),
                    nomenclatureTypeMnemonic = "VENT",
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 8,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = emptyList()
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 12,
                        fieldType = AdditionalField.FieldType.RADIO,
                        name = "radio_test2",
                        label = "Radio test 2"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 12,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 12,
                            value = "true"
                        ),
                        FieldValue(
                            additionalFieldId = 12,
                            value = "false"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 11,
                        fieldType = AdditionalField.FieldType.RADIO,
                        name = "bool_radio",
                        label = "Test bool radio"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 11,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 11,
                            value = "truc"
                        ),
                        FieldValue(
                            additionalFieldId = 11,
                            value = "bidule"
                        )
                    )
                ),
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 13,
                        fieldType = AdditionalField.FieldType.MULTISELECT,
                        name = "select_multiple",
                        label = "test select multiple"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 13,
                            key = "OCCTAX_DENOMBREMENT"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 13,
                            value = "val1",
                            label = "val 01"
                        ),
                        FieldValue(
                            additionalFieldId = 13,
                            value = "val2",
                            label = "val 02"
                        ),
                        FieldValue(
                            additionalFieldId = 13,
                            value = "val3",
                            label = "val 03"
                        ),
                        FieldValue(
                            additionalFieldId = 13,
                            value = "val4",
                            label = "val 04"
                        )
                    )
                )
            ),
            additionalFieldWithValues
        )
    }

    @Test
    fun `should read additional field of type 'multiselect' with invalid values`() {
        // given an additional field to read with invalid values
        val json = getFixture("additional_field_multiselect_invalid.json")

        // when parsing this JSON
        val additionalField = additionalFieldJsonReader.read(json)

        // then
        assertEquals(
            listOf(
                AdditionalFieldWithValues(
                    additionalField = AdditionalField(
                        id = 7,
                        fieldType = AdditionalField.FieldType.MULTISELECT,
                        name = "test_multi_select",
                        label = "Test multiselect"
                    ),
                    codeObjects = listOf(
                        CodeObject(
                            additionalFieldId = 7,
                            key = "OCCTAX_RELEVE"
                        )
                    ),
                    values = listOf(
                        FieldValue(
                            additionalFieldId = 7,
                            value = "cablage"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "point_eau"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Clôture"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Groupe isolé d'arbres"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Ecobuage"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Bâti"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Falaise"
                        ),
                        FieldValue(
                            additionalFieldId = 7,
                            value = "Autres"
                        )
                    )
                )
            ),
            additionalField
        )
    }
}