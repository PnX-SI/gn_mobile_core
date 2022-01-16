package fr.geonature.datasync.settings.io

import fr.geonature.datasync.FixtureHelper.getFixture
import fr.geonature.datasync.settings.DataSyncSettings
import fr.geonature.datasync.settings.error.DataSyncSettingsJsonParseException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.StringReader
import kotlin.time.DurationUnit
import kotlin.time.toDuration

/**
 * Unit tests about [DataSyncSettingsJsonReader].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
@Config(manifest = Config.NONE)
class DataSyncSettingsJsonReaderTest {

    @Test
    fun `should read data sync settings from valid JSON file`() {
        // given a JSON settings
        val json = getFixture("settings_datasync.json")

        // when read the JSON as DataSyncSettings
        val dataSyncSettings = DataSyncSettingsJsonReader().read(json)

        // then
        assertNotNull(dataSyncSettings)
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = 1000,
                dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
            ),
            dataSyncSettings
        )
    }

    @Test
    fun `should read data sync settings from valid JSON file with sync property`() {
        // given a JSON settings
        val json = getFixture("settings_sync.json")

        // when read the JSON as DataSyncSettings
        val dataSyncSettings = DataSyncSettingsJsonReader().read(json)

        // then
        assertNotNull(dataSyncSettings)
        assertEquals(
            DataSyncSettings(
                geoNatureServerUrl = "https://demo.geonature.fr/geonature",
                taxHubServerUrl = "https://demo.geonature.fr/taxhub",
                applicationId = 3,
                usersListId = 1,
                taxrefListId = 100,
                codeAreaType = "M10",
                pageSize = 1000,
                dataSyncPeriodicity = 30.toDuration(DurationUnit.MINUTES),
                essentialDataSyncPeriodicity = 20.toDuration(DurationUnit.MINUTES)
            ),
            dataSyncSettings
        )
    }

    @Test
    fun `should fail to read data sync settings from invalid empty JSON`() {
        // when read an invalid JSON as AppSettings
        val dataSyncSettings = DataSyncSettingsJsonReader().read("")

        // then
        assertNull(dataSyncSettings)
    }

    @Test
    fun `should throw DataSyncSettingsJsonParseException if trying to read data sync settings from empty JSON`() {
        assertEquals(
            "lateinit property geoNatureServerUrl has not been initialized",
            assertThrows(
                DataSyncSettingsJsonParseException::class.java
            ) {
                DataSyncSettingsJsonReader().read(StringReader("{}"))
            }.message
        )
    }

    @Test
    fun `should throw DataSyncSettingsJsonParseException if trying to read data sync settings from empty string`() {
        assertEquals(
            "End of input",
            assertThrows(
                DataSyncSettingsJsonParseException::class.java
            ) {
                DataSyncSettingsJsonReader().read(StringReader(""))
            }.message
        )
    }

    @Test
    fun `should throw DataSyncSettingsJsonParseException if trying to read data sync settings from JSON with empty sync property`() {
        assertEquals(
            "lateinit property geoNatureServerUrl has not been initialized",
            assertThrows(
                DataSyncSettingsJsonParseException::class.java
            ) {
                DataSyncSettingsJsonReader().read(StringReader("{\"sync\":{}}"))
            }.message
        )
    }
}