package fr.geonature.sync.ui.observers

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.InputObserver
import fr.geonature.sync.R

/**
 * Let the user to choose an [InputObserver] from the list.
 *
 * @see InputObserverListFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class InputObserverListActivity : AppCompatActivity(),
                                  InputObserverListFragment.OnInputObserverListFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("title")

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                     InputObserverListFragment.newInstance())
            .commit()
    }

    override fun onOptionsItemSelected(item: MenuItem?): Boolean {
        return when (item?.itemId ?: return super.onOptionsItemSelected(item)) {
            android.R.id.home -> {
                finish()
                return true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onSelectedInputObservers(inputObservers: List<InputObserver>) {
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context,
                          InputObserverListActivity::class.java)
        }
    }
}
