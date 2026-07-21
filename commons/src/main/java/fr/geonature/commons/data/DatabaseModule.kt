package fr.geonature.commons.data

import android.content.Context
import android.os.Build
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.AdditionalFieldDao
import fr.geonature.commons.data.dao.AdditionalFieldDatasetDao
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.data.dao.CodeObjectDao
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.dao.DefaultNomenclatureDao
import fr.geonature.commons.data.dao.FieldValueDao
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTaxonomyDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.dao.TaxonAreaDao
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.commons.util.getDatabaseFolder
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.mountpoint.util.getFile
import org.tinylog.Logger
import javax.inject.Qualifier
import javax.inject.Singleton

/**
 * Content Provider authority name.
 */
@MustBeDocumented
@Qualifier
annotation class ContentProviderAuthority

/**
 * GeoNature module name.
 */
@MustBeDocumented
@Qualifier
annotation class GeoNatureModuleName

/**
 * Database module.
 *
 * @author S. Grimault
 */
@InstallIn(SingletonComponent::class)
@Module
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext appContext: Context): LocalDatabase {
        val localDatabase =  FileUtils.getDatabaseFolder(
            appContext,
            MountPoint.StorageType.INTERNAL
        ).getFile("data.db")

        Logger.info { "loading local database '${localDatabase.absolutePath}'..." }

        return Room
            .databaseBuilder(
                appContext,
                LocalDatabase::class.java,
                localDatabase.absolutePath
            )
            .fallbackToDestructiveMigration()
            .addCallback(onCreateTaxaFtsCallback)
            .build()
    }

    /**
     * @return The DAO for [AppSync].
     */
    @Provides
    fun provideAppsyncDao(@ApplicationContext appContext: Context): AppSyncDao {
        return AppSyncDao(appContext)
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.Dataset.TABLE_NAME] table.
     */
    @Provides
    fun provideDatasetDao(database: LocalDatabase): DatasetDao {
        return database.datasetDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.InputObserver.TABLE_NAME] table.
     */
    @Provides
    fun provideInputObserverDao(database: LocalDatabase): InputObserverDao {
        return database.inputObserverDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.Taxonomy.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonomyDao(database: LocalDatabase): TaxonomyDao {
        return database.taxonomyDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.Taxon.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonDao(database: LocalDatabase): TaxonDao {
        return database.taxonDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.TaxonArea.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonAreaDao(database: LocalDatabase): TaxonAreaDao {
        return database.taxonAreaDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.NomenclatureType.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureTypeDao(database: LocalDatabase): NomenclatureTypeDao {
        return database.nomenclatureTypeDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.Nomenclature.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureDao(database: LocalDatabase): NomenclatureDao {
        return database.nomenclatureDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.NomenclatureTaxonomy.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureTaxonomyDao(database: LocalDatabase): NomenclatureTaxonomyDao {
        return database.nomenclatureTaxonomyDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.DefaultNomenclature.TABLE_NAME] table.
     */
    @Provides
    fun provideDefaultNomenclatureDao(database: LocalDatabase): DefaultNomenclatureDao {
        return database.defaultNomenclatureDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.AdditionalField.TABLE_NAME] table.
     */
    @Provides
    fun provideAdditionalFieldDao(database: LocalDatabase): AdditionalFieldDao {
        return database.additionalFieldDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.AdditionalFieldDataset.TABLE_NAME] table.
     */
    @Provides
    fun provideAdditionalFieldDatasetDao(database: LocalDatabase): AdditionalFieldDatasetDao {
        return database.additionalFieldDatasetDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.CodeObject.TABLE_NAME] table.
     */
    @Provides
    fun provideCodeObjectDao(database: LocalDatabase): CodeObjectDao {
        return database.codeObjectDao()
    }

    /**
     * @return The DAO for the [fr.geonature.commons.data.entity.FieldValue.TABLE_NAME] table.
     */
    @Provides
    fun provideFieldValueDao(database: LocalDatabase): FieldValueDao {
        return database.fieldValueDao()
    }
}

val onCreateTaxaFtsCallback = object : RoomDatabase.Callback() {
    override fun onOpen(db: SupportSQLiteDatabase) {
        super.onOpen(db)

        // use FTS4 with 'unicode61' as tokenizer and remove diacritics=2 only for API 30+
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val hasFTSTables = db
                .query("SELECT COUNT(*) FROM sqlite_master WHERE type='table' AND name='taxa_fts'")
                .let {
                    if (!it.moveToFirst()) {
                        it.close()
                        false
                    } else {
                        val hasFTSTables = it.getInt(0) > 0
                        it.close()
                        hasFTSTables
                    }
                }

            if (!hasFTSTables) {
                Logger.info { "creating FTS tables..." }
            }

            db.execSQL(
                """
                CREATE VIRTUAL TABLE IF NOT EXISTS `taxa_fts`
                USING FTS4(
                    `_id` INTEGER NOT NULL,
                    `name` TEXT NOT NULL,
                    `name_common` TEXT,
                    `description` TEXT,
                    `kingdom` TEXT NOT NULL,
                    `group` TEXT NOT NULL,
                    tokenize=unicode61
                    `remove_diacritics=2`,
                    content=`taxa`
                )""".trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS
                    room_fts_content_sync_taxa_fts_BEFORE_UPDATE BEFORE UPDATE ON `taxa`
                    BEGIN
                        DELETE FROM `taxa_fts` WHERE `docid`=OLD.`rowid`;
                    END
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS
                    room_fts_content_sync_taxa_fts_BEFORE_DELETE BEFORE DELETE ON `taxa`
                    BEGIN
                        DELETE FROM `taxa_fts` WHERE `docid`=OLD.`rowid`;
                    END
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS
                    room_fts_content_sync_taxa_fts_AFTER_UPDATE AFTER UPDATE ON `taxa`
                    BEGIN
                        INSERT INTO `taxa_fts`(`docid`, `_id`, `name`, `name_common`, `description`, `kingdom`, `group`)
                        VALUES (NEW.`rowid`, NEW.`_id`, NEW.`name`, NEW.`name_common`, NEW.`description`, NEW.`kingdom`, NEW.`group`);
                    END
                """.trimIndent()
            )
            db.execSQL(
                """
                CREATE TRIGGER IF NOT EXISTS
                    room_fts_content_sync_taxa_fts_AFTER_INSERT AFTER INSERT ON `taxa`
                    BEGIN
                        INSERT INTO `taxa_fts`(`docid`, `_id`, `name`, `name_common`, `description`, `kingdom`, `group`)
                        VALUES (NEW.`rowid`, NEW.`_id`, NEW.`name`, NEW.`name_common`, NEW.`description`, NEW.`kingdom`, NEW.`group`);
                    END
                """.trimIndent()
            )

            if (!hasFTSTables) {
                Logger.info { "FTS tables successfully created" }
            }
        }
    }
}