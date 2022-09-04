package fr.geonature.viewpager.model

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.DiffUtil

/**
 * [IPageFragment] view model.
 *
 * @author S. Grimault
 */
class PageFragmentViewModel : ViewModel() {
    private val _pageFragments = mutableMapOf<Int, IPageFragment>()
    val pageFragments: Map<Int, IPageFragment> = _pageFragments

    private val _onChanges = MutableLiveData<DiffUtil.DiffResult>()
    val onChanges: LiveData<DiffUtil.DiffResult> = _onChanges

    val size: Int get() = _pageFragments.size

    /**
     * Sets all pages.
     */
    fun set(vararg pageFragment: Pair<Int, IPageFragment>) {
        _onChanges.value = calculateDiff {
            _pageFragments.clear()
            _pageFragments.putAll(pageFragment)
        }
    }

    /**
     * Adds new pages at the end.
     */
    fun add(vararg pageFragment: Pair<Int, IPageFragment>) {
        _onChanges.value = calculateDiff {
            _pageFragments.putAll(pageFragment)
        }
    }

    /**
     * Removes existing pages from keys.
     */
    fun remove(vararg key: Int) {
        if (key.any { _pageFragments.keys.contains(it) }) {
            _onChanges.value = calculateDiff {
                key.forEach {
                    _pageFragments.remove(it)
                }
            }
        }
    }

    /**
     * Returns the current page from given position, or `null` otherwise.
     */
    fun getPageAtPosition(position: Int): IPageFragment? {
        return _pageFragments.entries.elementAtOrNull(position)?.value
    }

    /**
     * Returns the current position of the page from its key, or `null` if not found.
     */
    fun getPagePosition(key: Int): Int? {
        return _pageFragments.entries
            .indexOfLast { it.key == key }
            .takeIf { it >= 0 }
    }

    private fun calculateDiff(applyChanges: () -> Unit): DiffUtil.DiffResult {
        val oldKeys = _pageFragments.keys.toList()
        applyChanges()
        val newKeys = _pageFragments.keys.toList()

        return DiffUtil.calculateDiff(object : DiffUtil.Callback() {
            override fun getOldListSize(): Int =
                oldKeys.size

            override fun getNewListSize(): Int =
                newKeys.size

            override fun areItemsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) =
                oldKeys.elementAtOrNull(oldItemPosition) == newKeys.elementAtOrNull(newItemPosition)

            override fun areContentsTheSame(
                oldItemPosition: Int,
                newItemPosition: Int
            ) =
                areItemsTheSame(
                    oldItemPosition,
                    newItemPosition
                )
        })
    }
}