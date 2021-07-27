package fr.geonature.commons.fp

/**
 * Base class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure {
    object NetworkFailure : Failure()
    object ServerFailure : Failure()

    abstract class FeatureFailure : Failure()
}