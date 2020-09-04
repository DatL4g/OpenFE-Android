package de.datlag.openfe

import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu

class MainActivity : AdvancedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun getCurrentNavFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        return navHostFragment?.childFragmentManager?.fragments?.get(0)
    }

    override fun onBackPressed() {
        val pressed: Boolean = (getCurrentNavFragment() as? FragmentBackPressed)?.onBackPressed() ?: true

        if(pressed) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return (getCurrentNavFragment() as? FragmentOptionsMenu)?.onCreateMenu(menu, menuInflater) ?: true
    }
}
