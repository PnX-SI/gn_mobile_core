package fr.geonature.sync.ui.taxa

import android.content.Context
import android.database.Cursor
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.AbstractTaxon
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.commons.data.Taxon
import fr.geonature.sync.R
import kotlinx.android.synthetic.main.fast_scroll_recycler_view.*

/**
 * [Fragment] to let the user to choose an [InputObserver] from the list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaFragment : Fragment() {

    private var listener: OnTaxaFragmentListener? = null
    private lateinit var adapter: TaxaRecyclerViewAdapter
    private var progressBar: ProgressBar? = null
    private var emptyTextView: TextView? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int,
                                    args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_TAXA -> {
                    val selections = if (args?.getString(KEY_FILTER,
                                                         null) == null) Pair(null,
                                                                             null)
                    else {
                        val filter = "%${args.getString(KEY_FILTER)}%"
                        Pair("(${AbstractTaxon.COLUMN_NAME} LIKE ?)",
                             arrayOf(filter))
                    }

                    return CursorLoader(requireContext(),
                                        buildUri(Taxon.TABLE_NAME,
                                                 "area/123"),
                                        null,
                                        selections.first,
                                        selections.second,
                                        null)
                }

                else -> throw IllegalArgumentException()
            }
        }

        override fun onLoadFinished(loader: Loader<Cursor>,
                                    data: Cursor?) {

            showView(progressBar,
                     false)

            if (data == null) {
                Log.w(TAG,
                      "Failed to load data from '${(loader as CursorLoader).uri}'")

                return
            }

            when (loader.id) {
                LOADER_TAXA -> adapter.bind(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_TAXA -> adapter.bind(null)
            }
        }
    }

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        val view = inflater.inflate(R.layout.fast_scroll_recycler_view,
                                    container,
                                    false)

        progressBar = view.findViewById(R.id.progressBar)
        emptyTextView = view.findViewById(R.id.emptyTextView)

        showView(progressBar,
                 true)

        return view
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        // we have a menu item to show in action bar
        setHasOptionsMenu(true)

        adapter = TaxaRecyclerViewAdapter(object : TaxaRecyclerViewAdapter.OnTaxaRecyclerViewAdapterListener {
            override fun onSelectedTaxon(taxon: Taxon) {
                listener?.onSelectedTaxon(taxon)
            }

            override fun scrollToFirstSelectedItemPosition(position: Int) {
                recyclerView.smoothScrollToPosition(position)
            }
        })
        adapter.registerAdapterDataObserver(object : RecyclerView.AdapterDataObserver() {
            override fun onChanged() {
                super.onChanged()

                showView(emptyTextView,
                         adapter.itemCount == 0)
            }

            override fun onItemRangeChanged(positionStart: Int,
                                            itemCount: Int) {
                super.onItemRangeChanged(positionStart,
                                         itemCount)

                showView(emptyTextView,
                         adapter.itemCount == 0)
            }

            override fun onItemRangeInserted(positionStart: Int,
                                             itemCount: Int) {
                super.onItemRangeInserted(positionStart,
                                          itemCount)

                showView(emptyTextView,
                         false)
            }
        })

        val selectedTaxon: Taxon? = arguments?.getParcelable(ARG_SELECTED_TAXON)

        if (selectedTaxon != null) {
            adapter.setSelectedTaxon(selectedTaxon)
        }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@TaxaFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                                                          (recyclerView.layoutManager as LinearLayoutManager).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        LoaderManager.getInstance(this)
            .initLoader(LOADER_TAXA,
                        null,
                        loaderCallbacks)
    }

    override fun onCreateOptionsMenu(menu: Menu,
                                     inflater: MenuInflater) {

        super.onCreateOptionsMenu(menu,
                                  inflater)

        inflater.inflate(R.menu.search,
                         menu)

        val searchItem = menu.findItem(R.id.action_search)
        val searchView = searchItem.actionView as SearchView
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                LoaderManager.getInstance(this@TaxaFragment)
                    .restartLoader(LOADER_TAXA,
                                   bundleOf(Pair(KEY_FILTER,
                                                 newText)),
                                   loaderCallbacks)

                return true
            }
        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                val selectedTaxon = adapter.getSelectedTaxon()

                if (selectedTaxon != null) {
                    listener?.onSelectedTaxon(selectedTaxon)
                }

                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnTaxaFragmentListener) {
            throw RuntimeException("$context must implement OnTaxaFragmentListener")
        }

        listener = context
    }

    override fun onDetach() {
        super.onDetach()

        listener = null
    }

    private fun showView(view: View?,
                         show: Boolean) {
        if (view == null) return

        if (view.visibility == View.VISIBLE == show) {
            return
        }

        if (show) {
            view.startAnimation(AnimationUtils.loadAnimation(context,
                                                             android.R.anim.fade_in))
            view.visibility = View.VISIBLE

        }
        else {
            view.postDelayed({
                                 view.startAnimation(AnimationUtils.loadAnimation(context,
                                                                                  android.R.anim.fade_out))
                                 view.visibility = View.GONE
                             },
                             500)
        }
    }

    /**
     * Callback used by [TaxaFragment].
     */
    interface OnTaxaFragmentListener {

        /**
         * Called when a [Taxon] has been selected.
         *
         * @param taxon the selected [Taxon]
         */
        fun onSelectedTaxon(taxon: Taxon)
    }

    companion object {

        private val TAG = TaxaFragment::class.java.name
        private const val ARG_SELECTED_TAXON = "arg_selected_taxon"
        private const val LOADER_TAXA = 1
        private const val KEY_FILTER = "filter"

        /**
         * Use this factory method to create a new instance of [TaxaFragment].
         *
         * @return A new instance of [TaxaFragment]
         */
        @JvmStatic
        fun newInstance(selectedTaxon: Taxon? = null) = TaxaFragment().apply {
            arguments = Bundle().apply {
                putParcelable(ARG_SELECTED_TAXON,
                              selectedTaxon)
            }
        }
    }
}
