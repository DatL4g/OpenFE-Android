package de.datlag.openfe

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.Menu
import androidx.fragment.app.Fragment
import de.datlag.openfe.extend.AdvancedActivity
import de.datlag.openfe.interfaces.FragmentBackPressed
import de.datlag.openfe.interfaces.FragmentOptionsMenu
import timber.log.Timber

class MainActivity : AdvancedActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
    }

    private fun getCurrentNavFragment(): Fragment? {
        val navHostFragment = supportFragmentManager.primaryNavigationFragment
        val fragmentList = navHostFragment?.childFragmentManager?.fragments
        return if (!fragmentList.isNullOrEmpty() && fragmentList.size >= 1) fragmentList[0] else null
    }

    override fun onBackPressed() {
        val pressed: Boolean = (getCurrentNavFragment() as? FragmentBackPressed?)?.onBackPressed() ?: true

        if (pressed) {
            super.onBackPressed()
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        return (getCurrentNavFragment() as? FragmentOptionsMenu?)?.onCreateMenu(menu, menuInflater) ?: true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Timber.e("activity called")

        when (requestCode) {
            1337 -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    Timber.e("activity granted")
                } else {
                    Timber.e("activity not granted")
                }
            }
        }
    }
}
