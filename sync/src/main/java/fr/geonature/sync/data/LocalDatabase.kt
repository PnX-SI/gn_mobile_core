package fr.geonature.sync.data

import android.content.Context
import android.util.Log
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.model.MountPoint
import fr.geonature.sync.BuildConfig
import fr.geonature.sync.util.FileUtils.getDatabaseFolder
import fr.geonature.sync.util.FileUtils.getFile

/**
 * The Room database.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@Database(entities = [InputObserver::class, Taxon::class],
          version = 3,
          exportSchema = false)
abstract class LocalDatabase : RoomDatabase() {

    /**
     * @return The DAO for the 'observers' table.
     */
    abstract fun inputObserverDao(): InputObserverDao

    /**
     * @return The DAO for the 'taxon' table.
     */
    abstract fun taxonDao(): TaxonDao

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
        @Synchronized
        fun getInstance(context: Context): LocalDatabase {
            if (INSTANCE == null) {
                INSTANCE = buildInstance(context)
            }

            return INSTANCE!!
        }

        private fun buildInstance(context: Context): LocalDatabase {
            val localDatabase = getFile(getDatabaseFolder(context,
                                                          MountPoint.StorageType.INTERNAL),
                // TODO: fetch database name from loaded settings
                                        "data.db")

            if (BuildConfig.DEBUG) {
                Log.d(TAG,
                      "Loading local database '" + localDatabase.absolutePath + "'...")
            }

            return Room.databaseBuilder(context.applicationContext,
                                        LocalDatabase::class.java,
                                        localDatabase.absolutePath)
                .build()
        }
    }
}