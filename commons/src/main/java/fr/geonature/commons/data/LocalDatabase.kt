package fr.geonature.commons.data

import androidx.room.Database
import androidx.room.RoomDatabase
import fr.geonature.commons.data.dao.AdditionalFieldDao
import fr.geonature.commons.data.dao.AdditionalFieldDatasetDao
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
import fr.geonature.commons.data.entity.AdditionalField
import fr.geonature.commons.data.entity.AdditionalFieldDataset
import fr.geonature.commons.data.entity.CodeObject
import fr.geonature.commons.data.entity.Dataset
import fr.geonature.commons.data.entity.DefaultNomenclature
import fr.geonature.commons.data.entity.FieldValue
import fr.geonature.commons.data.entity.InputObserver
import fr.geonature.commons.data.entity.Nomenclature
import fr.geonature.commons.data.entity.NomenclatureTaxonomy
import fr.geonature.commons.data.entity.NomenclatureType
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonArea
import fr.geonature.commons.data.entity.Taxonomy

/**
 * The Room database.
 *
 * @author S. Grimault
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
        DefaultNomenclature::class,
        AdditionalField::class,
        AdditionalFieldDataset::class,
        CodeObject::class,
        FieldValue::class,
    ],
    version = 20,
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

    /**
     * @return The DAO for the [AdditionalField.TABLE_NAME] table.
     */
    abstract fun additionalFieldDao(): AdditionalFieldDao

    /**
     * @return The DAO for the [AdditionalFieldDataset.TABLE_NAME] table.
     */
    abstract fun additionalFieldDatasetDao(): AdditionalFieldDatasetDao

    /**
     * @return The DAO for the [CodeObject.TABLE_NAME] table.
     */
    abstract fun codeObjectDao(): CodeObjectDao

    /**
     * @return The DAO for the [FieldValue.TABLE_NAME] table.
     */
    abstract fun fieldValueDao(): FieldValueDao
}
