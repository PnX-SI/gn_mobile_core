package fr.geonature.sync.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.text.TextUtils
import androidx.sqlite.db.SimpleSQLiteQuery
import androidx.sqlite.db.SupportSQLiteQueryBuilder
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.Dataset
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureTaxonomy
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.NomenclatureWithTaxonomy
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
            DATASET, DATASET_ACTIVE -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Dataset.TABLE_NAME}"
            INPUT_OBSERVERS, INPUT_OBSERVERS_IDS -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            INPUT_OBSERVER_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            TAXA -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXON_ID, TAXON_AREA_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXONOMY, TAXONOMY_KINGDOM -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
            TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
            NOMENCLATURE_TYPES -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${NomenclatureType.TABLE_NAME}"
            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Nomenclature.TABLE_NAME}"
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
            DATASET, DATASET_ACTIVE -> datasetQuery(context,
                                                    uri,
                                                    projection,
                                                    sortOrder)
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
            TAXONOMY, TAXONOMY_KINGDOM, TAXONOMY_KINGDOM_GROUP -> taxonomyQuery(context,
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
            NOMENCLATURE_TYPES -> nomenclatureTypesQuery(context,
                                                         projection,
                                                         selection,
                                                         selectionArgs,
                                                         sortOrder)
            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> nomenclaturesByTaxonomyQuery(context,
                                                                                                                           uri,
                                                                                                                           projection,
                                                                                                                           selection,
                                                                                                                           selectionArgs,
                                                                                                                           sortOrder)
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

    private fun datasetQuery(context: Context,
                             uri: Uri,
                             projection: Array<String>?,
                             sortOrder: String?): Cursor {
        val onlyActive = uri.lastPathSegment == "active"

        val queryBuilder = SupportSQLiteQueryBuilder.builder(Dataset.TABLE_NAME)
                .columns(projection ?: Dataset.DEFAULT_PROJECTION)
                .orderBy(sortOrder ?: "${Dataset.COLUMN_NAME} COLLATE NOCASE ASC")

        if (onlyActive) {
            queryBuilder.selection("${Dataset.COLUMN_ACTIVE} = 1",
                                   null)
        }

        return LocalDatabase.getInstance(context)
                .datasetDao()
                .select(queryBuilder.create())
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

    private fun taxaQuery(context: Context,
                          projection: Array<String>?,
                          selection: String?,
                          selectionArgs: Array<String>?,
                          sortOrder: String?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(Taxon.TABLE_NAME)
                .columns((projection
                    ?: AbstractTaxon.DEFAULT_PROJECTION).map { "\"$it\"" }.toTypedArray())
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

        val defaultProjection = (projection ?: TaxonWithArea.DEFAULT_PROJECTION).asSequence()
                .filter { column -> TaxonWithArea.DEFAULT_PROJECTION.any { it === column } }
                .map {
                    when (it) {
                        in AbstractTaxon.DEFAULT_PROJECTION -> "t.\"$it\""
                        in TaxonArea.DEFAULT_PROJECTION -> "ta.\"$it\""
                        else -> it
                    }
                }
                .joinToString(", ")

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
            """.trimIndent()

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
                .columns((projection
                    ?: AbstractTaxon.DEFAULT_PROJECTION).map { "\"$it\"" }.toTypedArray())
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

        val defaultProjection = (projection ?: TaxonWithArea.DEFAULT_PROJECTION).asSequence()
                .filter { column -> TaxonWithArea.DEFAULT_PROJECTION.any { it === column } }
                .map {
                    when (it) {
                        in AbstractTaxon.DEFAULT_PROJECTION -> "t.\"$it\""
                        in TaxonArea.DEFAULT_PROJECTION -> "ta.\"$it\""
                        else -> it
                    }
                }
                .joinToString(", ")

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
            """.trimIndent()

        return LocalDatabase.getInstance(context)
                .taxonAreaDao()
                .select(SimpleSQLiteQuery(sql,
                                          bindArgs.toTypedArray()))
    }

    private fun nomenclatureTypesQuery(context: Context,
                                       projection: Array<String>?,
                                       selection: String?,
                                       selectionArgs: Array<String>?,
                                       sortOrder: String?): Cursor {

        val queryBuilder = SupportSQLiteQueryBuilder.builder(NomenclatureType.TABLE_NAME)
                .columns(projection ?: NomenclatureType.DEFAULT_PROJECTION)
                .selection(selection,
                           selectionArgs)
                .orderBy(sortOrder ?: "${NomenclatureType.COLUMN_MNEMONIC} COLLATE NOCASE ASC")

        return LocalDatabase.getInstance(context)
                .nomenclatureTypeDao()
                .select(queryBuilder.create())
    }

    private fun nomenclaturesByTaxonomyQuery(context: Context,
                                             uri: Uri,
                                             projection: Array<String>?,
                                             selection: String?,
                                             selectionArgs: Array<String>?,
                                             sortOrder: String?): Cursor {
        val bindArgs = mutableListOf<Any?>()

        val defaultProjection = (projection
            ?: NomenclatureWithTaxonomy.DEFAULT_PROJECTION).asSequence()
                .filter { column ->
                    NomenclatureWithTaxonomy.DEFAULT_PROJECTION.any { it === column }
                }
                .map {
                    when (it) {
                        NomenclatureType.COLUMN_MNEMONIC -> "nty.\"$it\""
                        in Nomenclature.DEFAULT_PROJECTION -> "n.\"$it\""
                        in Taxonomy.DEFAULT_PROJECTION -> "nta.\"$it\""
                        else -> it
                    }
                }
                .joinToString(", ")

        val taxonomyTypeMnemonic = uri.pathSegments.drop(uri.pathSegments.indexOf(NomenclatureType.TABLE_NAME) + 1)
                .take(1)
                .firstOrNull()
        val joinFilterOnNomenclatureTypeClause = if (TextUtils.isEmpty(taxonomyTypeMnemonic)) {
            ""
        }
        else {
            bindArgs.add(taxonomyTypeMnemonic)
            """
            JOIN ${NomenclatureType.TABLE_NAME} nty ON nty."${NomenclatureType.COLUMN_ID}" = n."${Nomenclature.COLUMN_TYPE_ID}" AND nty."${NomenclatureType.COLUMN_MNEMONIC}" = ?
            """.trimIndent()
        }

        val lastPathSegments = uri.pathSegments.drop(uri.pathSegments.indexOf("items") + 1)
                .take(2)
        val joinFilterOnTaxonomyClause = if (lastPathSegments.isEmpty()) {
            ""
        }
        else {
            bindArgs.addAll(lastPathSegments)

            """
            LEFT JOIN ${NomenclatureTaxonomy.TABLE_NAME} nta ON
                nta."${NomenclatureTaxonomy.COLUMN_NOMENCLATURE_ID}" = n."${Nomenclature.COLUMN_ID}"
                AND (nta."${Taxonomy.COLUMN_KINGDOM}" = ? OR nta."${Taxonomy.COLUMN_KINGDOM}" = "${Taxonomy.ANY}")
                ${if (lastPathSegments.size == 2) "AND (nta.\"${Taxonomy.COLUMN_GROUP}\" = ? OR nta.\"${Taxonomy.COLUMN_GROUP}\" = \"${Taxonomy.ANY}\")" else ""}
            """.trimIndent()
        }

        val whereClause = if (selection == null) "" else "WHERE $selection"
        val orderBy = sortOrder ?: "n.\"${Nomenclature.COLUMN_HIERARCHY}\" COLLATE NOCASE ASC"

        val sql = """
            SELECT $defaultProjection
            FROM ${Nomenclature.TABLE_NAME} n
            $joinFilterOnNomenclatureTypeClause
            $joinFilterOnTaxonomyClause
            $whereClause
            ORDER BY $orderBy
            """.trimIndent()

        return LocalDatabase.getInstance(context)
                .nomenclatureDao()
                .select(SimpleSQLiteQuery(sql,
                                          bindArgs.also {
                                              it.addAll(selectionArgs?.asList() ?: emptyList())
                                          }.toTypedArray()))
    }

    companion object {

        // used for the UriMatcher
        const val APP_SYNC_ID = 1
        const val DATASET = 10
        const val DATASET_ACTIVE = 11
        const val INPUT_OBSERVERS = 20
        const val INPUT_OBSERVERS_IDS = 21
        const val INPUT_OBSERVER_ID = 22
        const val TAXONOMY = 30
        const val TAXONOMY_KINGDOM = 31
        const val TAXONOMY_KINGDOM_GROUP = 32
        const val TAXA = 40
        const val TAXA_AREA = 41
        const val TAXON_ID = 42
        const val TAXON_AREA_ID = 43
        const val NOMENCLATURE_TYPES = 50
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM = 51
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP = 52

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
                   Dataset.TABLE_NAME,
                   DATASET)
            addURI(AUTHORITY,
                   "${Dataset.TABLE_NAME}/active",
                   DATASET_ACTIVE)
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
                   Taxonomy.TABLE_NAME,
                   TAXONOMY)
            addURI(AUTHORITY,
                   "${Taxonomy.TABLE_NAME}/*",
                   TAXONOMY_KINGDOM)
            addURI(AUTHORITY,
                   "${Taxonomy.TABLE_NAME}/*/*",
                   TAXONOMY_KINGDOM_GROUP)
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
                   NomenclatureType.TABLE_NAME,
                   NOMENCLATURE_TYPES)
            addURI(AUTHORITY,
                   "${NomenclatureType.TABLE_NAME}/*/items/*",
                   NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM)
            addURI(AUTHORITY,
                   "${NomenclatureType.TABLE_NAME}/*/items/*/*",
                   NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP)
        }
    }
}