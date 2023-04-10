package fr.geonature.commons.ui.adapter

import android.view.View
import io.mockk.MockKAnnotations.init
import io.mockk.clearAllMocks
import io.mockk.impl.annotations.RelaxedMockK
import io.mockk.verify
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

/**
 * Unit tests about [AbstractListItemRecyclerViewAdapter].
 *
 * @author S. Grimault
 */
@RunWith(RobolectricTestRunner::class)
class ListItemRecyclerViewAdapterTest {

    @RelaxedMockK
    private lateinit var onListItemRecyclerViewAdapterListener: AbstractListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<String>

    private lateinit var stringListItemRecyclerViewAdapter: StringListItemRecyclerViewAdapter

    @Before
    fun setUp() {
        init(this)

        stringListItemRecyclerViewAdapter =
            StringListItemRecyclerViewAdapter(onListItemRecyclerViewAdapterListener)
    }

    @Test
    fun testSetEmptyItems() {
        // given an empty list
        stringListItemRecyclerViewAdapter.setItems(emptyList())

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(true) }
        assertEquals(
            0,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertTrue(stringListItemRecyclerViewAdapter.items.isEmpty())
    }

    @Test
    fun testSetItems() {
        // given a new list
        stringListItemRecyclerViewAdapter.setItems(
            listOf(
                "item #1",
                "item #2"
            )
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            2,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #1",
                "item #2"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding some new element
        stringListItemRecyclerViewAdapter.setItems(listOf("item #3"))

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            1,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf("item #3"),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding nothing
        stringListItemRecyclerViewAdapter.setItems(emptyList())

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(true) }
        assertEquals(
            0,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertTrue(stringListItemRecyclerViewAdapter.items.isEmpty())
    }

    @Test
    fun testSetItemsAndClear() {
        // given a new list
        stringListItemRecyclerViewAdapter.setItems(
            listOf(
                "item #1",
                "item #2"
            )
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            2,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #1",
                "item #2"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when clearing the adapter
        stringListItemRecyclerViewAdapter.clear()

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(true) }
        assertEquals(
            0,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertTrue(stringListItemRecyclerViewAdapter.items.isEmpty())
    }

    @Test
    fun testAdd() {
        // when adding item
        stringListItemRecyclerViewAdapter.add("item #1")

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            1,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf("item #1"),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding another item at first position
        stringListItemRecyclerViewAdapter.add(
            "item #2",
            0
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            2,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
                "item #1"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding item at invalid position
        stringListItemRecyclerViewAdapter.add(
            "item #3",
            100
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            3,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
                "item #1",
                "item #3"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding item at invalid position
        stringListItemRecyclerViewAdapter.add(
            "item #4",
            -2
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            4,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
                "item #1",
                "item #3",
                "item #4"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )
    }

    @Test
    fun testSet() {
        // when adding item
        stringListItemRecyclerViewAdapter.set(
            "item #1",
            0
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            1,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf("item #1"),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when updating item at first position
        stringListItemRecyclerViewAdapter.set(
            "item #2",
            0
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            1,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding item at invalid position
        stringListItemRecyclerViewAdapter.set(
            "item #3",
            100
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            2,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
                "item #3"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when adding item at invalid position
        stringListItemRecyclerViewAdapter.add(
            "item #4",
            -2
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            3,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #2",
                "item #3",
                "item #4"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )
    }

    @Test
    fun testRemove() {
        // given a new list
        stringListItemRecyclerViewAdapter.setItems(
            listOf(
                "item #1",
                "item #2"
            )
        )

        // then
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertEquals(
            2,
            stringListItemRecyclerViewAdapter.itemCount
        )
        assertArrayEquals(
            arrayOf(
                "item #1",
                "item #2"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        clearAllMocks()

        // when removing non existing item
        assertEquals(
            -1,
            stringListItemRecyclerViewAdapter.remove("item #3")
        )
        verify(inverse = true) { onListItemRecyclerViewAdapterListener.showEmptyTextView(any()) }
        assertArrayEquals(
            arrayOf(
                "item #1",
                "item #2"
            ),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when removing last item
        assertEquals(
            1,
            stringListItemRecyclerViewAdapter.remove("item #2")
        )
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(false) }
        assertArrayEquals(
            arrayOf("item #1"),
            stringListItemRecyclerViewAdapter.items.toTypedArray()
        )

        // when removing last item
        assertEquals(
            0,
            stringListItemRecyclerViewAdapter.remove("item #1")
        )
        verify { onListItemRecyclerViewAdapterListener.showEmptyTextView(true) }
        assertTrue(stringListItemRecyclerViewAdapter.items.isEmpty())
    }

    class StringListItemRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<String>) :
        AbstractListItemRecyclerViewAdapter<String>(listener) {
        override fun getViewHolder(
            view: View,
            viewType: Int
        ): AbstractViewHolder {
            return ViewHolder(view)
        }

        override fun getLayoutResourceId(
            position: Int,
            item: String
        ): Int {
            return android.R.layout.simple_list_item_1
        }

        override fun areItemsTheSame(
            oldItems: List<String>,
            newItems: List<String>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        override fun areContentsTheSame(
            oldItems: List<String>,
            newItems: List<String>,
            oldItemPosition: Int,
            newItemPosition: Int
        ): Boolean {
            return oldItems[oldItemPosition] == newItems[newItemPosition]
        }

        inner class ViewHolder(itemView: View) :
            AbstractListItemRecyclerViewAdapter<String>.AbstractViewHolder(itemView) {
            override fun onBind(item: String) {
            }
        }
    }
}
