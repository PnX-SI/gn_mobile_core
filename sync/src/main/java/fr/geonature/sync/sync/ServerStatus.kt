package fr.geonature.sync.sync

/**
 * Server status.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
enum class ServerStatus(val httpStatus: Int) {
    OK(200),
    UNAUTHORIZED(401),
    FORBIDDEN(403),
    INTERNAL_SERVER_ERROR(500)
}
