package fr.geonature.commons.ui.adapter

import androidx.recyclerview.widget.RecyclerView

/**
 * RecyclerView adapter with sticky headers.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see <a href="https://github.com/shuhart/StickyHeader">https://github.com/shuhart/StickyHeader</a>
 */
abstract class AbstractStickyRecyclerViewAdapter<SVH : RecyclerView.ViewHolder, VH : RecyclerView.ViewHolder> :
    RecyclerView.Adapter<VH>(),
    IStickyRecyclerViewAdapter<SVH> {

    override fun onAttachedToRecyclerView(recyclerView: RecyclerView) {
        super.onAttachedToRecyclerView(recyclerView)

        recyclerView.addItemDecoration(
            StickyHeaderItemDecorator(
                this,
                recyclerView
            )
        )
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)

        for (i in 0 until recyclerView.itemDecorationCount) {
            val decorator = recyclerView.getItemDecorationAt(i)

            if (decorator is StickyHeaderItemDecorator<*>) {
                recyclerView.removeItemDecoration(decorator)
            }
        }
    }
}
