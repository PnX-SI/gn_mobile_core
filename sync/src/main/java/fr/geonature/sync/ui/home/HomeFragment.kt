package fr.geonature.sync.ui.home

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.util.Pair
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.sync.R
import fr.geonature.sync.ui.observers.InputObserverListActivity
import fr.geonature.sync.ui.taxa.TaxaActivity

/**
 * A fragment representing a list of Items.
 * Activities containing this fragment MUST implement the
 * [HomeFragment.OnHomeFragmentListener] interface.
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeFragment : Fragment() {

    private var listener: OnHomeFragmentListener? = null
    private lateinit var adapter: HomeRecyclerViewAdapter

    override fun onCreateView(inflater: LayoutInflater,
                              container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home,
                                container,
                                false)
    }

    override fun onViewCreated(view: View,
                               savedInstanceState: Bundle?) {
        super.onViewCreated(view,
                            savedInstanceState)

        adapter =
            HomeRecyclerViewAdapter(object : HomeRecyclerViewAdapter.OnHomeRecyclerViewAdapterListener {
                override fun onItemClicked(itemIntent: Pair<String, Intent>) {
                    Log.i(TAG,
                          "onItemClicked: ${itemIntent.first}")

                    listener?.onItemClicked(itemIntent)
                }
            })

        // Set the adapter
        if (view is RecyclerView) {
            with(view) {
                layoutManager = LinearLayoutManager(context)
                adapter = this@HomeFragment.adapter
            }

            val dividerItemDecoration = DividerItemDecoration(view.context,
                                                              (view.layoutManager as LinearLayoutManager).orientation)
            view.addItemDecoration(dividerItemDecoration)
        }

        adapter.setItems(listOf(Pair.create("Observers",
                                            InputObserverListActivity.newIntent(requireContext())),
                                Pair.create("Taxa",
                                            TaxaActivity.newIntent(requireContext()))))
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)

        if (context is OnHomeFragmentListener) {
            listener = context
        }
        else {
            throw RuntimeException("$context must implement OnHomeFragmentListener")
        }
    }

    override fun onDetach() {
        super.onDetach()
        listener = null
    }

    /**
     * Callback used by [HomeFragment].
     */
    interface OnHomeFragmentListener {
        fun onItemClicked(itemIntent: Pair<String, Intent>)
    }

    companion object {

        private val TAG = HomeFragment::class.java.name

        /**
         * Use this factory method to create a new instance of [HomeFragment].
         *
         * @return A new instance of [HomeFragment]
         */
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
