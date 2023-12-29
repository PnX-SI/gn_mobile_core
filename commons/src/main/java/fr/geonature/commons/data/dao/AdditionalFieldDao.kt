package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldNomenclature
import fr.geonature.commons.data.entity.AdditionalFieldWithCodeObject
import fr.geonature.commons.data.entity.AdditionalFieldWithNomenclatureAndCodeObject
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.commons.data.entity.NomenclatureType

/**
 * Data access object for [AdditionalField] and [AdditionalFieldWithValues].
 *
 * @author S. Grimault
 */
@Dao
abstract class AdditionalFieldDao : BaseDao<AdditionalField>() {

    /**
     * Fetches all additional fields linked with no dataset matching the given code objects as main
     * filters.
     */
    @Query(
        """SELECT
            af.*,
            co.*,
            fv.*
        FROM ${AdditionalField.TABLE_NAME} af
        JOIN ${CodeObject.TABLE_NAME} co ON co.${CodeObject.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        LEFT JOIN ${AdditionalFieldDataset.TABLE_NAME} afd ON afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        LEFT JOIN ${FieldValue.TABLE_NAME} fv on fv.${FieldValue.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        WHERE af.${AdditionalField.COLUMN_FIELD_TYPE} IS NOT "NOMENCLATURE"
        AND afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} IS NULL
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllByCodeObject(vararg codeObject: String): Map<AdditionalFieldWithCodeObject, List<FieldValue>>

    /**
     * Fetches all additional fields of type 'nomenclature' linked with no dataset and linked to
     * nomenclature type matching the given nomenclature type and code objects as main filters.
     */
    @Query(
        """SELECT
            af.*,
            nt.${NomenclatureType.COLUMN_MNEMONIC},
            co.*
        FROM ${AdditionalField.TABLE_NAME} af
        JOIN ${AdditionalFieldNomenclature.TABLE_NAME} afn ON afn.${AdditionalFieldNomenclature.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        JOIN ${NomenclatureType.TABLE_NAME} nt ON nt.${NomenclatureType.COLUMN_MNEMONIC} = afn.${AdditionalFieldNomenclature.COLUMN_NOMENCLATURE_TYPE_MNEMONIC}
        JOIN ${CodeObject.TABLE_NAME} co ON co.${CodeObject.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        LEFT JOIN ${AdditionalFieldDataset.TABLE_NAME} afd ON afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        WHERE af.${AdditionalField.COLUMN_FIELD_TYPE} = "NOMENCLATURE"
        AND afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} IS NULL
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllWithNomenclatureByCodeObject(vararg codeObject: String): List<AdditionalFieldWithNomenclatureAndCodeObject>

    /**
     * Fetches all additional fields matching the given dataset and code objects as main filters.
     */
    @Query(
        """SELECT
            af.*,
            co.*,
            fv.*
        FROM ${AdditionalField.TABLE_NAME} af
        JOIN ${CodeObject.TABLE_NAME} co ON co.${CodeObject.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        JOIN ${AdditionalFieldDataset.TABLE_NAME} afd ON afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
            AND afd.${AdditionalFieldDataset.COLUMN_DATASET_ID} = :datasetId
        LEFT JOIN ${FieldValue.TABLE_NAME} fv on fv.${FieldValue.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        WHERE af.${AdditionalField.COLUMN_FIELD_TYPE} IS NOT "NOMENCLATURE"
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllByDatasetAndCodeObject(
        datasetId: Long,
        vararg codeObject: String
    ): Map<AdditionalFieldWithCodeObject, List<FieldValue>>

    /**
     * Fetches all additional fields of type 'nomenclature' linked with nomenclature type matching
     * the given dataset and code objects as main filters.
     */
    @Query(
        """SELECT
            af.*,
            nt.${NomenclatureType.COLUMN_MNEMONIC},
            co.*
        FROM ${AdditionalField.TABLE_NAME} af
        JOIN ${AdditionalFieldNomenclature.TABLE_NAME} afn ON afn.${AdditionalFieldNomenclature.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        JOIN ${NomenclatureType.TABLE_NAME} nt ON nt.${NomenclatureType.COLUMN_MNEMONIC} = afn.${AdditionalFieldNomenclature.COLUMN_NOMENCLATURE_TYPE_MNEMONIC}
        JOIN ${CodeObject.TABLE_NAME} co ON co.${CodeObject.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
        JOIN ${AdditionalFieldDataset.TABLE_NAME} afd ON afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} = af.${AdditionalField.COLUMN_ID}
            AND afd.${AdditionalFieldDataset.COLUMN_DATASET_ID} = :datasetId
        WHERE af.${AdditionalField.COLUMN_FIELD_TYPE} = "NOMENCLATURE"
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllWithNomenclatureByDatasetAndCodeObject(
        datasetId: Long,
        vararg codeObject: String
    ): List<AdditionalFieldWithNomenclatureAndCodeObject>
}

/**
 * Data access object for [AdditionalFieldDataset].
 *
 * @author S. Grimault
 */
@Dao
abstract class AdditionalFieldDatasetDao : BaseDao<AdditionalFieldDataset>()

/**
 * Data access object for [AdditionalFieldNomenclature].
 *
 * @author S. Grimault
 */
@Dao
abstract class AdditionalFieldNomenclatureDao : BaseDao<AdditionalFieldNomenclature>()

/**
 * Data access object for [CodeObject].
 *
 * @author S. Grimault
 */
@Dao
abstract class CodeObjectDao : BaseDao<CodeObject>()

/**
 * Data access object for [FieldValue].
 *
 * @author S. Grimault
 */
@Dao
abstract class FieldValueDao : BaseDao<FieldValue>()