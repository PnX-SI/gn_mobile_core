package fr.geonature.sync.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.model.MountPoint
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.util.FileUtils.getDatabaseFolder
import fr.geonature.sync.util.FileUtils.getFile

/**
 * The Room database.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Database(entities = [InputObserver::class, Taxonomy::class, Taxon::class, TaxonArea::class],
          version = 6,
          exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {

    /**
     * @return The DAO for the 'observers' table.
     */
    abstract fun inputObserverDao(): InputObserverDao

    /**
     * @return The DAO for the 'Taxonomy' table.
     */
    abstract fun taxonomyDao(): TaxonomyDao

    /**
     * @return The DAO for the 'taxa' table.
     */
    abstract fun taxonDao(): TaxonDao

    /**
     * @return The DAO for the 'taxa_area' table.
     */
    abstract fun taxonAreaDao(): TaxonAreaDao

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
            val localDatabase = getFile(getDatabaseFolder(context,
                                                          MountPoint.StorageType.INTERNAL),
                                        "data.db")

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "Loading local database '" + localDatabase.absolutePath + "'...")
            }

            return Room.databaseBuilder(context.applicationContext,
                                        LocalDatabase::class.java,
                                        localDatabase.absolutePath)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}