package fr.geonature.viewpager.model

import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.recyclerview.widget.BatchingListUpdateCallback
import io.mockk.MockKAnnotations
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verifySequence
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/**
 * Unit test for [PageFragmentViewModel].
 *
 * @author S. Grimault
 */
class PageFragmentViewModelTest {

    @get:Rule
    val instantTaskExecutorRule = InstantTaskExecutorRule()

    @RelaxedMockK
    private lateinit var listUpdateCallback: BatchingListUpdateCallback

    @Before
    fun setUp() {
        MockKAnnotations.init(this)
    }

    @Test
    fun `should set new pages`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        val pages = listOf(
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 1
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
        )

        pageFragmentViewModel.set(*pages.associateBy { it.getResourceTitle() }.entries
            .map {
                Pair(
                    it.key,
                    it.value
                )
            }
            .toTypedArray())

        val diffResult = pageFragmentViewModel.onChanges.value
        assertNotNull(diffResult)
        assertEquals(
            pages.associateBy { it.getResourceTitle() },
            pageFragmentViewModel.pageFragments
        )

        diffResult?.dispatchUpdatesTo(listUpdateCallback)
        verifySequence {
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.dispatchLastEvent()
        }
    }

    @Test
    fun `should add new pages from empty pager`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        pageFragmentViewModel.add(
            1 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 1
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            2 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
        )
        val diffResult = pageFragmentViewModel.onChanges.value
        assertNotNull(diffResult)

        diffResult?.dispatchUpdatesTo(listUpdateCallback)
        verifySequence {
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.dispatchLastEvent()
        }
    }

    @Test
    fun `should add new pages to existing ones`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        pageFragmentViewModel.set(
            1 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 1
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            2 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
        )
        pageFragmentViewModel.add(3 to object : IPageFragment {
            override fun getResourceTitle(): Int {
                return 3
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun pagingEnabled(): Boolean {
                return true
            }
        })

        val diffResult = pageFragmentViewModel.onChanges.value
        assertNotNull(diffResult)

        diffResult?.dispatchUpdatesTo(listUpdateCallback)
        verifySequence {
            listUpdateCallback.onInserted(
                2,
                1
            )
            listUpdateCallback.dispatchLastEvent()
        }
    }

    @Test
    fun `should remove page`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        pageFragmentViewModel.set(
            1 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 1
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            2 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            3 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
        )
        pageFragmentViewModel.remove(2)

        val diffResult = pageFragmentViewModel.onChanges.value
        assertNotNull(diffResult)

        diffResult?.dispatchUpdatesTo(listUpdateCallback)
        verifySequence {
            listUpdateCallback.onRemoved(
                1,
                1
            )
            listUpdateCallback.dispatchLastEvent()
        }
    }

    @Test
    fun `should remove non existing page`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        pageFragmentViewModel.set(
            1 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 1
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            2 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            3 to object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
        )
        pageFragmentViewModel.remove(4)

        val diffResult = pageFragmentViewModel.onChanges.value
        assertNotNull(diffResult)
        assertEquals(
            setOf(
                1,
                2,
                3
            ),
            pageFragmentViewModel.pageFragments.keys
        )

        diffResult?.dispatchUpdatesTo(listUpdateCallback)
        verifySequence {
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.onInserted(
                0,
                1
            )
            listUpdateCallback.dispatchLastEvent()
        }
    }

    @Test
    fun `should get page at position`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        val pages = listOf(object : IPageFragment {
            override fun getResourceTitle(): Int {
                return 1
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun pagingEnabled(): Boolean {
                return true
            }
        },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            })

        pageFragmentViewModel.set(*pages.associateBy { it.getResourceTitle() }.entries
            .map {
                Pair(
                    it.key,
                    it.value
                )
            }
            .toTypedArray())
        val page = pageFragmentViewModel.getPageAtPosition(1)

        assertNotNull(page)
        assertEquals(
            pages[1],
            page,
        )
    }

    @Test
    fun `should return null if no page was found at position`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        val pages = listOf(object : IPageFragment {
            override fun getResourceTitle(): Int {
                return 1
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun pagingEnabled(): Boolean {
                return true
            }
        },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            })

        pageFragmentViewModel.set(*pages.associateBy { it.getResourceTitle() }.entries
            .map {
                Pair(
                    it.key,
                    it.value
                )
            }
            .toTypedArray())
        val page = pageFragmentViewModel.getPageAtPosition(4)

        assertNull(page)
    }

    @Test
    fun `should get the current position of the given page`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        val pages = listOf(object : IPageFragment {
            override fun getResourceTitle(): Int {
                return 1
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun pagingEnabled(): Boolean {
                return true
            }
        },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            })

        pageFragmentViewModel.set(*pages.associateBy { it.getResourceTitle() }.entries
            .map {
                Pair(
                    it.key,
                    it.value
                )
            }
            .toTypedArray())
        val position = pageFragmentViewModel.getPagePosition(2)

        assertEquals(
            1,
            position
        )
    }

    @Test
    fun `should return null if no position was found from given page`() {
        val pageFragmentViewModel = PageFragmentViewModel()
        val pages = listOf(object : IPageFragment {
            override fun getResourceTitle(): Int {
                return 1
            }

            override fun getSubtitle(): CharSequence? {
                return null
            }

            override fun pagingEnabled(): Boolean {
                return true
            }
        },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 2
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            },
            object : IPageFragment {
                override fun getResourceTitle(): Int {
                    return 3
                }

                override fun getSubtitle(): CharSequence? {
                    return null
                }

                override fun pagingEnabled(): Boolean {
                    return true
                }
            })

        pageFragmentViewModel.set(*pages.associateBy { it.getResourceTitle() }.entries
            .map {
                Pair(
                    it.key,
                    it.value
                )
            }
            .toTypedArray())
        val position = pageFragmentViewModel.getPagePosition(4)

        assertNull(position)
    }
}