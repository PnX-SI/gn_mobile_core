package fr.geonature.sync.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.util.Pair
import fr.geonature.sync.ui.home.HomeFragment

class MainActivity : AppCompatActivity(),
                     HomeFragment.OnHomeFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(android.R.id.content,
                     HomeFragment.newInstance())
            .commit()
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

    companion object {

        private val TAG = MainActivity::class.java.name
    }
}
