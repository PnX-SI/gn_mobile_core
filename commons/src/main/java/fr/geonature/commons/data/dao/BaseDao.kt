package fr.geonature.commons.data.dao

import android.database.Cursor
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.RawQuery
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import java.lang.reflect.ParameterizedType

/**
 * Default DAO about entities.
 *
 * @author S. Grimault
 */
abstract class BaseDao<T> {

    /**
     * Gets the table name used by entity.
     */
    val entityTableName: String

    init {
        fun parameterizedType(clazz: Class<Any>?): ParameterizedType {
            if (clazz == null) {
                throw IllegalArgumentException("No generic type found for '${javaClass.simpleName}'")
            }

            if (clazz.genericSuperclass is ParameterizedType) {
                return clazz.genericSuperclass as ParameterizedType
            }

            return parameterizedType(clazz.superclass as Class<Any>?)
        }

        @Suppress("UNCHECKED_CAST")
        val clazz = parameterizedType(javaClass).actualTypeArguments[0] as Class<T>
        entityTableName = clazz
            .getDeclaredField("TABLE_NAME")
            .get(String::class) as String
    }

    /**
     * Returns all items from the given entity table.
     */
    suspend fun findAll(): List<T> {
        return findAll(SimpleSQLiteQuery("SELECT * FROM $entityTableName"))
    }

    @RawQuery
    protected abstract suspend fun findAll(query: SupportSQLiteQuery): List<T>

    /**
     * Insert an array of objects in database (Replace strategy on conflict).
     *
     * @param entity the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insert(vararg entity: T)

    /**
     * Insert an array of objects in database (Ignore strategy on conflict).
     *
     * @param entity the objects to be inserted.
     */
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertOrIgnore(vararg entity: T)

    /**
     * Execute the given raw query.
     *
     * @param query the query
     *
     * @return A [Cursor] of results
     */
    @RawQuery
    abstract fun query(query: SupportSQLiteQuery): Cursor

    /**
     * Delete all items from the given entity table.
     */
    fun deleteAll() {
        query(SimpleSQLiteQuery("DELETE FROM $entityTableName")).moveToFirst()
    }

    /**
     * Gets the default query builder for this DAO.
     */
    fun createQueryBuilder(): SQLiteSelectQueryBuilder {
        return SQLiteSelectQueryBuilder.from(
            entityTableName,
            entityTableName
        )
    }

    /**
     * Internal query builder for this DAO.
     */
    open inner class QB {

        protected val selectQueryBuilder: SQLiteSelectQueryBuilder = createQueryBuilder()

        fun whereSelection(
            selection: String?,
            selectionArgs: Array<Any>? = null
        ): QB {
            if (selection.isNullOrBlank()) {
                return this
            }

            selectQueryBuilder.andWhere(
                selection,
                *selectionArgs
                    ?: emptyArray()
            )

            return this
        }

        fun cursor(): Cursor {
            return query(selectQueryBuilder.build())
        }
    }
}
