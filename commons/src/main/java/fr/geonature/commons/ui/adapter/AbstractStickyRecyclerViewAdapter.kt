package fr.geonature.commons.ui.adapter

import android.graphics.Canvas
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter with sticky headers.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see <a href="https://github.com/shuhart/StickyHeader">https://github.com/shuhart/StickyHeader</a>
 */
abstract class AbstractStickyRecyclerViewAdapter<SVH : RecyclerView.ViewHolder,
    VH : RecyclerView.ViewHolder> : RecyclerView.Adapter<VH>() {

    /**
     * This method gets called by [StickyHeaderItemDecorator] to fetch the position of the header
     * item in the adapter that is used for (represents) item at specified position.
     *
     * @param itemPosition Adapter's position of the item for which to do the search of the position
     * of the header item.
     *
     * @return int. Position of the header for an item in the adapter or
     * [RecyclerView.NO_POSITION] (-1) if an item has no header.
     */
    abstract fun getHeaderPositionForItem(itemPosition: Int): Int

    /**
     * This method gets called by [StickyHeaderItemDecorator] to setup the header View.
     *
     * @param holder         Holder to bind the data on.
     * @param headerPosition Position of the header item in the adapter.
     */
    abstract fun onBindHeaderViewHolder(holder: SVH, headerPosition: Int)

    /**
     * Called when [StickyHeaderItemDecorator] needs a new [RecyclerView.ViewHolder] to represent a
     * sticky header item.
     * Those two instances will be cached and used to represent a current top sticky header and the
     * moving one.
     *
     * We can either create a new View manually or inflate it from an XML layout file.
     *
     * The new ViewHolder will be used to display items of the adapter using
     * [onBindHeaderViewHolder].
     * Since it will be re-used to display different items in the data set, it is a good idea to
     * cache references to sub views of the View to avoid unnecessary [View.findViewById] calls.
     *
     * @param parent The ViewGroup to resolve a layout params.
     *
     * @return A new ViewHolder that holds a View of the given view type.
     *
     * @see [onBindHeaderViewHolder]
     */
    abstract fun onCreateHeaderViewHolder(parent: ViewGroup): SVH

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(StickyHeaderItemDecorator(this, recyclerView))
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        for (i in 0 until recyclerView.itemDecorationCount) {
            val decorator = recyclerView.getItemDecorationAt(i)

            if (decorator is StickyHeaderItemDecorator<*, *>) {
                recyclerView.removeItemDecoration(decorator)
            }
        }
    }

    /**
     * Decorator used for sticky header.
     */
    class StickyHeaderItemDecorator<SVH : RecyclerView.ViewHolder,
        VH : RecyclerView.ViewHolder>(
        private val adapter: AbstractStickyRecyclerViewAdapter<SVH, VH>,
        recyclerView: RecyclerView
    ) : RecyclerView.ItemDecoration() {

        private var currentStickyPosition = RecyclerView.NO_POSITION
        private var currentStickyHolder: SVH? = null
        private var lastViewOverlappedByHeader: View? = null

        init {
            currentStickyHolder = adapter.onCreateHeaderViewHolder(recyclerView)
            fixLayoutSize(recyclerView)
        }

        override fun onDrawOver(c: Canvas, parent: RecyclerView, state: RecyclerView.State) {
            super.onDrawOver(c, parent, state)

            val layoutManager = parent.layoutManager ?: return
            var topChildPosition = RecyclerView.NO_POSITION

            if (layoutManager is LinearLayoutManager) {
                topChildPosition = layoutManager.findFirstVisibleItemPosition()
            } else {
                val topChild = parent.getChildAt(0)

                if (topChild != null) {
                    topChildPosition = parent.getChildAdapterPosition(topChild)
                }
            }

            if (topChildPosition == RecyclerView.NO_POSITION) {
                return
            }

            var viewOverlappedByHeader: View? =
                getChildInContact(parent, currentStickyHolder!!.itemView.bottom)

            if (viewOverlappedByHeader == null) {
                viewOverlappedByHeader = if (lastViewOverlappedByHeader != null) {
                    lastViewOverlappedByHeader
                } else {
                    parent.getChildAt(topChildPosition)
                }
            }
            lastViewOverlappedByHeader = viewOverlappedByHeader

            val overlappedByHeaderPosition =
                parent.getChildAdapterPosition(viewOverlappedByHeader!!)
            val overlappedHeaderPosition: Int
            val preOverlappedPosition: Int

            if (overlappedByHeaderPosition > 0) {
                preOverlappedPosition =
                    adapter.getHeaderPositionForItem(overlappedByHeaderPosition - 1)
                overlappedHeaderPosition =
                    adapter.getHeaderPositionForItem(overlappedByHeaderPosition)
            } else {
                preOverlappedPosition = adapter.getHeaderPositionForItem(topChildPosition)
                overlappedHeaderPosition = preOverlappedPosition
            }

            if (preOverlappedPosition == RecyclerView.NO_POSITION) {
                return
            }

            if (preOverlappedPosition != overlappedHeaderPosition && shouldMoveHeader(
                    viewOverlappedByHeader
                )
            ) {
                updateStickyHeader(topChildPosition)
                moveHeader(c, viewOverlappedByHeader)
            } else {
                updateStickyHeader(topChildPosition)
                drawHeader(c)
            }
        }

        /**
         * Whether the sticky header should move or not.
         *
         * This method is for avoiding sinking/departing the sticky header into/from top of screen
         */
        private fun shouldMoveHeader(viewOverlappedByHeader: View): Boolean {
            val dy = viewOverlappedByHeader.top - viewOverlappedByHeader.height
            return viewOverlappedByHeader.top >= 0 && dy <= 0
        }

        private fun updateStickyHeader(topChildPosition: Int) {
            val currentStickyHolder = currentStickyHolder ?: return
            val headerPositionForItem = adapter.getHeaderPositionForItem(topChildPosition)

            if (headerPositionForItem != currentStickyPosition && headerPositionForItem != RecyclerView.NO_POSITION) {
                adapter.onBindHeaderViewHolder(currentStickyHolder, headerPositionForItem)
                currentStickyPosition = headerPositionForItem
            } else if (headerPositionForItem != RecyclerView.NO_POSITION) {
                adapter.onBindHeaderViewHolder(currentStickyHolder, headerPositionForItem)
            }
        }

        private fun drawHeader(c: Canvas) {
            c.save()
            c.translate(0f, 0f)
            currentStickyHolder!!.itemView.draw(c)
            c.restore()
        }

        private fun moveHeader(
            c: Canvas,
            nextHeader: View
        ) {
            c.save()
            c.translate(0f, nextHeader.top - nextHeader.height.toFloat())
            currentStickyHolder!!.itemView.draw(c)
            c.restore()
        }

        private fun getChildInContact(
            parent: RecyclerView,
            contactPoint: Int
        ): View? {
            var childInContact: View? = null

            for (i in 0 until parent.childCount) {
                val child = parent.getChildAt(i)

                if (child.bottom > contactPoint) {
                    if (child.top <= contactPoint) { // This child overlaps the contact point
                        childInContact = child
                        break
                    }
                }
            }

            return childInContact
        }

        private fun fixLayoutSize(recyclerView: RecyclerView) {
            val currentStickyHolder = currentStickyHolder ?: return

            recyclerView.addOnLayoutChangeListener(object : View.OnLayoutChangeListener {
                override fun onLayoutChange(
                    v: View?,
                    left: Int,
                    top: Int,
                    right: Int,
                    bottom: Int,
                    oldLeft: Int,
                    oldTop: Int,
                    oldRight: Int,
                    oldBottom: Int
                ) {
                    recyclerView.removeOnLayoutChangeListener(this)

                    // Specs for parent (RecyclerView)
                    val widthSpec = View.MeasureSpec.makeMeasureSpec(
                        recyclerView.width,
                        View.MeasureSpec.EXACTLY
                    )

                    val heightSpec = View.MeasureSpec.makeMeasureSpec(
                        recyclerView.height,
                        View.MeasureSpec.UNSPECIFIED
                    )

                    // Specs for children (headers)
                    val childWidthSpec = ViewGroup.getChildMeasureSpec(
                        widthSpec,
                        recyclerView.paddingLeft + recyclerView.paddingRight,
                        currentStickyHolder.itemView.layoutParams.width
                    )

                    val childHeightSpec = ViewGroup.getChildMeasureSpec(
                        heightSpec,
                        recyclerView.paddingTop + recyclerView.paddingBottom,
                        currentStickyHolder.itemView.layoutParams.height
                    )

                    currentStickyHolder.itemView.measure(childWidthSpec, childHeightSpec)
                    currentStickyHolder.itemView.layout(
                        0, 0,
                        currentStickyHolder.itemView.measuredWidth,
                        currentStickyHolder.itemView.measuredHeight
                    )
                }
            })
        }
    }
}