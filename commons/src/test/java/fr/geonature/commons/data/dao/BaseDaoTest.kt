package fr.geonature.commons.data.dao

import android.database.Cursor
import android.database.MatrixCursor
import androidx.room.Entity
import androidx.sqlite.db.SupportSQLiteQuery
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

/**
 * Unit tests about [BaseDao].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class BaseDaoTest {

    @Test
    fun testTableName() {
        assertEquals(SimpleEntity.TABLE_NAME,
                     SimpleEntityDao().entityTableName)
    }

    @Test(expected = NoSuchFieldException::class)
    fun testInvalidEntity() {
        InvalidEntityDao().entityTableName
    }

    @Test
    fun getSimpleQueryBuilder() {
        // given a simple query builder from DAO
        val sqLiteQuery = SimpleEntityDao().createQueryBuilder()
                .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals("""
            SELECT *
            FROM entity_table entity_table
        """.trimIndent(),
                     sqLiteQuery.sql)
    }

    @Test
    fun getSimpleQueryBuilderFromQB() {
        // given a simple query builder from DAO
        val sqLiteQuery = SimpleEntityDao().QB()
                .getQueryBuilder()
                .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals("""
            SELECT *
            FROM entity_table entity_table
        """.trimIndent(),
                     sqLiteQuery.sql)
    }

    @Test
    fun getQueryBuilderWithSelectionWithNoArgs() {
        // given a simple query builder from DAO
        val sqLiteQuery = (SimpleEntityDao().QB().whereSelection("col = 1") as SimpleEntityDao.QB).getQueryBuilder()
                .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals("""
            SELECT *
            FROM entity_table entity_table
            WHERE (col = 1)
        """.trimIndent(),
                     sqLiteQuery.sql)
        assertEquals(0,
                     sqLiteQuery.argCount)
    }

    @Test
    fun getQueryBuilderWithSelectionWithArgs() {
        // given a simple query builder from DAO
        val sqLiteQuery = (SimpleEntityDao().QB().whereSelection("col = ? OR col = ?",
                                                                 arrayOf(12,
                                                                         "some_args")) as SimpleEntityDao.QB).getQueryBuilder()
                .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals("""
            SELECT *
            FROM entity_table entity_table
            WHERE (col = ? OR col = ?)
        """.trimIndent(),
                     sqLiteQuery.sql)
        assertEquals(2,
                     sqLiteQuery.argCount)
    }

    @Entity
    class InvalidEntity

    @Entity(tableName = SimpleEntity.TABLE_NAME)
    class SimpleEntity {
        companion object {
            const val TABLE_NAME = "entity_table"
        }
    }

    class InvalidEntityDao : BaseDao<InvalidEntity>() {
        override fun insert(vararg entity: InvalidEntity) {
        }

        override fun select(query: SupportSQLiteQuery): Cursor {
            return MatrixCursor(emptyArray())
        }
    }

    class SimpleEntityDao : BaseDao<SimpleEntity>() {
        override fun insert(vararg entity: SimpleEntity) {
        }

        override fun select(query: SupportSQLiteQuery): Cursor {
            return MatrixCursor(emptyArray())
        }

        inner class QB : BaseDao<SimpleEntity>.QB() {
            fun getQueryBuilder(): SQLiteSelectQueryBuilder {
                return selectQueryBuilder
            }
        }
    }
}