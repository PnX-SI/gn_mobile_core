package fr.geonature.datasync.api

import fr.geonature.commons.fp.Failure

/**
 * Failure about missing configuration about GeoNature services.
 *
 * @author S. Grimault
 */
object GeoNatureMissingConfigurationFailure : Failure.FeatureFailure()
