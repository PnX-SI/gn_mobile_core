package fr.geonature.datasync.packageinfo.io

import fr.geonature.datasync.FixtureHelper
import fr.geonature.datasync.api.model.AppPackage
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

/**
 * Unit tests about [AppPackageJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class AppPackageJsonReaderTest {

    @Test
    fun `should read a list of app packages from valid JSON file`() {
        // given a JSON settings
        val json = FixtureHelper.getFixture("t_mobile_apps.json")

        // when read the JSON as list of AppPackage
        val appPackages = AppPackageJsonReader().read(json)

        // then
        assertNotNull(appPackages)
        assertEquals(
            listOf(
                AppPackage(
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.1-generic-release.apk",
                    packageName = "fr.geonature.occtax2",
                    code = "OCCTAX2",
                    versionCode = 2575,
                    settings = mapOf(
                        "area_observation_duration" to 365.0,
                        "sync" to mapOf(
                            "geonature_url" to "https://demo.geonature.fr/geonature",
                            "taxhub_url" to "https://demo.geonature.fr/taxhub",
                            "gn_application_id" to 1.0,
                            "observers_list_id" to 1.0,
                            "taxa_list_id" to 100.0,
                            "code_area_type" to "M1",
                            "page_size" to 1000.0
                        ),
                        "map" to mapOf(
                            "show_scale" to true,
                            "show_compass" to true,
                            "max_bounds" to arrayListOf(
                                arrayListOf(
                                    52.0,
                                    -6.0
                                ),
                                arrayListOf(
                                    40.0,
                                    9.0
                                )
                            ),
                            "center" to arrayListOf(
                                46.0,
                                2.0
                            ),
                            "start_zoom" to 10.0,
                            "min_zoom" to 8.0,
                            "max_zoom" to 19.0,
                            "min_zoom_editing" to 12.0,
                            "layers" to arrayListOf(
                                mapOf(
                                    "label" to "OSM",
                                    "source" to "https://a.tile.openstreetmap.org",
                                ),
                                mapOf(
                                    "label" to "Nantes",
                                    "source" to "nantes.mbtiles",
                                ),
                                mapOf(
                                    "label" to "Mailles 5km",
                                    "source" to "mailles5pne.geojson",
                                    "style" to mapOf(
                                        "stroke" to true,
                                        "color" to "#FF0000",
                                        "weight" to 4.0,
                                        "opacity" to 0.9,
                                        "fill" to true,
                                        "fillColor" to "#FF8000",
                                        "fillOpacity" to 0.2
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            appPackages
        )
    }

    @Test
    fun `should read a single app package from valid JSON file`() {
        // given a JSON settings
        val json = FixtureHelper.getFixture("t_mobile_app.json")

        // when read the JSON as list of AppPackage
        val appPackages = AppPackageJsonReader().read(json)

        // then
        assertNotNull(appPackages)
        assertEquals(
            listOf(
                AppPackage(
                    apkUrl = "https://demo.geonature.fr/geonature/api/static/mobile/occtax/occtax-2.0.1-generic-release.apk",
                    packageName = "fr.geonature.occtax2",
                    code = "OCCTAX2",
                    versionCode = 2575,
                    settings = mapOf(
                        "area_observation_duration" to 365.0,
                        "sync" to mapOf(
                            "geonature_url" to "https://demo.geonature.fr/geonature",
                            "taxhub_url" to "https://demo.geonature.fr/taxhub",
                            "gn_application_id" to 1.0,
                            "observers_list_id" to 1.0,
                            "taxa_list_id" to 100.0,
                            "code_area_type" to "M1",
                            "page_size" to 1000.0
                        ),
                        "map" to mapOf(
                            "show_scale" to true,
                            "show_compass" to true,
                            "max_bounds" to arrayListOf(
                                arrayListOf(
                                    52.0,
                                    -6.0
                                ),
                                arrayListOf(
                                    40.0,
                                    9.0
                                )
                            ),
                            "center" to arrayListOf(
                                46.0,
                                2.0
                            ),
                            "start_zoom" to 10.0,
                            "min_zoom" to 8.0,
                            "max_zoom" to 19.0,
                            "min_zoom_editing" to 12.0,
                            "layers" to arrayListOf(
                                mapOf(
                                    "label" to "OSM",
                                    "source" to "https://a.tile.openstreetmap.org",
                                ),
                                mapOf(
                                    "label" to "Nantes",
                                    "source" to "nantes.mbtiles",
                                ),
                                mapOf(
                                    "label" to "Mailles 5km",
                                    "source" to "mailles5pne.geojson",
                                    "style" to mapOf(
                                        "stroke" to true,
                                        "color" to "#FF0000",
                                        "weight" to 4.0,
                                        "opacity" to 0.9,
                                        "fill" to true,
                                        "fillColor" to "#FF8000",
                                        "fillOpacity" to 0.2
                                    )
                                )
                            )
                        )
                    )
                )
            ),
            appPackages
        )
    }

    @Test
    fun `should returns an empty list of app packages from invalid empty JSON`() {
        // when read an invalid JSON as list of AppPackage
        val appPackages = AppPackageJsonReader().read("")

        // then
        assertTrue(appPackages.isEmpty())
    }

    @Test
    fun `should returns an empty list of app packages from empty JSON`() {
        // when read an invalid JSON as list of AppPackage
        val appPackages = AppPackageJsonReader().read("{}")

        assertTrue(appPackages.isEmpty())
    }
}