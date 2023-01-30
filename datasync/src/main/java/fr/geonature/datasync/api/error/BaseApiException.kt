package fr.geonature.datasync.api.error

import okhttp3.Response
import java.io.IOException

/**
 * Base exception about API errors.
 *
 * @author S. Grimault
 */
sealed class BaseApiException(
    open val statusCode: Int,
    message: String? = null,
    open val response: Response? = null,
    cause: Throwable? = null,
) : IOException(
    message,
    cause
) {

    /**
     * Handles other HTTP response status error code.
     */
    data class ApiException(
        override val statusCode: Int,
        override val message: String? = null,
        override val response: Response? = null,
        override val cause: Throwable? = null
    ) : BaseApiException(
        statusCode,
        message,
        response,
        cause
    )

    /**
     * Handles "Bad Request" HTTP response status code.
     */
    data class BadRequestException(
        override val message: String? = null,
        override val response: Response? = null,
        override val cause: Throwable? = null
    ) : BaseApiException(
        400,
        message,
        response,
        cause
    )

    /**
     * Handles "Unauthorized" HTTP response status code.
     */
    data class UnauthorizedException(
        override val message: String? = null,
        override val response: Response? = null,
        override val cause: Throwable? = null
    ) : BaseApiException(
        401,
        message,
        response,
        cause
    )

    /**
     * Handles "Not Found" HTTP response status code.
     */
    data class NotFoundException(
        override val message: String? = null,
        override val response: Response? = null,
        override val cause: Throwable? = null
    ) : BaseApiException(
        404,
        message,
        response,
        cause
    )

    /**
     * Handles "Internal Server" HTTP response status code.
     */
    data class InternalServerException(
        override val message: String? = null,
        override val response: Response? = null,
        override val cause: Throwable? = null
    ) : BaseApiException(
        500,
        message,
        response,
        cause
    )
}

