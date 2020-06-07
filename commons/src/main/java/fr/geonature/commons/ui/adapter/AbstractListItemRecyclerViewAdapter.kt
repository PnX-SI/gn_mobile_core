package fr.geonature.commons.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.LayoutRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView

/**
 * Base [RecyclerView.Adapter] that is backed by a [List] of arbitrary objects.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
abstract class AbstractListItemRecyclerViewAdapter<T>(private val listener: OnListItemRecyclerViewAdapterListener<T>? = null) :
    RecyclerView.Adapter<AbstractListItemRecyclerViewAdapter<T>.AbstractViewHolder>() {

    private val _items = mutableListOf<T>()
    val items: List<T> = _items

    init {
        this.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                listener?.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeChanged(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeChanged(
                    positionStart,
                    itemCount
                )

                listener?.showEmptyTextView(itemCount == 0)
            }

            override fun onItemRangeInserted(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeInserted(
                    positionStart,
                    itemCount
                )

                listener?.showEmptyTextView(false)
            }

            override fun onItemRangeRemoved(
                positionStart: Int,
                itemCount: Int
            ) {
                super.onItemRangeRemoved(
                    positionStart,
                    itemCount
                )

                listener?.showEmptyTextView(itemCount == 0)
            }
        })
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AbstractViewHolder {
        return getViewHolder(
            LayoutInflater.from(parent.context)
                .inflate(
                    viewType,
                    parent,
                    false
                ),
            viewType
        )
    }

    override fun getItemCount(): Int {
        return _items.size
    }

    override fun onBindViewHolder(
        holder: AbstractViewHolder,
        position: Int
    ) {
        holder.bind(_items[position])
    }

    override fun getItemViewType(position: Int): Int {
        return getLayoutResourceId(
            position,
            _items[position]
        )
    }

    /**
     * Sets new items.
     */
    fun setItems(newItems: List<T>) {
        if (this._items.isEmpty()) {
            this._items.addAll(newItems)

            if (this._items.isNotEmpty()) {
                notifyItemRangeInserted(
                    0,
                    this._items.size
                )
            } else {
                notifyDataSetChanged()
            }

            return
        }

        if (newItems.isEmpty()) {
            this._items.clear()
            notifyDataSetChanged()

            return
        }

        val diffResult = DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int {
                return this@AbstractListItemRecyclerViewAdapter._items.size
            }

            override fun getNewListSize(): Int {
                return newItems.size
            }

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@AbstractListItemRecyclerViewAdapter.areItemsTheSame(
                    this@AbstractListItemRecyclerViewAdapter._items,
                    newItems,
                    oldItemPosition,
                    newItemPosition
                )
            }

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ): Boolean {
                return this@AbstractListItemRecyclerViewAdapter.areContentsTheSame(
                    this@AbstractListItemRecyclerViewAdapter._items,
                    newItems,
                    oldItemPosition,
                    newItemPosition
                )
            }
        })

        this._items.clear()
        this._items.addAll(newItems)

        diffResult.dispatchUpdatesTo(this)
    }

    /**
     * Add or insert item at given position.
     */
    fun add(
        item: T,
        index: Int = -1
    ) {
        val position = if (index < 0 || index > this._items.size) this._items.size else index
        this._items.add(
            position,
            item
        )

        notifyItemInserted(position)
    }

    /**
     * Removes item from the list.
     *
     * @return item position if successfully removed, -1 otherwise
     */
    fun remove(item: T): Int {
        val itemPosition = this._items.indexOf(item)
        val removed = this._items.remove(item)

        if (removed) {
            notifyItemRemoved(itemPosition)

            if (this._items.isEmpty()) {
                notifyDataSetChanged()
            }
        }

        return if (removed) itemPosition else -1
    }

    /**
     * Clear the list.
     */
    fun clear(notify: Boolean = true) {
        this._items.clear()

        if (notify) {
            notifyDataSetChanged()
        }
    }

    /**
     * Gets the [AbstractViewHolder] implementation for given view type.
     */
    protected abstract fun getViewHolder(
        view: View,
        viewType: Int
    ): AbstractViewHolder

    /**
     * Gets the layout resource Id at given position.
     */
    @LayoutRes
    protected abstract fun getLayoutResourceId(
        position: Int,
        item: T
    ): Int

    /**
     * Called by the `DiffUtil` to decide whether two object represent the same item.
     */
    protected abstract fun areItemsTheSame(
        oldItems: List<T>,
        newItems: List<T>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean

    /**
     * Called by the `DiffUtil` when it wants to check whether two items have the same data.
     */
    protected abstract fun areContentsTheSame(
        oldItems: List<T>,
        newItems: List<T>,
        oldItemPosition: Int,
        newItemPosition: Int
    ): Boolean

    abstract inner class AbstractViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        open fun bind(item: T) {
            onBind(item)

            listener?.also { l ->
                with(itemView) {
                    setOnClickListener {
                        l.onClick(item)
                    }
                    setOnLongClickListener {
                        l.onLongClicked(
                            adapterPosition,
                            item
                        )
                        true
                    }
                }
            }
        }

        abstract fun onBind(item: T)
    }

    /**
     * Callback used by [AbstractListItemRecyclerViewAdapter].
     */
    interface OnListItemRecyclerViewAdapterListener<T> {

        /**
         * Called when an item has been clicked.
         *
         * @param item the selected item to edit
         */
        fun onClick(item: T)

        /**
         * Called when an item has been clicked and held.
         *
         * @param item the selected item
         */
        fun onLongClicked(
            position: Int,
            item: T
        )

        /**
         * Whether to show an empty text view when data changed.
         */
        fun showEmptyTextView(show: Boolean)
    }
}
