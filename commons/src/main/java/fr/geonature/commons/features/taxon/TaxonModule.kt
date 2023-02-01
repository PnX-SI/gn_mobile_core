package fr.geonature.commons.features.taxon

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import fr.geonature.commons.data.dao.TaxonDao
import fr.geonature.commons.features.taxon.data.ITaxonLocalDataSource
import fr.geonature.commons.features.taxon.data.TaxonLocalDataSourceImpl
import javax.inject.Singleton

/**
 * Taxon module.
 *
 * @author S. Grimault
 */
@Module
@InstallIn(SingletonComponent::class)
object TaxonModule {

    @Singleton
    @Provides
    fun provideTaxonLocalDataSource(taxonDao: TaxonDao): ITaxonLocalDataSource {
        return TaxonLocalDataSourceImpl(taxonDao)
    }
}