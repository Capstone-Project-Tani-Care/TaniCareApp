package com.dicoding.tanicare

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.fragment.app.FragmentContainerView
import androidx.navigation.findNavController
import com.dicoding.tanicare.credentials.LoginFragment
import com.dicoding.tanicare.helper.SharedPreferencesManager

class SplashActivity : AppCompatActivity() {

    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    override fun onCreate(savedInstanceState: Bundle?) {
        sharedPreferencesManager = SharedPreferencesManager(applicationContext)
        applyDarkMode()
        super.onCreate(savedInstanceState)
        //enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Check for auth token
        val authToken = sharedPreferencesManager.getAuthToken()

        Handler().postDelayed({
            // Get NavController
            val navController = findNavController(R.id.nav_host_fragment)

            if (authToken != null) {
                // Navigate to HomeFragment
                navController.navigate(R.id.action_global_homeFragment)
            } else {
                // Navigate to LoginFragment
                navController.navigate(R.id.action_global_loginFragment)
            }

            // Hide logo splash screen
            findViewById<ImageView>(R.id.logoImageView).visibility = View.GONE

            // Show NavHostFragment after splash
            findViewById<FragmentContainerView>(R.id.nav_host_fragment).visibility = View.VISIBLE
        }, 2000) // 2 seconds delay
    }

    private fun applyDarkMode() {
        val isDarkMode = sharedPreferencesManager.isDarkModeEnabled()
        if (isDarkMode) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
        }
    }
}

