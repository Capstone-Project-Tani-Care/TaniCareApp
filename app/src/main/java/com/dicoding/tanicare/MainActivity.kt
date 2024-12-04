package com.dicoding.tanicare

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.FragmentTransaction
import com.dicoding.tanicare.credentials.OnboardingFragment

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Set the content view if you have any layout for the activity
        // setContentView(R.layout.activity_main)

        // Load OnboardingFragment directly when the activity is created
        if (savedInstanceState == null) {
            val transaction: FragmentTransaction = supportFragmentManager.beginTransaction()
            transaction.replace(android.R.id.content, HomeFragment()) // Replace the activity content with OnboardingFragment
            transaction.commit()
        }
    }
}