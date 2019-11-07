package fr.geonature.sync.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.AnimationUtils
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import fr.geonature.commons.ui.adapter.ListItemRecyclerViewAdapter
import fr.geonature.sync.R
import fr.geonature.sync.sync.DataSyncViewModel
import fr.geonature.sync.sync.PackageInfo
import fr.geonature.sync.sync.PackageInfoViewModel
import kotlinx.android.synthetic.main.fragment_home.*

/**
 * Home screen [Fragment].
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class HomeFragment : Fragment() {

    private var listener: OnHomeFragmentListener? = null
    private var adapter: PackageInfoRecyclerViewAdapter? = null

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

        adapter = PackageInfoRecyclerViewAdapter(object : ListItemRecyclerViewAdapter.OnListItemRecyclerViewAdapterListener<PackageInfo> {
            override fun onClick(item: PackageInfo) {
                listener?.onItemClicked(item)
            }

            override fun onLongClicked(position: Int,
                                       item: PackageInfo) {
                // nothing to do...
            }

            override fun showEmptyTextView(show: Boolean) {
                if (emptyTextView.visibility == View.VISIBLE == show) {
                    return
                }

                if (show) {
                    emptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                              android.R.anim.fade_in))
                    emptyTextView.visibility = View.VISIBLE

                }
                else {
                    emptyTextView.startAnimation(AnimationUtils.loadAnimation(context,
                                                                              android.R.anim.fade_out))
                    emptyTextView.visibility = View.GONE
                }
            }
        })

        with(appRecyclerView as RecyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = this@HomeFragment.adapter

            val dividerItemDecoration = DividerItemDecoration(context,
                                                              (layoutManager as LinearLayoutManager).orientation)
            addItemDecoration(dividerItemDecoration)
        }

        activity?.let { activity ->
            ViewModelProvider(activity).get(DataSyncViewModel::class.java)
                    .also { dataSyncViewModel ->
                        dataSyncViewModel.syncOutputStatus.observe(this,
                                                                   Observer {
                                                                       if (it == null || it.isEmpty()) {
                                                                           return@Observer
                                                                       }

                                                                       val workInfo = it[0]
                                                                       dataSyncView.setState(workInfo.state)
                                                                   })
                        dataSyncViewModel.lastSynchronizedDate.observe(this,
                                                                       Observer {
                                                                           dataSyncView.setLastSynchronizedDate(it)
                                                                       })
                        dataSyncViewModel.syncMessage.observe(this,
                                                              Observer {
                                                                  dataSyncView.setMessage(it)
                                                              })

                        dataSyncViewModel.startSync()
                    }
            ViewModelProvider(activity).get(PackageInfoViewModel::class.java)
                    .also { packageInfoViewModel ->
                        packageInfoViewModel.getInstalledApplications()
                                .observe(this,
                                         Observer {
                                             adapter?.setItems(it)
                                         })
                    }
        }
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
        fun onItemClicked(packageInfo: PackageInfo)
    }

    companion object {

        /**
         * Use this factory method to create a new instance of [HomeFragment].
         *
         * @return A new instance of [HomeFragment]
         */
        @JvmStatic
        fun newInstance() = HomeFragment()
    }
}
