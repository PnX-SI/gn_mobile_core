package fr.geonature.commons.data.dao

import android.content.Context
import android.database.Cursor
import android.database.MatrixCursor
import android.provider.BaseColumns
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.room.ColumnInfo
import androidx.room.Dao
import androidx.room.Database
import androidx.room.Entity
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteQuery
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import fr.geonature.commons.CoroutineTestRule
import fr.geonature.commons.data.helper.SQLiteSelectQueryBuilder
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

/**
 * Unit tests about [BaseDao].
 *
 * @author S. Grimault
 */
@ExperimentalCoroutinesApi
@RunWith(AndroidJUnit4::class)
class BaseDaoTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @get:Rule
    val coroutineTestRule = CoroutineTestRule()

    private lateinit var db: DummyDatabase
    private lateinit var simpleEntityDao: SimpleEntityDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room
            .inMemoryDatabaseBuilder(
                context,
                DummyDatabase::class.java
            )
            .allowMainThreadQueries()
            .build()
        simpleEntityDao = db.simpleEntityDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun `should get entity table name from base DAO`() {
        assertEquals(
            SimpleEntity.TABLE_NAME,
            simpleEntityDao.entityTableName
        )
    }

    @Test(expected = NoSuchFieldException::class)
    fun `should throw NoSuchFieldException from entity with no table name defined`() {
        InvalidEntityDao().entityTableName
    }

    @Test
    fun `should insert and find all items from DAO`() =
        runTest {
            val expectedData = initializeData()
            val dataFromDb = simpleEntityDao.findAll()

            assertEquals(
                expectedData,
                dataFromDb
            )
        }

    @Test
    fun `isEmpty() should return true if no data is present in DB`() {
        assertTrue(simpleEntityDao.isEmpty())
    }

    @Test
    fun `isEmpty() should return false if we have some data in DB`() {
        initializeData()
        assertFalse(simpleEntityDao.isEmpty())
    }

    @Test
    fun `should create SQL query from builder`() {
        // given a simple query builder from DAO
        val sqLiteQuery = simpleEntityDao
            .createQueryBuilder()
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM entity_table entity_table
            """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun `should create SQL query from QB`() {
        // given a simple query builder from DAO
        val sqLiteQuery = simpleEntityDao
            .QB()
            .getQueryBuilder()
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM entity_table entity_table
            """.trimIndent(),
            sqLiteQuery.sql
        )
    }

    @Test
    fun `should create SQL query with selection and no arguments from QB`() {
        // given a simple query builder from DAO
        val sqLiteQuery = (simpleEntityDao
            .QB()
            .whereSelection("col = 1") as SimpleEntityDao.QB)
            .getQueryBuilder()
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM entity_table entity_table
            WHERE (col = 1)
            """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            0,
            sqLiteQuery.argCount
        )
    }

    @Test
    fun `should create SQL query with selection and arguments from QB`() {
        // given a simple query builder from DAO
        val sqLiteQuery = (simpleEntityDao
            .QB()
            .whereSelection(
                "col = ? OR col = ?",
                arrayOf(
                    12,
                    "some_args"
                )
            ) as SimpleEntityDao.QB)
            .getQueryBuilder()
            .build()

        // then
        assertNotNull(sqLiteQuery)
        assertEquals(
            """
            SELECT *
            FROM entity_table entity_table
            WHERE (col = ? OR col = ?)
            """.trimIndent(),
            sqLiteQuery.sql
        )
        assertEquals(
            2,
            sqLiteQuery.argCount
        )
    }

    private fun initializeData(): List<SimpleEntity> {
        return listOf(
            SimpleEntity(1),
            SimpleEntity(2),
            SimpleEntity(3)
        ).also {
            simpleEntityDao.insert(*it.toTypedArray())
        }
    }

    @Entity
    class InvalidEntity

    @Entity(
        tableName = SimpleEntity.TABLE_NAME,
        primaryKeys = [SimpleEntity.COLUMN_ID],
    )
    data class SimpleEntity(
        @ColumnInfo(name = COLUMN_ID) val id: Long,
    ) {
        companion object {
            const val TABLE_NAME = "entity_table"
            const val COLUMN_ID = BaseColumns._ID
        }
    }

    class InvalidEntityDao : BaseDao<InvalidEntity>() {
        override suspend fun findAll(query: SupportSQLiteQuery): List<InvalidEntity> {
            return emptyList()
        }

        override fun insert(vararg entity: InvalidEntity) {
        }

        override fun insertAll(entities: Iterable<InvalidEntity>) {
        }

        override fun insertOrIgnore(vararg entity: InvalidEntity) {
        }

        override fun query(query: SupportSQLiteQuery): Cursor {
            return MatrixCursor(emptyArray())
        }
    }

    @Dao
    abstract class SimpleEntityDao : BaseDao<SimpleEntity>() {
        inner class QB : BaseDao<SimpleEntity>.QB() {
            fun getQueryBuilder(): SQLiteSelectQueryBuilder {
                return selectQueryBuilder
            }
        }
    }

    @Database(
        entities = [SimpleEntity::class],
        version = 1,
        exportSchema = false
    )
    abstract class DummyDatabase : RoomDatabase() {
        abstract fun simpleEntityDao(): SimpleEntityDao
    }
}
