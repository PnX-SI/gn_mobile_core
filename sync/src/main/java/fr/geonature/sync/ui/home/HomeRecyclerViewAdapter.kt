package fr.geonature.sync.ui.home

import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.util.Pair
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.sync.ui.home.HomeRecyclerViewAdapter.OnHomeRecyclerViewAdapterListener

/**
 * [RecyclerView.Adapter] that can display item and makes a call to the
 * specified [OnHomeRecyclerViewAdapterListener].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeRecyclerViewAdapter(private val listener: OnHomeRecyclerViewAdapterListener) : RecyclerView.Adapter<HomeRecyclerViewAdapter.ViewHolder>() {

    private val itemIntents: MutableList<Pair<String, Intent>> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup,
                                    viewType: Int): ViewHolder {
        return ViewHolder(parent)

    }

    override fun onBindViewHolder(holder: ViewHolder,
                                  position: Int) {
        holder.bind(itemIntents[position])
    }

    override fun getItemCount(): Int = itemIntents.size

    fun setItems(items: List<Pair<String, Intent>>) {
        this.itemIntents.clear()
        this.itemIntents.addAll(items)

        notifyDataSetChanged()
    }

    inner class ViewHolder(parent: ViewGroup) : RecyclerView.ViewHolder(LayoutInflater.from(parent.context).inflate(android.R.layout.simple_list_item_1,
                                                                                                                    parent,
                                                                                                                    false)) {

        private val text1: TextView = itemView.findViewById(android.R.id.text1)

        fun bind(itemIntent: Pair<String, Intent>) {
            text1.text = itemIntent.first
            itemView.setOnClickListener { listener.onItemClicked(itemIntent) }
        }
    }

    /**
     * Callback used by [HomeRecyclerViewAdapter].
     */
    interface OnHomeRecyclerViewAdapterListener {

        /**
         * Called when an item has been clicked.
         *
         * @param itemIntent the selected item
         */
        fun onItemClicked(itemIntent: Pair<String, Intent>)
    }
}
