package fr.geonature.commons.data.dao

import androidx.room.Dao
import androidx.room.Query
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.AdditionalFieldWithCodeObject
import fr.geonature.commons.data.entity.AdditionalFieldWithValues
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.FieldValue

/**
 * Data access object for [AdditionalField] and [AdditionalFieldWithValues].
 *
 * @author S. Grimault
 */
@Dao
abstract class AdditionalFieldDao : BaseDao<AdditionalField>() {

    /**
     * Fetches all additional fields matching the given module, dataset and code object as main
     * filters.
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
        WHERE af.${AdditionalField.COLUMN_MODULE} = :module
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllByModuleAndDatasetAndCodeObject(
        module: String,
        datasetId: Long,
        vararg codeObject: String
    ): Map<AdditionalFieldWithCodeObject, List<FieldValue>>

    /**
     * Fetches all additional fields linked with no dataset matching the given module and code
     * object as main filters.
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
        WHERE af.${AdditionalField.COLUMN_MODULE} = :module
        AND afd.${AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID} IS NULL
        AND co.${CodeObject.COLUMN_CODE} IN (:codeObject)
        """
    )
    abstract suspend fun findAllByModuleAndCodeObject(
        module: String,
        vararg codeObject: String
    ): Map<AdditionalFieldWithCodeObject, List<FieldValue>>
}

/**
 * Data access object for [AdditionalFieldDataset].
 *
 * @author S. Grimault
 */
@Dao
abstract class AdditionalFieldDatasetDao: BaseDao<AdditionalFieldDataset>()

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