package com.dicoding.tanicare

import android.os.Bundle
import android.os.Handler
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.dicoding.tanicare.credentials.OnboardingFragment

class SplashActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_splash)

        // Apply window insets for edge-to-edge layout
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_container)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Menunggu beberapa detik (misalnya 2 detik) setelah animasi selesai
        Handler().postDelayed({
            // Ganti fragment setelah animasi selesai
            replaceToOnboardingFragment()
        }, 2000) // 2000 milidetik = 2 detik
    }

    private fun replaceToOnboardingFragment() {
        // Mendapatkan fragment transaction dan mengganti fragment
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.main_container, OnboardingFragment()) // Pastikan R.id.fragment_container ada di layout activity_splash
        fragmentTransaction.commit()
    }
}
