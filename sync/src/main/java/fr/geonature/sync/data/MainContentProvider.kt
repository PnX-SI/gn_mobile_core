package fr.geonature.sync.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import fr.geonature.commons.data.AppSync
import fr.geonature.commons.data.Dataset
import fr.geonature.commons.data.DefaultNomenclature
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Nomenclature
import fr.geonature.commons.data.NomenclatureType
import fr.geonature.commons.data.Taxon
import fr.geonature.commons.data.Taxonomy
import fr.geonature.commons.data.helper.Provider.AUTHORITY
import fr.geonature.commons.data.helper.Provider.checkReadPermission
import fr.geonature.sync.data.dao.AppSyncDao

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
            DATASET_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Dataset.TABLE_NAME}"
            INPUT_OBSERVERS, INPUT_OBSERVERS_IDS -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            INPUT_OBSERVER_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${InputObserver.TABLE_NAME}"
            TAXA, TAXA_AREA -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXON_ID, TAXON_AREA_ID -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxon.TABLE_NAME}"
            TAXONOMY, TAXONOMY_KINGDOM -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
            TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_ITEM_PREFIX/$AUTHORITY.${Taxonomy.TABLE_NAME}"
            NOMENCLATURE_TYPES -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${NomenclatureType.TABLE_NAME}"
            NOMENCLATURE_TYPES_DEFAULT -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${DefaultNomenclature.TABLE_NAME}"
            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_DIR_PREFIX/$AUTHORITY.${Nomenclature.TABLE_NAME}"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor? {
        val context = context ?: return null

        if (!checkReadPermission(
                context,
                readPermission
            )
        ) {
            throw SecurityException("Permission denial: require READ permission")
        }

        return when (MATCHER.match(uri)) {
            APP_SYNC_ID -> appSyncByPackageIdQuery(
                context,
                uri
            )
            DATASET, DATASET_ACTIVE -> datasetQuery(
                context,
                uri
            )
            DATASET_ID -> datasetByIdQuery(
                context,
                uri
            )
            INPUT_OBSERVERS -> inputObserversQuery(
                context,
                selection,
                selectionArgs
            )
            INPUT_OBSERVERS_IDS -> inputObserversByIdsQuery(
                context,
                uri
            )
            INPUT_OBSERVER_ID -> inputObserverByIdQuery(
                context,
                uri
            )
            TAXONOMY, TAXONOMY_KINGDOM, TAXONOMY_KINGDOM_GROUP -> taxonomyQuery(
                context,
                uri
            )
            TAXA -> taxaQuery(
                context,
                selection,
                selectionArgs
            )
            TAXON_ID -> taxonByIdQuery(
                context,
                uri
            )
            TAXA_AREA -> taxaWithAreaQuery(
                context,
                uri,
                selection,
                selectionArgs
            )
            TAXON_AREA_ID -> taxonWithAreaByIdQuery(
                context,
                uri
            )
            NOMENCLATURE_TYPES -> nomenclatureTypesQuery(context)
            NOMENCLATURE_TYPES_DEFAULT -> defaultNomenclaturesByModule(
                context,
                uri
            )
            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> nomenclaturesWithTaxonomyQuery(
                context,
                uri
            )
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        throw NotImplementedError("'insert' operation not implemented")
    }

    override fun update(
        uri: Uri,
        values: ContentValues?,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw NotImplementedError("'update' operation not implemented")
    }

    override fun delete(
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Int {
        throw NotImplementedError("'delete' operation not implemented")
    }

    private fun appSyncByPackageIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val appSyncDao = AppSyncDao(context)
        val packageId = uri.lastPathSegment

        return appSyncDao.findByPackageId(packageId)
    }

    private fun datasetQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val module =
            uri.pathSegments
                .drop(uri.pathSegments.indexOf(Dataset.TABLE_NAME) + 1)
                .take(1)
                .firstOrNull()
                ?.substringAfterLast(".")

        val onlyActive = uri.lastPathSegment == "active"

        return LocalDatabase.getInstance(context)
            .datasetDao()
            .QB()
            .whereModule(module)
            .also {
                if (onlyActive) {
                    it.whereActive()
                }
            }
            .cursor()
    }

    private fun datasetByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val module =
            uri.pathSegments
                .drop(uri.pathSegments.indexOf(Dataset.TABLE_NAME) + 1)
                .take(1)
                .firstOrNull()
                ?.substringAfterLast(".")

        return LocalDatabase.getInstance(context)
            .datasetDao()
            .QB()
            .whereModule(module)
            .whereId(uri.lastPathSegment?.toLongOrNull())
            .cursor()
    }

    private fun inputObserversQuery(
        context: Context,
        selection: String?,
        selectionArgs: Array<String>?
    ): Cursor {
        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .QB()
            .whereSelection(
                selection,
                arrayOf(*selectionArgs ?: emptyArray())
            )
            .cursor()
    }

    private fun inputObserversByIdsQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val selectedObserverIds =
            uri.lastPathSegment?.split(",")
                ?.mapNotNull { it.toLongOrNull() }
                ?.distinct()
                ?.toLongArray()
                ?: longArrayOf()

        if (selectedObserverIds.size == 1) {
            return inputObserverByIdQuery(
                context,
                uri
            )
        }

        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .QB()
            .whereIdsIn(*selectedObserverIds)
            .cursor()
    }

    private fun inputObserverByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        return LocalDatabase.getInstance(context)
            .inputObserverDao()
            .QB()
            .whereId(uri.lastPathSegment?.toLongOrNull())
            .cursor()
    }

    private fun taxonomyQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val lastPathSegments =
            uri.pathSegments
                .drop(uri.pathSegments.indexOf(Taxonomy.TABLE_NAME) + 1)
                .take(2)

        return LocalDatabase.getInstance(context)
            .taxonomyDao()
            .QB()
            .also {
                when (lastPathSegments.size) {
                    1 -> it.whereKingdom(lastPathSegments[0])
                    2 -> it.whereKingdomAndGroup(
                        lastPathSegments[0],
                        lastPathSegments[1]
                    )
                    else -> return@also
                }
            }
            .cursor()
    }

    private fun taxaQuery(
        context: Context,
        selection: String?,
        selectionArgs: Array<String>?
    ): Cursor {
        return LocalDatabase.getInstance(context)
            .taxonDao()
            .QB()
            .whereSelection(
                selection,
                arrayOf(*selectionArgs ?: emptyArray())
            )
            .cursor()
    }

    private fun taxonByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        return LocalDatabase.getInstance(context)
            .taxonDao()
            .QB()
            .whereId(uri.lastPathSegment?.toLongOrNull())
            .cursor()
    }

    private fun taxaWithAreaQuery(
        context: Context,
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?
    ): Cursor {
        val filterOnArea = uri.lastPathSegment?.toLongOrNull()

        return LocalDatabase.getInstance(context)
            .taxonDao()
            .QB()
            .withArea(filterOnArea)
            .whereSelection(
                selection,
                arrayOf(*selectionArgs ?: emptyArray())
            )
            .cursor()
    }

    private fun taxonWithAreaByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val filterOnArea = uri.lastPathSegment?.toLongOrNull()
        val taxonId = uri.pathSegments
            .asSequence()
            .map { it.toLongOrNull() }
            .filterNotNull()
            .firstOrNull()

        return LocalDatabase.getInstance(context)
            .taxonDao()
            .QB()
            .withArea(filterOnArea)
            .whereId(taxonId)
            .cursor()
    }

    private fun nomenclatureTypesQuery(context: Context): Cursor {
        return LocalDatabase.getInstance(context)
            .nomenclatureTypeDao()
            .QB()
            .cursor()
    }

    private fun defaultNomenclaturesByModule(
        context: Context,
        uri: Uri
    ): Cursor {
        val module =
            uri.pathSegments
                .drop(uri.pathSegments.indexOf(NomenclatureType.TABLE_NAME) + 1)
                .take(1)
                .firstOrNull()
                ?.substringAfterLast(".")

        return LocalDatabase.getInstance(context)
            .nomenclatureDao()
            .QB()
            .withNomenclatureType()
            .withDefaultNomenclature(module)
            .cursor()
    }

    private fun nomenclaturesWithTaxonomyQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val mnemonic =
            uri.pathSegments
                .drop(uri.pathSegments.indexOf(NomenclatureType.TABLE_NAME) + 1)
                .take(1)
                .firstOrNull()
        val lastPathSegments = uri.pathSegments
            .drop(uri.pathSegments.indexOf("items") + 1)
            .take(2)

        return LocalDatabase.getInstance(context)
            .nomenclatureDao()
            .QB()
            .withNomenclatureType(mnemonic)
            .withTaxonomy(
                lastPathSegments.getOrNull(0),
                lastPathSegments.getOrNull(1)
            )
            .cursor()
    }

    companion object {

        // used for the UriMatcher
        const val APP_SYNC_ID = 1
        const val DATASET = 10
        const val DATASET_ACTIVE = 11
        const val DATASET_ID = 12
        const val INPUT_OBSERVERS = 20
        const val INPUT_OBSERVERS_IDS = 21
        const val INPUT_OBSERVER_ID = 22
        const val TAXONOMY = 30
        const val TAXONOMY_KINGDOM = 31
        const val TAXONOMY_KINGDOM_GROUP = 32
        const val TAXA = 40
        const val TAXON_ID = 41
        const val TAXA_AREA = 42
        const val TAXON_AREA_ID = 43
        const val NOMENCLATURE_TYPES = 50
        const val NOMENCLATURE_TYPES_DEFAULT = 51
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM = 52
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP = 53

        const val VND_TYPE_DIR_PREFIX = "vnd.android.cursor.dir"
        const val VND_TYPE_ITEM_PREFIX = "vnd.android.cursor.item"

        /**
         * The URI matcher.
         */
        @JvmStatic
        private val MATCHER = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(
                AUTHORITY,
                "${AppSync.TABLE_NAME}/*",
                APP_SYNC_ID
            )
            addURI(
                AUTHORITY,
                "${Dataset.TABLE_NAME}/*",
                DATASET
            )
            addURI(
                AUTHORITY,
                "${Dataset.TABLE_NAME}/*/active",
                DATASET_ACTIVE
            )
            addURI(
                AUTHORITY,
                "${Dataset.TABLE_NAME}/*/#",
                DATASET_ID
            )
            addURI(
                AUTHORITY,
                InputObserver.TABLE_NAME,
                INPUT_OBSERVERS
            )
            addURI(
                AUTHORITY,
                "${InputObserver.TABLE_NAME}/*",
                INPUT_OBSERVERS_IDS
            )
            addURI(
                AUTHORITY,
                "${InputObserver.TABLE_NAME}/#",
                INPUT_OBSERVER_ID
            )
            addURI(
                AUTHORITY,
                Taxonomy.TABLE_NAME,
                TAXONOMY
            )
            addURI(
                AUTHORITY,
                "${Taxonomy.TABLE_NAME}/*",
                TAXONOMY_KINGDOM
            )
            addURI(
                AUTHORITY,
                "${Taxonomy.TABLE_NAME}/*/*",
                TAXONOMY_KINGDOM_GROUP
            )
            addURI(
                AUTHORITY,
                Taxon.TABLE_NAME,
                TAXA
            )
            addURI(
                AUTHORITY,
                "${Taxon.TABLE_NAME}/area/#",
                TAXA_AREA
            )
            addURI(
                AUTHORITY,
                "${Taxon.TABLE_NAME}/#",
                TAXON_ID
            )
            addURI(
                AUTHORITY,
                "${Taxon.TABLE_NAME}/#/area/#",
                TAXON_AREA_ID
            )
            addURI(
                AUTHORITY,
                NomenclatureType.TABLE_NAME,
                NOMENCLATURE_TYPES
            )
            addURI(
                AUTHORITY,
                "${NomenclatureType.TABLE_NAME}/*/default",
                NOMENCLATURE_TYPES_DEFAULT
            )
            addURI(
                AUTHORITY,
                "${NomenclatureType.TABLE_NAME}/*/items/*",
                NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM
            )
            addURI(
                AUTHORITY,
                "${NomenclatureType.TABLE_NAME}/*/items/*/*",
                NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP
            )
        }
    }
}
