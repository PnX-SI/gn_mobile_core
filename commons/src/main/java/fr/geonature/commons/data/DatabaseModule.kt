package fr.geonature.commons.data

import android.content.Context
import androidx.room.Room
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.dao.DefaultNomenclatureDao
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTaxonomyDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.dao.TaxonAreaDao
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.commons.util.getDatabaseFolder
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils
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
        val localDatabase = FileUtils.getFile(
            FileUtils.getDatabaseFolder(
                appContext,
                MountPoint.StorageType.INTERNAL
            ),
            "data.db"
        )

        Logger.info { "loading local database '${localDatabase.absolutePath}'..." }

        return Room
            .databaseBuilder(
                appContext,
                LocalDatabase::class.java,
                localDatabase.absolutePath
            )
            .fallbackToDestructiveMigration()
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
     * @return The DAO for the [Dataset.TABLE_NAME] table.
     */
    @Provides
    fun provideDatasetDao(database: LocalDatabase): DatasetDao {
        return database.datasetDao()
    }

    /**
     * @return The DAO for the [InputObserver.TABLE_NAME] table.
     */
    @Provides
    fun provideInputObserverDao(database: LocalDatabase): InputObserverDao {
        return database.inputObserverDao()
    }

    /**
     * @return The DAO for the [Taxonomy.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonomyDao(database: LocalDatabase): TaxonomyDao {
        return database.taxonomyDao()
    }

    /**
     * @return The DAO for the [Taxon.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonDao(database: LocalDatabase): TaxonDao {
        return database.taxonDao()
    }

    /**
     * @return The DAO for the [TaxonArea.TABLE_NAME] table.
     */
    @Provides
    fun provideTaxonAreaDao(database: LocalDatabase): TaxonAreaDao {
        return database.taxonAreaDao()
    }

    /**
     * @return The DAO for the [NomenclatureType.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureTypeDao(database: LocalDatabase): NomenclatureTypeDao {
        return database.nomenclatureTypeDao()
    }

    /**
     * @return The DAO for the [Nomenclature.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureDao(database: LocalDatabase): NomenclatureDao {
        return database.nomenclatureDao()
    }

    /**
     * @return The DAO for the [NomenclatureTaxonomy.TABLE_NAME] table.
     */
    @Provides
    fun provideNomenclatureTaxonomyDao(database: LocalDatabase): NomenclatureTaxonomyDao {
        return database.nomenclatureTaxonomyDao()
    }

    /**
     * @return The DAO for the [DefaultNomenclature.TABLE_NAME] table.
     */
    @Provides
    fun provideDefaultNomenclatureDao(database: LocalDatabase): DefaultNomenclatureDao {
        return database.defaultNomenclatureDao()
    }
}