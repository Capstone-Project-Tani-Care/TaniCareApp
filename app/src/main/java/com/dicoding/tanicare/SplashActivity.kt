package com.dicoding.tanicare

import android.os.Bundle
import android.os.Handler
import android.view.View
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentContainerView
import androidx.fragment.app.FragmentTransaction
import com.dicoding.tanicare.credentials.OnboardingFragment

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Delay untuk splash screen
        Handler().postDelayed({
            // Sembunyikan logo splash
            findViewById<ImageView>(R.id.logoImageView).visibility = View.GONE

            // Tampilkan NavHostFragment
            findViewById<FragmentContainerView>(R.id.nav_host_fragment).visibility = View.VISIBLE
        }, 1000)
    }
}
