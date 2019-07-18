package fr.geonature.sync.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import androidx.lifecycle.ViewModelProviders
import fr.geonature.sync.ui.home.HomeFragment
import fr.geonature.sync.viewmodel.SyncViewModel

class MainActivity : AppCompatActivity(),
                     HomeFragment.OnHomeFragmentListener {

    private lateinit var syncViewModel: SyncViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        syncViewModel = ViewModelProviders.of(this)
            .get(SyncViewModel::class.java)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content,
                     HomeFragment.newInstance())
            .commit()

        startSync()
    }

    override fun onItemClicked(itemIntent: Pair<String, Intent>) {
        Log.i(TAG,
              "onItemClicked: ${itemIntent.first}")

        val intent = itemIntent.second ?: return

        startActivity(intent.apply {
            putExtra("title",
                     itemIntent.first)
        })
    }

    private fun startSync() {
        syncViewModel.startSync()
    }

    companion object {
        private val TAG = MainActivity::class.java.name
    }
}
