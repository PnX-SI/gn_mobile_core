package fr.geonature.commons.features.taxon.data

import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.data.entity.Taxon
import fr.geonature.commons.data.entity.TaxonWithArea
import fr.geonature.commons.features.taxon.error.TaxonException

/**
 * Default implementation of [ITaxonLocalDataSource] using local database.
 *
 * @author S. Grimault
 */
class TaxonLocalDataSourceImpl(private val taxonDao: TaxonDao) : ITaxonLocalDataSource {

    override suspend fun findTaxonById(taxonId: Long): Taxon {
        return taxonDao.findById(taxonId)
            ?: throw TaxonException.NoTaxonFoundException(taxonId)
    }

    override suspend fun findTaxaByIds(vararg taxonIds: Long): List<Taxon> {
        return taxonDao.findByIds(*taxonIds)
    }

    override suspend fun findTaxonByIdWithArea(
        taxonId: Long,
        areaId: Long
    ): TaxonWithArea {
        return taxonDao.findByIdMatchingArea(
            taxonId,
            areaId
        ).entries
            .firstOrNull()
            ?.let { TaxonWithArea(it.key).apply { taxonArea = it.value } }
            ?: throw TaxonException.NoTaxonFoundException(taxonId)
    }
}