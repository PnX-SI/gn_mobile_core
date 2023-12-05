package fr.geonature.commons.data.entity

import android.database.Cursor
import android.os.Parcelable
import android.provider.BaseColumns
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.TypeConverters
import fr.geonature.commons.data.helper.Converters
import fr.geonature.commons.data.helper.EntityHelper.column
import fr.geonature.commons.data.helper.get
import kotlinx.parcelize.Parcelize
import org.tinylog.Logger
import java.util.Date

/**
 * Describes a dataset.
 *
 * @author S. Grimault
 */
@Entity(
    tableName = Dataset.TABLE_NAME,
    primaryKeys = [Dataset.COLUMN_ID, Dataset.COLUMN_MODULE],
)
@TypeConverters(Converters::class)
@Parcelize
data class Dataset(

    /**
     * The unique ID of this dataset.
     */
    @ColumnInfo(name = COLUMN_ID) val id: Long,

    /**
     * The related module of this dataset.
     */
    @ColumnInfo(name = COLUMN_MODULE) val module: String,

    /**
     * The name of the dataset.
     */
    @ColumnInfo(name = COLUMN_NAME) val name: String,

    /**
     * The description of the dataset.
     */
    @ColumnInfo(name = COLUMN_DESCRIPTION) val description: String?,

    /**
     * Whether this dataset is active or not.
     */
    @ColumnInfo(name = COLUMN_ACTIVE) val active: Boolean = false,

    /**
     * The creation date of this dataset.
     */
    @ColumnInfo(name = COLUMN_CREATED_AT) val createdAt: Date?,

    /**
     * The taxa list id of this dataset.
     */
    @ColumnInfo(name = COLUMN_TAXA_LIST_ID) val taxaListId: Long?
) : Parcelable {

    companion object {

        /**
         * The name of the 'observers' table.
         */
        const val TABLE_NAME = "dataset"

        /**
         * The name of the 'ID' column.
         */
        const val COLUMN_ID = BaseColumns._ID

        const val COLUMN_MODULE = "module"
        const val COLUMN_NAME = "name"
        const val COLUMN_DESCRIPTION = "description"
        const val COLUMN_ACTIVE = "active"
        const val COLUMN_CREATED_AT = "created_at"
        const val COLUMN_TAXA_LIST_ID = "taxa_list_id"

        /**
         * Gets the default projection.
         */
        fun defaultProjection(tableAlias: String = TABLE_NAME): Array<Pair<String, String>> {
            return arrayOf(
                column(
                    COLUMN_ID,
                    tableAlias
                ),
                column(
                    COLUMN_MODULE,
                    tableAlias
                ),
                column(
                    COLUMN_NAME,
                    tableAlias
                ),
                column(
                    COLUMN_DESCRIPTION,
                    tableAlias
                ),
                column(
                    COLUMN_ACTIVE,
                    tableAlias
                ),
                column(
                    COLUMN_CREATED_AT,
                    tableAlias
                ),
                column(
                    COLUMN_TAXA_LIST_ID,
                    tableAlias
                )
            )
        }

        /**
         * Gets alias from given column name.
         */
        fun getColumnAlias(
            columnName: String,
            tableAlias: String = TABLE_NAME
        ): String {
            return column(
                columnName,
                tableAlias
            ).second
        }

        /**
         * Create a new [Dataset] from the specified [Cursor].
         *
         * @param cursor A valid [Cursor]
         *
         * @return A newly created [Dataset] instance
         */
        fun fromCursor(
            cursor: Cursor,
            tableAlias: String = TABLE_NAME
        ): Dataset? {
            if (cursor.isClosed) {
                return null
            }

            return try {
                Dataset(
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ID,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_MODULE,
                                tableAlias
                            )
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_NAME,
                                tableAlias
                            )
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_DESCRIPTION,
                            tableAlias
                        )
                    ),
                    requireNotNull(
                        cursor.get(
                            getColumnAlias(
                                COLUMN_ACTIVE,
                                tableAlias
                            ),
                            false
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_CREATED_AT,
                            tableAlias
                        )
                    ),
                    cursor.get(
                        getColumnAlias(
                            COLUMN_TAXA_LIST_ID,
                            tableAlias
                        )
                    )
                )
            } catch (e: Exception) {
                e.message?.run {
                    Logger.warn { this }
                }

                null
            }
        }
    }
}
