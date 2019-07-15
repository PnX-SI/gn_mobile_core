package fr.geonature.sync.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.AUTHORITY
import fr.geonature.commons.data.Provider.checkReadPermission
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.util.StringUtils

/**
 * Default ContentProvider implementation.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class MainContentProvider : ContentProvider() {

    override fun onCreate(): Boolean {
        return true
    }

    override fun getType(uri: Uri): String? {
        return when (MATCHER.match(uri)) {
            APP_SYNC_ID -> "vnd.android.cursor.item/" + AUTHORITY + "." + AppSync.TABLE_NAME
            INPUT_OBSERVERS -> "vnd.android.cursor.dir/" + AUTHORITY + "." + InputObserver.TABLE_NAME
            INPUT_OBSERVERS_IDS -> "vnd.android.cursor.dir/" + AUTHORITY + "." + InputObserver.TABLE_NAME
            INPUT_OBSERVER_ID -> "vnd.android.cursor.item/" + AUTHORITY + "." + InputObserver.TABLE_NAME
            TAXA -> "vnd.android.cursor.dir/" + AUTHORITY + "." + Taxon.TABLE_NAME
            TAXON_ID -> "vnd.android.cursor.item/" + AUTHORITY + "." + Taxon.TABLE_NAME
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun query(uri: Uri,
                       projection: Array<String>?,
                       selection: String?,
                       selectionArgs: Array<String>?,
                       sortOrder: String?): Cursor? {
        val context = context ?: return null

        if (!checkReadPermission(context,
                                 readPermission)) {
            throw SecurityException("Permission denial: require READ permission")
        }

        return when (MATCHER.match(uri)) {
            APP_SYNC_ID -> {
                val appSyncDao = AppSyncDao(context)
                val packageId = uri.lastPathSegment

                if (StringUtils.isEmpty(packageId)) {
                    throw IllegalArgumentException("Missing package ID for URI: $uri")
                }

                appSyncDao.findByPackageId(packageId!!)
            }

            INPUT_OBSERVERS -> {
                val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
                    .columns(projection)
                    .selection(selection,
                               selectionArgs)
                    .orderBy(sortOrder ?: "${InputObserver.COLUMN_LASTNAME} COLLATE NOCASE ASC")

                LocalDatabase.getInstance(context)
                    .inputObserverDao()
                    .select(queryBuilder.create())
            }

            INPUT_OBSERVERS_IDS -> {
                val selectedObserverIds =
                    uri.lastPathSegment?.split(",")?.map { it.toLongOrNull() }?.filter { it != null }?.distinct()
                        ?: listOf()

                val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
                    .columns(projection)
                    .selection("${InputObserver.COLUMN_ID} IN (${selectedObserverIds.joinToString(",")})",
                               null)
                    .orderBy(sortOrder ?: "${InputObserver.COLUMN_LASTNAME} COLLATE NOCASE ASC")

                LocalDatabase.getInstance(context)
                    .inputObserverDao()
                    .select(queryBuilder.create())
            }

            INPUT_OBSERVER_ID -> {
                val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
                    .columns(projection)
                    .selection("${InputObserver.COLUMN_ID} = ?",
                               arrayOf(uri.lastPathSegment?.toLongOrNull()))
                    .orderBy(sortOrder ?: "${InputObserver.COLUMN_LASTNAME} COLLATE NOCASE ASC")

                LocalDatabase.getInstance(context)
                    .inputObserverDao()
                    .select(queryBuilder.create())
            }

            TAXA -> {
                val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxon.TABLE_NAME)
                    .columns(projection)
                    .selection(selection,
                               selectionArgs)
                    .orderBy(sortOrder ?: "${Taxon.COLUMN_NAME} COLLATE NOCASE ASC")

                LocalDatabase.getInstance(context)
                    .taxonDao()
                    .select(queryBuilder.create())
            }

            TAXON_ID -> {
                val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxon.TABLE_NAME)
                    .columns(projection)
                    .selection("${Taxon.COLUMN_ID} = ?",
                               arrayOf(uri.lastPathSegment?.toLongOrNull()))
                    .orderBy(sortOrder ?: "${Taxon.COLUMN_NAME} COLLATE NOCASE ASC")

                LocalDatabase.getInstance(context)
                    .taxonDao()
                    .select(queryBuilder.create())
            }

            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(uri: Uri,
                        values: ContentValues?): Uri? {
        throw NotImplementedError("'insert' operation not implemented")
    }

    override fun update(uri: Uri,
                        values: ContentValues?,
                        selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw NotImplementedError("'update' operation not implemented")
    }

    override fun delete(uri: Uri,
                        selection: String?,
                        selectionArgs: Array<String>?): Int {
        throw NotImplementedError("'delete' operation not implemented")
    }

    companion object {

        // used for the UriMacher
        const val APP_SYNC_ID = 1
        const val INPUT_OBSERVERS = 10
        const val INPUT_OBSERVERS_IDS = 11
        const val INPUT_OBSERVER_ID = 12
        const val TAXA = 20
        const val TAXON_ID = 21

        /**
         * The URI matcher.
         */
        @JvmStatic
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY,
                   AppSync.TABLE_NAME + "/*",
                   APP_SYNC_ID)
            addURI(AUTHORITY,
                   InputObserver.TABLE_NAME,
                   INPUT_OBSERVERS)
            addURI(AUTHORITY,
                   InputObserver.TABLE_NAME + "/*",
                   INPUT_OBSERVERS_IDS)
            addURI(AUTHORITY,
                   InputObserver.TABLE_NAME + "/#",
                   INPUT_OBSERVER_ID)
            addURI(AUTHORITY,
                   Taxon.TABLE_NAME,
                   TAXA)
            addURI(AUTHORITY,
                   Taxon.TABLE_NAME + "/#",
                   TAXON_ID)
        }
    }
}