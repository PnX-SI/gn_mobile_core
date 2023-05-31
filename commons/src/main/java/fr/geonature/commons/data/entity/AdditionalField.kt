package fr.geonature.commons.data.entity

import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import kotlinx.parcelize.Parcelize

/**
 * Describes an additional field.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = AdditionalField.TABLE_NAME,
    primaryKeys = [AdditionalField.COLUMN_ID],
    indices = [Index(
        value = [AdditionalField.COLUMN_ID, AdditionalField.COLUMN_MODULE],
        unique = true
    )]
)
@Parcelize
data class AdditionalField(

    /**
     * The unique ID of this additional field.
     */
    @ColumnInfo(name = COLUMN_ID) val id: Long,

    /**
     * The related module of this additional field.
     */
    @ColumnInfo(name = COLUMN_MODULE) val module: String,

    /**
     * The type of this additional field.
     */
    @ColumnInfo(name = COLUMN_FIELD_TYPE) val fieldType: FieldType,

    /**
     * The name of this additional field.
     */
    @ColumnInfo(name = COLUMN_FIELD_NAME) val name: String,

    /**
     * The label of this additional field.
     */
    @ColumnInfo(name = COLUMN_FIELD_LABEL) val label: String,
) : Parcelable {

    enum class FieldType(val type: String) {
        CHECKBOX("checkbox"),
        DATE("date"),
        MULTISELECT("multiselect"),
        NOMENCLATURE("nomenclature"),
        NUMBER("number"),
        RADIO("radio"),
        SELECT("select"),
        TEXT("text"),
        TIME("time")
    }

    companion object {

        /**
         * The name of the 'additional_fields' table.
         */
        const val TABLE_NAME = "additional_fields"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_MODULE = "module"
        const val COLUMN_FIELD_TYPE = "field_type"
        const val COLUMN_FIELD_NAME = "name"
        const val COLUMN_FIELD_LABEL = "label"
    }
}

/**
 * Describes a relationship between [AdditionalField] and [Dataset].
 *
 * @author S. Grimault
 */
@Entity(
    tableName = AdditionalFieldDataset.TABLE_NAME,
    primaryKeys = [AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID, AdditionalFieldDataset.COLUMN_DATASET_ID, AdditionalFieldDataset.COLUMN_MODULE],
    indices = [Index(
        value = [AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID, AdditionalFieldDataset.COLUMN_MODULE],
    ), Index(
        value = [AdditionalFieldDataset.COLUMN_DATASET_ID, AdditionalFieldDataset.COLUMN_MODULE],
    )],
    foreignKeys = [ForeignKey(
        entity = AdditionalField::class,
        parentColumns = [AdditionalField.COLUMN_ID, AdditionalField.COLUMN_MODULE],
        childColumns = [AdditionalFieldDataset.COLUMN_ADDITIONAL_FIELD_ID, AdditionalFieldDataset.COLUMN_MODULE],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = Dataset::class,
        parentColumns = [Dataset.COLUMN_ID, Dataset.COLUMN_MODULE],
        childColumns = [AdditionalFieldDataset.COLUMN_DATASET_ID, AdditionalFieldDataset.COLUMN_MODULE],
        onDelete = ForeignKey.CASCADE
    )]
)
data class AdditionalFieldDataset(
    @ColumnInfo(name = COLUMN_ADDITIONAL_FIELD_ID) val additionalFieldId: Long,
    @ColumnInfo(name = COLUMN_DATASET_ID) val datasetId: Long,
    @ColumnInfo(name = COLUMN_MODULE) val module: String,
) {
    companion object {

        /**
         * The name of the 'additional_fields_dataset' table.
         */
        const val TABLE_NAME = "additional_fields_dataset"

        const val COLUMN_ADDITIONAL_FIELD_ID = "additional_field_id"
        const val COLUMN_DATASET_ID = "dataset_id"
        const val COLUMN_MODULE = "module"
    }
}

/**
 * Describes a code object. Used as main filter for [AdditionalField].
 *
 * @author S. Grimault
 */
@Entity(
    tableName = CodeObject.TABLE_NAME,
    primaryKeys = [CodeObject.COLUMN_ADDITIONAL_FIELD_ID, CodeObject.COLUMN_CODE],
    foreignKeys = [ForeignKey(
        entity = AdditionalField::class,
        parentColumns = [AdditionalField.COLUMN_ID],
        childColumns = [CodeObject.COLUMN_ADDITIONAL_FIELD_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class CodeObject(
    @ColumnInfo(name = COLUMN_ADDITIONAL_FIELD_ID) val additionalFieldId: Long,
    @ColumnInfo(name = COLUMN_CODE) val key: String,
) : Parcelable {
    companion object {

        /**
         * The name of the 'code_object' table.
         */
        const val TABLE_NAME = "code_objects"

        const val COLUMN_ADDITIONAL_FIELD_ID = "additional_field_id"
        const val COLUMN_CODE = "code"
    }
}

/**
 * Describes an additional field value.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = FieldValue.TABLE_NAME,
    primaryKeys = [FieldValue.COLUMN_ADDITIONAL_FIELD_ID, FieldValue.COLUMN_VALUE],
    foreignKeys = [ForeignKey(
        entity = AdditionalField::class,
        parentColumns = [AdditionalField.COLUMN_ID],
        childColumns = [FieldValue.COLUMN_ADDITIONAL_FIELD_ID],
        onDelete = ForeignKey.CASCADE
    )]
)
@Parcelize
data class FieldValue(
    @ColumnInfo(name = COLUMN_ADDITIONAL_FIELD_ID) val additionalFieldId: Long,
    @ColumnInfo(name = COLUMN_VALUE) val value: String,
    @ColumnInfo(name = COLUMN_LABEL) val label: String? = null
) : Parcelable {
    companion object {

        /**
         * The name of the 'field_values' table.
         */
        const val TABLE_NAME = "field_values"

        const val COLUMN_ADDITIONAL_FIELD_ID = "additional_field_id"
        const val COLUMN_VALUE = "value"
        const val COLUMN_LABEL = "label"
    }
}

/**
 * Describes an additional field linked to [CodeObject].
 *
 * @author S. Grimault
 */
@Parcelize
data class AdditionalFieldWithCodeObject(
    @Embedded val additionalField: AdditionalField,
    @Embedded val codeObject: CodeObject
) : Parcelable

/**
 * Describes an additional fields with all possible values.
 *
 * @author S. Grimault
 */
@Parcelize
data class AdditionalFieldWithValues(
    val additionalField: AdditionalField,
    val datasetIds: List<Long> = emptyList(),
    val codeObjects: List<CodeObject>,
    val values: List<FieldValue> = emptyList()
) : Parcelable