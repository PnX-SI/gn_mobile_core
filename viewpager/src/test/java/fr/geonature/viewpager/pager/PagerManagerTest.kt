package fr.geonature.viewpager.pager

import androidx.test.core.app.ApplicationProvider
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit test for [PagerManager].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
@RunWith(RobolectricTestRunner::class)
class PagerManagerTest {
    private lateinit var pagerManager: PagerManager

    @Before
    fun setUp() {
        pagerManager = PagerManager(ApplicationProvider.getApplicationContext())
    }

    @Test
    fun testLoadingNonExistingPager() {
        // when loading non existing Pager
        val pager = runBlocking { pagerManager.load(42) }

        // then
        assertNotNull(pager)
        assertEquals(0,
                     pager.id)
    }

    @Test
    fun testLoadingUndefinedPager() {
        // when loading undefined Pager
        val pager = runBlocking { pagerManager.load() }

        // then
        assertNotNull(pager)
        assertEquals(0,
                     pager.id)
    }

    @Test
    fun testSaveAndLoadPager() {
        // given a pager metadata to save and load
        val pager = Pager(1234L)
        pager.size = 5
        pager.position = 3
        pager.history.add(1)
        pager.history.add(4)
        pager.history.add(3)
        pager.history.add(2)

        // when saving this Pager
        val saved = runBlocking { pagerManager.save(pager) }

        // then
        assertTrue(saved)

        // when reading this Pager from manager
        val loadedPager = runBlocking { pagerManager.load(pager.id) }

        // then
        assertNotNull(loadedPager)
        assertEquals(pager,
                     loadedPager)
    }

    @Test
    fun testSaveAndDeletePager() {
        // given a pager metadata to save and delete
        val pager = Pager(1234L)
        pager.size = 5
        pager.position = 3
        pager.history.add(1)
        pager.history.add(4)
        pager.history.add(3)
        pager.history.add(2)

        // when saving this Pager
        val saved = runBlocking { pagerManager.save(pager) }

        // then
        assertTrue(saved)

        // when deleting this Pager from manager
        val deleted = runBlocking { pagerManager.delete(pager.id) }

        // then
        assertTrue(deleted)
        val noSuchPager = runBlocking { pagerManager.load(pager.id) }

        assertNotNull(noSuchPager)
        assertEquals(0,
                     noSuchPager.id)
        assertEquals(0,
                     noSuchPager.size)
        assertEquals(0,
                     noSuchPager.position)
        assertTrue(noSuchPager.history.isEmpty())
    }
}