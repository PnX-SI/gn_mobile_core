package fr.geonature.commons.ui.adapter

import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

/**
 * Common interface about [RecyclerView.Adapter] with sticky headers.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see <a href="https://github.com/shuhart/StickyHeader">https://github.com/shuhart/StickyHeader</a>
 */
interface IStickyRecyclerViewAdapter<SVH : RecyclerView.ViewHolder> {

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
    fun getHeaderPositionForItem(itemPosition: Int): Int

    /**
     * This method gets called by [StickyHeaderItemDecorator] to setup the header View.
     *
     * @param holder         Holder to bind the data on.
     * @param headerPosition Position of the header item in the adapter.
     */
    fun onBindHeaderViewHolder(holder: SVH, headerPosition: Int)

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
    fun onCreateHeaderViewHolder(parent: ViewGroup): SVH
}