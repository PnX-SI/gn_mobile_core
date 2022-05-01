package fr.geonature.commons.error

/**
 * Base class for handling errors/failures/exceptions.
 * Every feature specific failure should extend [FeatureFailure] class.
 */
sealed class Failure {
    data class NetworkFailure(val reason: String) : Failure()
    object ServerFailure : Failure()

    abstract class FeatureFailure : Failure()
}