package com.keyauth.loader.ui

import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.keyauth.loader.R
import com.keyauth.loader.databinding.ActivityMainBinding
import com.keyauth.loader.ui.fragment.PubgVariantsFragment
import com.keyauth.loader.ui.fragment.ZeusPubgFragment
import com.keyauth.loader.ui.fragments.EnhancedSettingsFragment
import com.keyauth.loader.ui.fragments.UpdateFragment
import com.keyauth.loader.utils.LanguageManager
import com.keyauth.loader.viewmodel.AuthViewModel

/**
 * Main activity with bottom navigation and fragment-based architecture
 * Supports manual language switching and organized UI tabs
 */
class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var languageManager: LanguageManager

    // AuthViewModel for sharing user data across fragments
    private val authViewModel: AuthViewModel by viewModels()

    private var pubgVariantsFragment: ZeusPubgFragment? = null
    private var updateFragment: UpdateFragment? = null
    private var settingsFragment: EnhancedSettingsFragment? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        try {
            binding = ActivityMainBinding.inflate(layoutInflater)
            setContentView(binding.root)

            languageManager = LanguageManager(this)

            setupBottomNavigation()

            // Load PUBG variants fragment by default
            if (savedInstanceState == null) {
                loadFragment(getPubgVariantsFragment(), R.id.nav_home)
            }
        } catch (e: Exception) {
            // Log the error and finish the activity gracefully
            android.util.Log.e("MainActivity", "Error during onCreate", e)
            finish()
        }
    }

    private fun setupBottomNavigation() {
        binding.bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> {
                    loadFragment(getPubgVariantsFragment(), R.id.nav_home)
                    true
                }
                R.id.nav_update -> {
                    loadFragment(getUpdateFragment(), R.id.nav_update)
                    true
                }
                R.id.nav_settings -> {
                    loadFragment(getSettingsFragment(), R.id.nav_settings)
                    true
                }
                else -> false
            }
        }

        // Set default selection
        binding.bottomNavigation.selectedItemId = R.id.nav_home
    }

    private fun loadFragment(fragment: Fragment, navId: Int) {
        try {
            supportFragmentManager.beginTransaction()
                .replace(R.id.fragmentContainer, fragment)
                .commit()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error loading fragment", e)
        }
    }

    private fun getPubgVariantsFragment(): ZeusPubgFragment {
        if (pubgVariantsFragment == null) {
            pubgVariantsFragment = ZeusPubgFragment.newInstance()
        }
        return pubgVariantsFragment!!
    }

    private fun getUpdateFragment(): UpdateFragment {
        if (updateFragment == null) {
            updateFragment = UpdateFragment()
        }
        return updateFragment!!
    }

    private fun getSettingsFragment(): EnhancedSettingsFragment {
        if (settingsFragment == null) {
            settingsFragment = EnhancedSettingsFragment.newInstance()
        }
        return settingsFragment!!
    }

    /**
     * Called by SettingsFragment when language is changed
     * Updates all fragments with new language
     */
    fun updateLanguage() {
        // Update bottom navigation labels
        updateBottomNavigationLabels()

        // Update all fragments
        // Note: PubgVariantsFragment doesn't need language updates as it uses static content
        updateFragment?.updateLanguage()
        settingsFragment?.updateLanguage()
    }

    private fun updateBottomNavigationLabels() {
        val menu = binding.bottomNavigation.menu

        menu.findItem(R.id.nav_home)?.title = languageManager.getString("首页", "Home")
        menu.findItem(R.id.nav_update)?.title = languageManager.getString("更新", "Update")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("设置", "Settings")
    }
}
