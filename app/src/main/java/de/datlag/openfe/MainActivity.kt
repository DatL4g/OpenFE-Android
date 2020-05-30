package de.datlag.openfe

import android.os.Bundle
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed

class MainActivity : AdvancedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    override fun onBackPressed() {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragment = navHostFragment?.childFragmentManager?.fragments?.get(0)
        val pressed: Boolean = (fragment as? FragmentBackPressed)?.onBackPressed() ?: true

        if(pressed) {
            super.onBackPressed()
        }
    }
}
