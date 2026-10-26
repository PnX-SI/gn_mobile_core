package fr.geonature.api.auth

import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.File

/**
 * Backup rules XML files cannot reference resources: ensures their literal file names stay in sync
 * with [SecureCookieStore].
 *
 * @author S. Grimault
 */
class BackupRulesTest {

    private val xmlFiles = listOf(
        "gn_api_backup_rules.xml",
        "gn_api_data_extraction_rules.xml"
    )

    @Test
    fun `backup rules should exclude all secure cookie files`() {
        val expectedPaths = listOf(
            "${SecureCookieStore.PREFERENCE_FILE_NAME}.xml",
            "${SecureCookieStore.PREFERENCE_FILE_NAME_KEYSET}.xml"
        )

        xmlFiles.forEach { fileName ->
            val content = File("src/main/res/xml/$fileName").readText()

            expectedPaths.forEach { path ->
                assertTrue(
                    "$fileName should exclude $path",
                    content.contains("path=\"$path\"")
                )
            }
        }
    }
}

