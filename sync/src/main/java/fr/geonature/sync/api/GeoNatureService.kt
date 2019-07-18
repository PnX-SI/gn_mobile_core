package fr.geonature.sync.api

import fr.geonature.sync.api.model.User
import retrofit2.Call
import retrofit2.http.GET

/**
 * GeoNature API interface definition.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
interface GeoNatureService {

    @GET("users/menu/1")
    fun getUsers(): Call<List<User>>
}