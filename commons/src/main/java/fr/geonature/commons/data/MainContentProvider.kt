package fr.geonature.commons.data

import android.content.ContentProvider
import android.content.ContentValues
import android.content.Context
import android.content.UriMatcher
import android.database.Cursor
import android.net.Uri
import android.os.ParcelFileDescriptor
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors.fromApplication
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.AppSyncDao
import fr.geonature.commons.data.dao.DatasetDao
import fr.geonature.commons.data.dao.InputDao
import fr.geonature.commons.data.dao.InputObserverDao
import fr.geonature.commons.data.dao.NomenclatureDao
import fr.geonature.commons.data.dao.NomenclatureTypeDao
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.dao.TaxonomyDao
import fr.geonature.commons.data.entity.AppSync
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.Taxonomy
import fr.geonature.mountpoint.model.MountPoint
import fr.geonature.mountpoint.util.FileUtils
import org.tinylog.Logger
import java.io.File
import java.io.FileNotFoundException

/**
 * Default ContentProvider implementation.
 *
 * @author S. Grimault
 */
class MainContentProvider : ContentProvider() {

    @InstallIn(SingletonComponent::class)
    @EntryPoint
    interface MainContentProviderEntryPoint {
        fun appSyncDao(): AppSyncDao
        fun datasetDao(): DatasetDao
        fun inputObserverDao(): InputObserverDao
        fun taxonomyDao(): TaxonomyDao
        fun taxonDao(): TaxonDao
        fun nomenclatureTypeDao(): NomenclatureTypeDao
        fun nomenclatureDao(): NomenclatureDao
    }

    /**
     * Authority defined for this content provider.
     */
    private lateinit var authority: String

    /**
     * The URI matcher.
     */
    private lateinit var uriMatcher: UriMatcher

    override fun onCreate(): Boolean {
        val context = context
            ?: return false

        authority = "${context.packageName}.provider"
        uriMatcher = UriMatcher(UriMatcher.NO_MATCH).apply {
            addURI(
                authority,
                "${AppSync.TABLE_NAME}/*",
                APP_SYNC_ID
            )
            addURI(
                authority,
                "${Dataset.TABLE_NAME}/*",
                DATASET
            )
            addURI(
                authority,
                "${Dataset.TABLE_NAME}/*/active",
                DATASET_ACTIVE
            )
            addURI(
                authority,
                "${Dataset.TABLE_NAME}/*/#",
                DATASET_ID
            )
            addURI(
                authority,
                InputObserver.TABLE_NAME,
                INPUT_OBSERVERS
            )
            addURI(
                authority,
                "${InputObserver.TABLE_NAME}/*",
                INPUT_OBSERVERS_IDS
            )
            addURI(
                authority,
                "${InputObserver.TABLE_NAME}/#",
                INPUT_OBSERVER_ID
            )
            addURI(
                authority,
                Taxonomy.TABLE_NAME,
                TAXONOMY
            )
            addURI(
                authority,
                "${Taxonomy.TABLE_NAME}/*",
                TAXONOMY_KINGDOM
            )
            addURI(
                authority,
                "${Taxonomy.TABLE_NAME}/*/*",
                TAXONOMY_KINGDOM_GROUP
            )
            addURI(
                authority,
                Taxon.TABLE_NAME,
                TAXA
            )
            addURI(
                authority,
                "${Taxon.TABLE_NAME}/list/#",
                TAXA_LIST_ID
            )
            addURI(
                authority,
                "${Taxon.TABLE_NAME}/area/#",
                TAXA_AREA_ID
            )
            addURI(
                authority,
                "${Taxon.TABLE_NAME}/list/#/area/#",
                TAXA_LIST_AREA_ID
            )
            addURI(
                authority,
                "${Taxon.TABLE_NAME}/#",
                TAXON_ID
            )
            addURI(
                authority,
                "${Taxon.TABLE_NAME}/#/area/#",
                TAXON_AREA_ID
            )
            addURI(
                authority,
                NomenclatureType.TABLE_NAME,
                NOMENCLATURE_TYPES
            )
            addURI(
                authority,
                "${NomenclatureType.TABLE_NAME}/*/default",
                NOMENCLATURE_TYPES_DEFAULT
            )
            addURI(
                authority,
                "${NomenclatureType.TABLE_NAME}/*/items/*",
                NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM
            )
            addURI(
                authority,
                "${NomenclatureType.TABLE_NAME}/*/items/*/*",
                NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP
            )
            addURI(
                authority,
                "settings/*",
                SETTINGS
            )
            addURI(
                authority,
                "inputs/export",
                INPUTS_EXPORT
            )
            addURI(
                authority,
                "inputs/*/#",
                INPUT_ID
            )
        }

        return true
    }

    override fun getType(uri: Uri): String {
        return when (uriMatcher.match(uri)) {
            APP_SYNC_ID -> "$VND_TYPE_ITEM_PREFIX/$authority.${AppSync.TABLE_NAME}"
            DATASET, DATASET_ACTIVE -> "$VND_TYPE_DIR_PREFIX/$authority.${Dataset.TABLE_NAME}"
            DATASET_ID -> "$VND_TYPE_ITEM_PREFIX/$authority.${Dataset.TABLE_NAME}"
            INPUT_OBSERVERS, INPUT_OBSERVERS_IDS -> "$VND_TYPE_DIR_PREFIX/$authority.${InputObserver.TABLE_NAME}"
            INPUT_OBSERVER_ID -> "$VND_TYPE_ITEM_PREFIX/$authority.${InputObserver.TABLE_NAME}"
            TAXA, TAXA_LIST_ID, TAXA_AREA_ID, TAXA_LIST_AREA_ID -> "$VND_TYPE_DIR_PREFIX/$authority.${Taxon.TABLE_NAME}"
            TAXON_ID, TAXON_AREA_ID -> "$VND_TYPE_ITEM_PREFIX/$authority.${Taxon.TABLE_NAME}"
            TAXONOMY, TAXONOMY_KINGDOM -> "$VND_TYPE_DIR_PREFIX/$authority.${Taxonomy.TABLE_NAME}"
            TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_ITEM_PREFIX/$authority.${Taxonomy.TABLE_NAME}"
            NOMENCLATURE_TYPES -> "$VND_TYPE_DIR_PREFIX/$authority.${NomenclatureType.TABLE_NAME}"
            NOMENCLATURE_TYPES_DEFAULT -> "$VND_TYPE_DIR_PREFIX/$authority.${DefaultNomenclature.TABLE_NAME}"
            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> "$VND_TYPE_DIR_PREFIX/$authority.${Nomenclature.TABLE_NAME}"
            else -> throw IllegalArgumentException("Unknown URI: $uri")
        }
    }

    override fun query(
        uri: Uri,
        projection: Array<String>?,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val appContext = context?.applicationContext
            ?: throw IllegalStateException()

        return when (uriMatcher.match(uri)) {
            APP_SYNC_ID -> appSyncByPackageIdQuery(
                appContext,
                uri
            )

            DATASET, DATASET_ACTIVE -> datasetQuery(
                appContext,
                uri
            )

            DATASET_ID -> datasetByIdQuery(
                appContext,
                uri
            )

            INPUT_OBSERVERS -> inputObserversQuery(
                appContext,
                selection,
                selectionArgs
            )

            INPUT_OBSERVERS_IDS -> inputObserversByIdsQuery(
                appContext,
                uri
            )

            INPUT_OBSERVER_ID -> inputObserverByIdQuery(
                appContext,
                uri
            )

            TAXONOMY, TAXONOMY_KINGDOM, TAXONOMY_KINGDOM_GROUP -> taxonomyQuery(
                appContext,
                uri
            )

            TAXA, TAXA_LIST_ID, TAXA_AREA_ID -> taxaQuery(
                appContext,
                uri,
                selection,
                selectionArgs,
                sortOrder
            )

            TAXON_ID -> taxonByIdQuery(
                appContext,
                uri
            )

            TAXON_AREA_ID -> taxonWithAreaByIdQuery(
                appContext,
                uri
            )

            NOMENCLATURE_TYPES -> nomenclatureTypesQuery(appContext)
            NOMENCLATURE_TYPES_DEFAULT -> defaultNomenclaturesByModule(
                appContext,
                uri
            )

            NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM, NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP -> nomenclaturesWithTaxonomyQuery(
                appContext,
                uri
            )

            else -> throw IllegalArgumentException("Unknown URI (query): $uri")
        }
    }

    override fun openFile(
        uri: Uri,
        mode: String
    ): ParcelFileDescriptor? {
        val context = context
            ?: return null

        return when (uriMatcher.match(uri)) {
            SETTINGS -> {
                val filename = uri.lastPathSegment

                if (filename.isNullOrEmpty()) {
                    throw IllegalArgumentException("Missing filename")
                }

                val file = File(
                    FileUtils.getRootFolder(
                        context,
                        MountPoint.StorageType.INTERNAL
                    ),
                    filename
                )

                if (!file.exists()) {
                    throw FileNotFoundException("No file found at $uri")
                }

                ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            }

            INPUT_ID -> {
                val packageId = uri.pathSegments
                    .drop(uri.pathSegments.indexOf("inputs") + 1)
                    .take(1)
                    .firstOrNull()

                if (packageId.isNullOrEmpty()) {
                    throw IllegalArgumentException("Missing package ID from URI '$uri'")
                }

                val inputId = uri.lastPathSegment?.toLongOrNull()
                    ?: throw IllegalArgumentException("Missing input ID from URI '$uri'")

                val file = InputDao(context).getExportedInput(
                    packageId,
                    inputId
                )

                if (!file.exists()) {
                    throw FileNotFoundException("No input file found at $uri")
                }

                ParcelFileDescriptor.open(
                    file,
                    ParcelFileDescriptor.MODE_READ_ONLY
                )
            }

            else -> throw IllegalArgumentException("Unknown URI (openFile): $uri")
        }
    }

    override fun insert(
        uri: Uri,
        values: ContentValues?
    ): Uri? {
        val context = context
            ?: return null

        return when (uriMatcher.match(uri)) {
            INPUTS_EXPORT -> {
                if (values == null) {
                    throw IllegalArgumentException("Missing ContentValues")
                }

                InputDao(context).exportInput(
                    authority,
                    values
                )
            }

            else -> throw IllegalArgumentException("Unknown URI (insert): $uri")
        }
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
        appContext: Context,
        uri: Uri
    ): Cursor {
        return getAppSyncDao(appContext).findByPackageId(uri.lastPathSegment)
    }

    private fun datasetQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val module = uri.pathSegments
            .drop(uri.pathSegments.indexOf(Dataset.TABLE_NAME) + 1)
            .take(1)
            .firstOrNull()

        val onlyActive = uri.lastPathSegment == "active"

        return getDatasetDao(context)
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
        val module = uri.pathSegments
            .drop(uri.pathSegments.indexOf(Dataset.TABLE_NAME) + 1)
            .take(1)
            .firstOrNull()

        return getDatasetDao(context)
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
        return getInputObserverDao(context)
            .QB()
            .whereSelection(
                selection,
                arrayOf(
                    *selectionArgs
                        ?: emptyArray()
                )
            )
            .cursor()
    }

    private fun inputObserversByIdsQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val selectedObserverIds = uri.lastPathSegment
            ?.split(",")
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

        return getInputObserverDao(context)
            .QB()
            .whereIdsIn(*selectedObserverIds)
            .cursor()
    }

    private fun inputObserverByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        return getInputObserverDao(context)
            .QB()
            .whereId(uri.lastPathSegment?.toLongOrNull())
            .cursor()
    }

    private fun taxonomyQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val lastPathSegments = uri.pathSegments
            .drop(uri.pathSegments.indexOf(Taxonomy.TABLE_NAME) + 1)
            .take(2)

        return getTaxonomyDao(context)
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
        uri: Uri,
        selection: String?,
        selectionArgs: Array<String>?,
        sortOrder: String?
    ): Cursor {
        val uriRegex = "/${Taxon.TABLE_NAME}(/list/\\d+)?(/area/\\d+)?".toRegex()

        val mathResult = uri.path
            ?.takeIf { uriRegex.matches(it) }
            ?.let { uriRegex.find(it) }
            ?: run {
                Logger.warn { "invalid taxa URI: '$uri', fetch all taxa..." }

                return getTaxonDao(context)
                    .QB()
                    .whereSelection(
                        selection,
                        arrayOf(
                            *selectionArgs
                                ?: emptyArray()
                        )
                    )
                    .also {
                        if (sortOrder.isNullOrEmpty()) {
                            return@also
                        }

                        (it as TaxonDao.QB).orderBy(sortOrder)
                    }
                    .cursor()
            }

        val qb = getTaxonDao(context).QB()

        mathResult.groupValues
            .drop(1)
            .filterNot { it.isBlank() }
            .forEach {
                val id = it
                    .substringAfterLast("/")
                    .toLongOrNull()

                with(it) {
                    when {
                        startsWith("/list") -> qb.withListId(id)
                        startsWith("/area") -> qb.withArea(id)
                    }
                }
            }

        return qb
            .whereSelection(
                selection,
                arrayOf(
                    *selectionArgs
                        ?: emptyArray()
                )
            )
            .also {
                if (sortOrder.isNullOrEmpty()) {
                    return@also
                }

                (it as TaxonDao.QB).orderBy(sortOrder)
            }
            .cursor()
    }

    private fun taxonByIdQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        return getTaxonDao(context)
            .QB()
            .whereId(uri.lastPathSegment?.toLongOrNull())
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

        return getTaxonDao(context)
            .QB()
            .withArea(filterOnArea)
            .whereId(taxonId)
            .cursor()
    }

    private fun nomenclatureTypesQuery(context: Context): Cursor {
        return getNomenclatureTypeDao(context)
            .QB()
            .cursor()
    }

    private fun defaultNomenclaturesByModule(
        context: Context,
        uri: Uri
    ): Cursor {
        val module = uri.pathSegments
            .drop(uri.pathSegments.indexOf(NomenclatureType.TABLE_NAME) + 1)
            .take(1)
            .firstOrNull()

        return getNomenclatureDao(context)
            .QB()
            .withNomenclatureType()
            .withDefaultNomenclature(module)
            .cursor()
    }

    private fun nomenclaturesWithTaxonomyQuery(
        context: Context,
        uri: Uri
    ): Cursor {
        val mnemonic = uri.pathSegments
            .drop(uri.pathSegments.indexOf(NomenclatureType.TABLE_NAME) + 1)
            .take(1)
            .firstOrNull()
        val lastPathSegments = uri.pathSegments
            .drop(uri.pathSegments.indexOf("items") + 1)
            .take(2)

        return getNomenclatureDao(context)
            .QB()
            .withNomenclatureType(mnemonic)
            .withTaxonomy(
                lastPathSegments.getOrNull(0),
                lastPathSegments.getOrNull(1)
            )
            .cursor()
    }

    /**
     * Gets a [AppSyncDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getAppSyncDao(appContext: Context): AppSyncDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).appSyncDao()
    }

    /**
     * Gets a [DatasetDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getDatasetDao(appContext: Context): DatasetDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).datasetDao()
    }

    /**
     * Gets a [InputObserverDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getInputObserverDao(appContext: Context): InputObserverDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).inputObserverDao()
    }

    /**
     * Gets a [TaxonomyDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getTaxonomyDao(appContext: Context): TaxonomyDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).taxonomyDao()
    }

    /**
     * Gets a [TaxonDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getTaxonDao(appContext: Context): TaxonDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).taxonDao()
    }

    /**
     * Gets a [NomenclatureTypeDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getNomenclatureTypeDao(appContext: Context): NomenclatureTypeDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).nomenclatureTypeDao()
    }

    /**
     * Gets a [NomenclatureDao] instance provided by Hilt using the @EntryPoint annotated interface.
     */
    private fun getNomenclatureDao(appContext: Context): NomenclatureDao {
        return fromApplication(
            appContext,
            MainContentProviderEntryPoint::class.java
        ).nomenclatureDao()
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
        const val TAXA_LIST_ID = 42
        const val TAXA_AREA_ID = 43
        const val TAXA_LIST_AREA_ID = 44
        const val TAXON_AREA_ID = 45
        const val NOMENCLATURE_TYPES = 50
        const val NOMENCLATURE_TYPES_DEFAULT = 51
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM = 52
        const val NOMENCLATURE_ITEMS_TAXONOMY_KINGDOM_GROUP = 53
        const val SETTINGS = 60
        const val INPUTS_EXPORT = 70
        const val INPUT_ID = 71

        const val VND_TYPE_DIR_PREFIX = "vnd.android.cursor.dir"
        const val VND_TYPE_ITEM_PREFIX = "vnd.android.cursor.item"
    }
}
