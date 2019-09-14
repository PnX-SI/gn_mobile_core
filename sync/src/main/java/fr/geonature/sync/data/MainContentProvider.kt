package fr.geonature.sync.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.AUTHORITY
import fr.geonature.commons.data.Provider.checkReadPermission
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.TaxonArea
import fr.geonature.commons.data.TaxonWithArea
import fr.geonature.commons.data.Taxonomy

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
            APP_SYNC_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${AppSync.TABLE_NAME}"
            INPUT_OBSERVERS, INPUT_OBSERVERS_IDS -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            INPUT_OBSERVER_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            TAXA -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXON_ID, TAXON_AREA_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXONOMY, TAXONOMY_KINGDOM -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
            TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
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
            APP_SYNC_ID -> appSyncIdQuery(context,
                                          uri)
            INPUT_OBSERVERS -> inputObserversQuery(context,
                                                   projection,
                                                   selection,
                                                   selectionArgs,
                                                   sortOrder)
            INPUT_OBSERVERS_IDS -> inputObserversIdsQuery(context,
                                                          uri,
                                                          projection,
                                                          sortOrder)
            INPUT_OBSERVER_ID -> inputObserverIdQuery(context,
                                                      uri,
                                                      projection)
            TAXA -> taxaQuery(context,
                              projection,
                              selection,
                              selectionArgs,
                              sortOrder)
            TAXA_AREA -> taxaAreaQuery(context,
                                       uri,
                                       projection,
                                       selection,
                                       selectionArgs,
                                       sortOrder)
            TAXON_ID -> taxonIdQuery(context,
                                     uri,
                                     projection)
            TAXON_AREA_ID -> taxonAreaIdQuery(context,
                                              uri,
                                              projection)
            TAXONOMY, TAXONOMY_KINGDOM, TAXONOMY_KINGDOM_GROUP -> taxonomyQuery(context,
                                                                                uri,
                                                                                projection)
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

    private fun appSyncIdQuery(context: Context,
                               uri: Uri): Cursor {

        val appSyncDao = AppSyncDao(context)
        val packageId = uri.lastPathSegment

        return appSyncDao.findByPackageId(packageId)
    }

    private fun inputObserversQuery(context: Context,
                                    projection: Array<String>?,
                                    selection: String?,
                                    selectionArgs: Array<String>?,
                                    sortOrder: String?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
            .columns(projection ?: InputObserver.DEFAULT_PROJECTION)
            .selection(selection,
                       selectionArgs)
            .orderBy(sortOrder ?: "${InputObserver.COLUMN_LASTNAME} COLLATE NOCASE ASC")

        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .select(queryBuilder.create())
    }

    private fun inputObserversIdsQuery(context: Context,
                                       uri: Uri,
                                       projection: Array<String>?,
                                       sortOrder: String?): Cursor {

        val selectedObserverIds = uri.lastPathSegment?.split(",")?.mapNotNull { it.toLongOrNull() }?.distinct()
            ?: listOf()

        val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
            .columns(projection ?: InputObserver.DEFAULT_PROJECTION)
            .selection("${InputObserver.COLUMN_ID} IN (${selectedObserverIds.joinToString(",")})",
                       null)
            .orderBy(sortOrder ?: "${InputObserver.COLUMN_LASTNAME} COLLATE NOCASE ASC")

        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .select(queryBuilder.create())
    }

    private fun inputObserverIdQuery(context: Context,
                                     uri: Uri,
                                     projection: Array<String>?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(InputObserver.TABLE_NAME)
            .columns(projection ?: InputObserver.DEFAULT_PROJECTION)
            .selection("${InputObserver.COLUMN_ID} = ?",
                       arrayOf(uri.lastPathSegment?.toLongOrNull()))

        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .select(queryBuilder.create())
    }

    private fun taxaQuery(context: Context,
                          projection: Array<String>?,
                          selection: String?,
                          selectionArgs: Array<String>?,
                          sortOrder: String?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxon.TABLE_NAME)
            .columns(projection ?: AbstractTaxon.DEFAULT_PROJECTION)
            .selection(selection,
                       selectionArgs)
            .orderBy(sortOrder ?: "${AbstractTaxon.COLUMN_NAME} COLLATE NOCASE ASC")

        return LocalDatabase.getInstance(context)
            .taxonDao()
            .select(queryBuilder.create())
    }

    private fun taxaAreaQuery(context: Context,
                              uri: Uri,
                              projection: Array<String>?,
                              selection: String?,
                              selectionArgs: Array<String>?,
                              sortOrder: String?): Cursor {

        val bindArgs = mutableListOf<Any?>()

        val defaultProjection = projection
            ?: TaxonWithArea.DEFAULT_PROJECTION.asSequence().filter { column -> TaxonWithArea.DEFAULT_PROJECTION.any { it === column } }.map {
                when (it) {
                    AbstractTaxon.COLUMN_ID, AbstractTaxon.COLUMN_NAME, AbstractTaxon.COLUMN_DESCRIPTION, AbstractTaxon.COLUMN_HERITAGE -> "t.$it"
                    TaxonArea.COLUMN_TAXON_ID, TaxonArea.COLUMN_AREA_ID, TaxonArea.COLUMN_COLOR, TaxonArea.COLUMN_NUMBER_OF_OBSERVERS, TaxonArea.COLUMN_LAST_UPDATED_AT -> "ta.$it"
                    else -> it
                }
            }.joinToString(", ")

        val filterOnArea = uri.lastPathSegment?.toLongOrNull()
        val joinFilterOnAreaClause = if (filterOnArea == null) {
            ""
        }
        else {
            bindArgs.add(filterOnArea)
            "LEFT JOIN ${TaxonArea.TABLE_NAME} ta ON ta.${TaxonArea.COLUMN_TAXON_ID} = t.${AbstractTaxon.COLUMN_ID} AND ta.${TaxonArea.COLUMN_AREA_ID} = ?"
        }

        val whereClause = if (selection == null) "" else "WHERE $selection"
        val orderBy = sortOrder ?: "t.${AbstractTaxon.COLUMN_NAME} COLLATE NOCASE ASC"

        val sql = """
            SELECT $defaultProjection
            FROM ${Taxon.TABLE_NAME} t
            $joinFilterOnAreaClause
            $whereClause
            ORDER BY $orderBy
            """

        return LocalDatabase.getInstance(context)
            .taxonAreaDao()
            .select(SimpleSQLiteQuery(sql,
                                      bindArgs.also {
                                          it.addAll(selectionArgs?.asList() ?: emptyList())
                                      }.toTypedArray()))
    }

    private fun taxonIdQuery(context: Context,
                             uri: Uri,
                             projection: Array<String>?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxon.TABLE_NAME)
            .columns(projection ?: AbstractTaxon.DEFAULT_PROJECTION)
            .selection("${AbstractTaxon.COLUMN_ID} = ?",
                       arrayOf(uri.lastPathSegment?.toLongOrNull()))

        return LocalDatabase.getInstance(context)
            .taxonDao()
            .select(queryBuilder.create())
    }

    private fun taxonAreaIdQuery(context: Context,
                                 uri: Uri,
                                 projection: Array<String>?): Cursor {
        val bindArgs = mutableListOf<Any?>()

        val defaultProjection = projection
            ?: TaxonWithArea.DEFAULT_PROJECTION.asSequence().filter { column -> TaxonWithArea.DEFAULT_PROJECTION.any { it === column } }.map {
                when (it) {
                    AbstractTaxon.COLUMN_ID, AbstractTaxon.COLUMN_NAME, AbstractTaxon.COLUMN_DESCRIPTION, AbstractTaxon.COLUMN_HERITAGE -> "t.$it"
                    TaxonArea.COLUMN_TAXON_ID, TaxonArea.COLUMN_AREA_ID, TaxonArea.COLUMN_COLOR, TaxonArea.COLUMN_NUMBER_OF_OBSERVERS, TaxonArea.COLUMN_LAST_UPDATED_AT -> "ta.$it"
                    else -> it
                }
            }.joinToString(", ")

        val filterOnArea = uri.lastPathSegment?.toLongOrNull()
        val joinFilterOnAreaClause = if (filterOnArea == null) {
            ""
        }
        else {
            bindArgs.add(filterOnArea)
            "LEFT JOIN ${TaxonArea.TABLE_NAME} ta ON ta.${TaxonArea.COLUMN_TAXON_ID} = t.${AbstractTaxon.COLUMN_ID} AND ta.${TaxonArea.COLUMN_AREA_ID} = ?"
        }

        val taxonId = uri.pathSegments.asSequence()
            .map { it.toLongOrNull() }
            .filterNotNull()
            .firstOrNull()
        bindArgs.add(taxonId)

        val whereClause = "WHERE ${AbstractTaxon.COLUMN_ID} = ?"

        val sql = """
            SELECT $defaultProjection
            FROM ${Taxon.TABLE_NAME} t
            $joinFilterOnAreaClause
            $whereClause
            """

        return LocalDatabase.getInstance(context)
            .taxonAreaDao()
            .select(SimpleSQLiteQuery(sql,
                                      bindArgs.toTypedArray()))
    }

    private fun taxonomyQuery(context: Context,
                              uri: Uri,
                              projection: Array<String>?): Cursor {

        val lastPathSegments = uri.pathSegments.drop(uri.pathSegments.indexOf(Taxonomy.TABLE_NAME) + 1)
            .take(2)
        val selection = if (lastPathSegments.isEmpty()) "" else if (lastPathSegments.size == 1) "${Taxonomy.COLUMN_KINGDOM} LIKE ?" else "${Taxonomy.COLUMN_KINGDOM} = ? AND ${Taxonomy.COLUMN_GROUP} LIKE ?"

        val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxonomy.TABLE_NAME)
            .columns(projection ?: Taxonomy.DEFAULT_PROJECTION)

        if (selection.isNotEmpty()) {
            queryBuilder.selection(selection,
                                   lastPathSegments.toTypedArray())
        }

        return LocalDatabase.getInstance(context)
            .taxonomyDao()
            .select(queryBuilder.create())
    }

    companion object {

        // used for the UriMatcher
        const val APP_SYNC_ID = 1
        const val INPUT_OBSERVERS = 10
        const val INPUT_OBSERVERS_IDS = 11
        const val INPUT_OBSERVER_ID = 12
        const val TAXA = 20
        const val TAXA_AREA = 21
        const val TAXON_ID = 22
        const val TAXON_AREA_ID = 23
        const val TAXONOMY = 30
        const val TAXONOMY_KINGDOM = 31
        const val TAXONOMY_KINGDOM_GROUP = 32

        const val VND_TYPE_DIR_PREFIX = "vnd.android.cursor.dir"
        const val VND_TYPE_ITEM_PREFIX = "vnd.android.cursor.item"

        /**
         * The URI matcher.
         */
        @JvmStatic
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(AUTHORITY,
                   "${AppSync.TABLE_NAME}/*",
                   APP_SYNC_ID)
            addURI(AUTHORITY,
                   InputObserver.TABLE_NAME,
                   INPUT_OBSERVERS)
            addURI(AUTHORITY,
                   "${InputObserver.TABLE_NAME}/*",
                   INPUT_OBSERVERS_IDS)
            addURI(AUTHORITY,
                   "${InputObserver.TABLE_NAME}/#",
                   INPUT_OBSERVER_ID)
            addURI(AUTHORITY,
                   Taxon.TABLE_NAME,
                   TAXA)
            addURI(AUTHORITY,
                   "${Taxon.TABLE_NAME}/area/#",
                   TAXA_AREA)
            addURI(AUTHORITY,
                   "${Taxon.TABLE_NAME}/#",
                   TAXON_ID)
            addURI(AUTHORITY,
                   "${Taxon.TABLE_NAME}/#/area/#",
                   TAXON_AREA_ID)
            addURI(AUTHORITY,
                   Taxonomy.TABLE_NAME,
                   TAXONOMY)
            addURI(AUTHORITY,
                   "${Taxonomy.TABLE_NAME}/*",
                   TAXONOMY_KINGDOM)
            addURI(AUTHORITY,
                   "${Taxonomy.TABLE_NAME}/*/*",
                   TAXONOMY_KINGDOM_GROUP)
        }
    }
}