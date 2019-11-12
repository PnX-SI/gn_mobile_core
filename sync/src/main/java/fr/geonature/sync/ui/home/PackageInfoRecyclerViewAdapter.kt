package fr.geonature.sync.ui.home

import android.view.View
import android.widget.ImageView
import android.widget.TextView
import fr.geonature.commons.ui.adapter.ListItemRecyclerViewAdapter
import fr.geonature.sync.R
import fr.geonature.sync.sync.PackageInfo

/**
 * Default RecyclerView Adapter for [PackageInfo] used by [HomeActivity].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 *
 * @see HomeActivity
 */
class PackageInfoRecyclerViewAdapter(listener: OnListItemRecyclerViewAdapterListener<PackageInfo>) : ListItemRecyclerViewAdapter<PackageInfo>(listener) {
    override fun getViewHolder(view: View,
                               viewType: Int): AbstractViewHolder {
        return ViewHolder(view)
    }

    override fun getLayoutResourceId(position: Int,
                                     item: PackageInfo): Int {
        return R.layout.list_icon_item_2
    }

    override fun areItemsTheSame(oldItems: List<PackageInfo>,
                                 newItems: List<PackageInfo>,
                                 oldItemPosition: Int,
                                 newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition].packageName == newItems[newItemPosition].packageName
    }

    override fun areContentsTheSame(oldItems: List<PackageInfo>,
                                    newItems: List<PackageInfo>,
                                    oldItemPosition: Int,
                                    newItemPosition: Int): Boolean {
        return oldItems[oldItemPosition] == newItems[newItemPosition]
    }

    inner class ViewHolder(itemView: View) : ListItemRecyclerViewAdapter<PackageInfo>.AbstractViewHolder(itemView) {

        private val icon: ImageView = itemView.findViewById(android.R.id.icon)
        private val text1: TextView = itemView.findViewById(android.R.id.text1)
        private val text2: TextView = itemView.findViewById(android.R.id.text2)

        override fun onBind(item: PackageInfo) {
            icon.setImageDrawable(item.icon)
            text1.text = itemView.context.getString(R.string.home_app_version,
                                                    item.label,
                                                    item.versionName)
            text2.text = itemView.resources.getQuantityString(R.plurals.home_app_inputs,
                                                              item.inputs,
                                                              item.inputs)
        }
    }
}