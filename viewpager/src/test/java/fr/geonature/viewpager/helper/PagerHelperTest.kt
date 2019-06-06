package fr.geonature.viewpager.helper

import android.content.Context
import androidx.preference.PreferenceManager
import androidx.test.core.app.ApplicationProvider.getApplicationContext
import fr.geonature.viewpager.model.Pager
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit test for [PagerHelper].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PagerHelperTest {
    lateinit var context: Context
    lateinit var pagerHelper: PagerHelper

    @Before
    fun setUp() {
        context = getApplicationContext()
        pagerHelper = PagerHelper(context)
    }

    @Test
    @Throws(Exception::class)
    fun testLoadNullPager() {
        // given a not found pager
        val pagerId = 1234L

        // when trying to load this pager
        val pager = pagerHelper.load(pagerId)

        // then
        assertNotNull(pager)
        assertEquals(pagerId, pager.id)
    }

    @Test
    fun testLoadExistingPager() {
        // given an existing pager
        val jsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(1234L)
            .append(",\"size\":")
            .append(5)
            .append(",\"position\":")
            .append(3)
            .append(",\"history\":[1,4,3,2]")
            .append('}')
            .toString()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(pagerHelper.getPagerPreferenceKey(1234L), jsonString)
            .commit()

        // when trying to load this pager
        val pager = pagerHelper.load(1234L)

        // then
        assertNotNull(pager)
        assertEquals(1234L, pager.id)
        assertEquals(5, pager.size)
        assertEquals(3, pager.position)
        assertEquals(4, pager.history.size)
        assertEquals(Integer.valueOf(2), pager.history.pollLast())
        assertEquals(Integer.valueOf(3), pager.history.pollLast())
        assertEquals(Integer.valueOf(4), pager.history.pollLast())
        assertEquals(Integer.valueOf(1), pager.history.pollLast())
    }

    @Test
    fun testSave() {
        // given a pager metadata
        val pager = Pager(1234L)
        pager.size = 5
        pager.position = 3
        pager.history.add(1)
        pager.history.add(4)
        pager.history.add(3)
        pager.history.add(2)

        // when trying to save this pager
        pagerHelper.save(pager)

        // then
        assertEquals(pager, pagerHelper.load(pager.id))
    }

    @Test
    fun testDelete() {
        // given an existing pager
        val jsonString = StringBuilder().append('{')
            .append("\"id\":")
            .append(1234L)
            .append(",\"size\":")
            .append(5)
            .append(",\"position\":")
            .append(3)
            .append(",\"history\":[1,4,3,2]")
            .append('}')
            .toString()

        PreferenceManager.getDefaultSharedPreferences(context)
            .edit()
            .putString(pagerHelper.getPagerPreferenceKey(1234L), jsonString)
            .commit()

        // when trying to delete this pager
        pagerHelper.delete(1234L)

        // then
        val pagerLoader = pagerHelper.load(1234L)
        assertEquals(0, pagerLoader.size)
        assertEquals(0, pagerLoader.position)
        assertTrue(pagerLoader.history.isEmpty())
        assertNull(PreferenceManager.getDefaultSharedPreferences(context).getString(pagerHelper.getPagerPreferenceKey(
            1234L), null))

    }
}