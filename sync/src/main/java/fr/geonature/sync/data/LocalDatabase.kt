package fr.geonature.sync.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.geonature.commons.data.Dataset
import fr.geonature.commons.data.DefaultNomenclature
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureTaxonomy
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.util.getDatabaseFolder
import fr.geonature.mountpoint.model.MountPoint.StorageType.INTERNAL
import fr.geonature.mountpoint.util.FileUtils
import fr.geonature.mountpoint.util.FileUtils.getFile
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.data.dao.DatasetDao
import fr.geonature.sync.data.dao.DefaultNomenclatureDao
import fr.geonature.sync.data.dao.InputObserverDao
import fr.geonature.sync.data.dao.NomenclatureDao
import fr.geonature.sync.data.dao.NomenclatureTaxonomyDao
import fr.geonature.sync.data.dao.NomenclatureTypeDao
import fr.geonature.sync.data.dao.TaxonAreaDao
import fr.geonature.sync.data.dao.TaxonDao
import fr.geonature.sync.data.dao.TaxonomyDao

/**
 * The Room database.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Database(
    entities = [
        Dataset::class,
        InputObserver::class,
        Taxonomy::class,
        Taxon::class,
        TaxonArea::class,
        NomenclatureType::class,
        Nomenclature::class,
        NomenclatureTaxonomy::class,
        DefaultNomenclature::class
    ],
    version = 17,
    exportSchema = false
)
abstract class LocalDatabase : RoomDatabase() {

    /**
     * @return The DAO for the [Dataset.TABLE_NAME] table.
     */
    abstract fun datasetDao(): DatasetDao

    /**
     * @return The DAO for the [InputObserver.TABLE_NAME] table.
     */
    abstract fun inputObserverDao(): InputObserverDao

    /**
     * @return The DAO for the [Taxonomy.TABLE_NAME] table.
     */
    abstract fun taxonomyDao(): TaxonomyDao

    /**
     * @return The DAO for the [Taxon.TABLE_NAME] table.
     */
    abstract fun taxonDao(): TaxonDao

    /**
     * @return The DAO for the [TaxonArea.TABLE_NAME] table.
     */
    abstract fun taxonAreaDao(): TaxonAreaDao

    /**
     * @return The DAO for the [NomenclatureType.TABLE_NAME] table.
     */
    abstract fun nomenclatureTypeDao(): NomenclatureTypeDao

    /**
     * @return The DAO for the [Nomenclature.TABLE_NAME] table.
     */
    abstract fun nomenclatureDao(): NomenclatureDao

    /**
     * @return The DAO for the [NomenclatureTaxonomy.TABLE_NAME] table.
     */
    abstract fun nomenclatureTaxonomyDao(): NomenclatureTaxonomyDao

    /**
     * @return The DAO for the [DefaultNomenclature.TABLE_NAME] table.
     */
    abstract fun defaultNomenclatureDao(): DefaultNomenclatureDao

    companion object {

        private val TAG = LocalDatabase::class.java.name

        /**
         * The only instance
         */
        @Volatile
        private var INSTANCE: LocalDatabase? = null

        /**
         * Gets the singleton instance of [LocalDatabase].
         *
         * @param context The context.
         *
         * @return The singleton instance of [LocalDatabase].
         */
        fun getInstance(context: Context): LocalDatabase = INSTANCE ?: synchronized(this) {
            INSTANCE ?: buildDatabase(context).also { INSTANCE = it }
        }

        private fun buildDatabase(context: Context): LocalDatabase {
            val localDatabase = getFile(
                FileUtils.getDatabaseFolder(
                    context,
                    INTERNAL
                ),
                "data.db"
            )

            if (BuildConfig.DEBUG) {
                Log.d(
                    TAG,
                    "Loading local database '${localDatabase.absolutePath}'..."
                )
            }

            return Room.databaseBuilder(
                context.applicationContext,
                LocalDatabase::class.java,
                localDatabase.absolutePath
            )
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}
