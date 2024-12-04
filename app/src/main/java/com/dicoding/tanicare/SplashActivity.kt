package com.dicoding.tanicare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.dicoding.tanicare.databinding.ActivitySplashBinding
import com.dicoding.tanicare.credentials.OnboardingFragment

class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        runEntryAnimation()
        binding.logoImageView.postDelayed({
            loadFragment(OnboardingFragment())
        }, 500)
    }

    private fun runEntryAnimation() {
        // Animasi fade-in pada logo
        binding.logoImageView.animate()
            .alpha(1f)
            .setDuration(1500)
            .start()


    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.main_container, fragment)
            .commit()
    }
}
