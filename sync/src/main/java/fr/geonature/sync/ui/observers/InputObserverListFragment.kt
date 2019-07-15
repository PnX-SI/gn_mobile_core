package fr.geonature.sync.ui.observers

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
import android.widget.ListView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.appcompat.widget.SearchView
import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.loader.app.LoaderManager
import androidx.loader.content.CursorLoader
import androidx.loader.content.Loader
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.data.InputObserver
import fr.geonature.commons.data.Provider.buildUri
import fr.geonature.sync.R
import kotlinx.android.synthetic.main.fast_scroll_recycler_view.*

/**
 * [Fragment] to let the user to choose an [InputObserver] from the list.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputObserverListFragment : Fragment() {

    private var listener: OnInputObserverListFragmentListener? = null
    private lateinit var adapter: InputObserverRecyclerViewAdapter
    private var progressBar: ProgressBar? = null
    private var emptyTextView: TextView? = null

    private val loaderCallbacks = object : LoaderManager.LoaderCallbacks<Cursor> {
        override fun onCreateLoader(id: Int,
                                    args: Bundle?): Loader<Cursor> {

            when (id) {
                LOADER_OBSERVERS -> {
                    val selections = if (args?.getString(KEY_FILTER,
                                                         null) == null) Pair(null,
                                                                             null)
                    else {
                        val filter = "%${args.getString(KEY_FILTER)}%"
                        Pair("(${InputObserver.COLUMN_LASTNAME} LIKE ? OR ${InputObserver.COLUMN_FIRSTNAME} LIKE ?)",
                             arrayOf(filter,
                                     filter))
                    }

                    return CursorLoader(requireContext(),
                                        buildUri(InputObserver.TABLE_NAME),
                                        arrayOf(InputObserver.COLUMN_ID,
                                                InputObserver.COLUMN_LASTNAME,
                                                InputObserver.COLUMN_FIRSTNAME),
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
                LOADER_OBSERVERS -> adapter.bind(data)
            }
        }

        override fun onLoaderReset(loader: Loader<Cursor>) {
            when (loader.id) {
                LOADER_OBSERVERS -> adapter.bind(null)
            }
        }
    }

    private var actionMode: ActionMode? = null
    private val actionModeCallback = object : ActionMode.Callback {
        override fun onCreateActionMode(mode: ActionMode?,
                                        menu: Menu?): Boolean {
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?,
                                         menu: Menu?): Boolean {
            return false
        }

        override fun onActionItemClicked(mode: ActionMode?,
                                         item: MenuItem?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            actionMode = null
            listener?.onSelectedInputObservers(adapter.getSelectedInputObservers())
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

        adapter =
            InputObserverRecyclerViewAdapter(object : InputObserverRecyclerViewAdapter.OnInputObserverRecyclerViewAdapterListener {
                override fun onSelectedInputObservers(inputObservers: List<InputObserver>) {
                    if (adapter.isSingleChoice()) {
                        listener?.onSelectedInputObservers(inputObservers)
                        return
                    }

                    updateActionMode(inputObservers)
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
        adapter.setChoiceMode(arguments?.getInt(ARG_CHOICE_MODE) ?: ListView.CHOICE_MODE_SINGLE)
        adapter.setSelectedInputObservers(arguments?.getParcelableArrayList(ARG_SELECTED_INPUT_OBSERVERS)
                                              ?: listOf())
            .also { updateActionMode(adapter.getSelectedInputObservers()) }

        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@InputObserverListFragment.adapter
        }

        val dividerItemDecoration = DividerItemDecoration(recyclerView.context,
                                                          (recyclerView.layoutManager as LinearLayoutManager).orientation)
        recyclerView.addItemDecoration(dividerItemDecoration)

        LoaderManager.getInstance(this)
            .initLoader(LOADER_OBSERVERS,
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
                LoaderManager.getInstance(this@InputObserverListFragment)
                    .restartLoader(LOADER_OBSERVERS,
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
                listener?.onSelectedInputObservers(emptyList())
                true
            }

            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context !is OnInputObserverListFragmentListener) {
            throw RuntimeException("$context must implement OnInputObserverListFragmentListener")
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

    private fun updateActionMode(inputObservers: List<InputObserver>) {
        if (inputObservers.isEmpty()) {
            actionMode?.finish()
            return
        }

        if (actionMode == null) {
            actionMode =
                (activity as AppCompatActivity?)?.startSupportActionMode(actionModeCallback)
            actionMode?.setTitle(R.string.activity_observers_title)
        }

        actionMode?.subtitle =
            resources.getQuantityString(R.plurals.action_title_item_count_selected,
                                        inputObservers.size,
                                        inputObservers.size)
    }

    /**
     * Callback used by [InputObserverListFragment].
     */
    interface OnInputObserverListFragmentListener {

        /**
         * Called when [InputObserver]s were been selected.
         *
         * @param inputObservers the selected [InputObserver]s
         */
        fun onSelectedInputObservers(inputObservers: List<InputObserver>)
    }

    companion object {

        private val TAG = InputObserverListFragment::class.java.name
        private const val ARG_CHOICE_MODE = "arg_choice_mode"
        private const val ARG_SELECTED_INPUT_OBSERVERS = "arg_selected_input_observers"
        private const val LOADER_OBSERVERS = 1
        private const val KEY_FILTER = "filter"

        /**
         * Use this factory method to create a new instance of [InputObserverListFragment].
         *
         * @return A new instance of [InputObserverListFragment]
         */
        @JvmStatic
        fun newInstance(choiceMode: Int = ListView.CHOICE_MODE_SINGLE,
                        selectedObservers: List<InputObserver> = listOf()) = InputObserverListFragment().apply {
            arguments = Bundle().apply {
                putInt(ARG_CHOICE_MODE,
                       choiceMode)
                putParcelableArrayList(ARG_SELECTED_INPUT_OBSERVERS,
                                       ArrayList(selectedObservers))
            }
        }
    }
}
