package fr.geonature.sync.ui.taxa

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import fr.geonature.commons.data.Taxon
import fr.geonature.sync.R

/**
 * Let the user to choose an [Taxon] from the list.
 *
 * @see TaxaFragment
 *
 * @author [S. Grimault](mailto:sebastien.grimault@gmail.com)
 */
class TaxaActivity : AppCompatActivity(),
                     TaxaFragment.OnTaxaFragmentListener {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_toolbar)

        setSupportActionBar(findViewById(R.id.toolbar))
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = intent.getStringExtra("title")

        // Display the fragment as the main content.
        supportFragmentManager.beginTransaction()
            .replace(R.id.container,
                     TaxaFragment.newInstance())
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

    override fun onSelectedTaxon(taxon: Taxon) {
    }

    companion object {

        fun newIntent(context: Context): Intent {
            return Intent(context,
                          TaxaActivity::class.java)
        }
    }
}

