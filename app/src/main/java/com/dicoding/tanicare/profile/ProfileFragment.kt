package com.dicoding.tanicare.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.dicoding.tanicare.R
import com.dicoding.tanicare.databinding.FragmentProfileBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var adapter: MyPhotoAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding = FragmentProfileBinding.bind(view)

        // Setup RecyclerView
        binding.photoRecyclerView.layoutManager = GridLayoutManager(context, 3) // 3 kolom
        adapter = MyPhotoAdapter(emptyList()) // Adapter kosong awal
        binding.photoRecyclerView.adapter = adapter

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                    findNavController().navigateUp()
            }
        })
        // Navigasi tombol back
        binding.iconBack.setOnClickListener {
            findNavController().navigateUp()
        }

        // Navigasi ke halaman pengaturan
        binding.iconSettings.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_settingsFragment)
        }

        // Navigasi ke halaman edit profil
        binding.editProfile.setOnClickListener {
            findNavController().navigate(R.id.action_profileFragment_to_editProfileFragment)
        }


    }
}
