package com.dicoding.tanicare.profile

import android.os.Bundle
import android.view.View
import android.widget.Toast
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

        // Panggil API untuk mendapatkan data foto
        fetchPhotos()
    }

    private fun fetchPhotos() {
        val apiService = ApiClient.getClient().create(ApiService::class.java)
        apiService.getPhotos().enqueue(object : Callback<List<String>> {
            override fun onResponse(call: Call<List<String>>, response: Response<List<String>>) {
                if (response.isSuccessful) {
                    val photos = response.body() ?: emptyList()
                    adapter = MyPhotoAdapter(photos) // Update adapter dengan data API
                    binding.photoRecyclerView.adapter = adapter
                } else {
                    Toast.makeText(context, "Gagal memuat data", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<List<String>>, t: Throwable) {
                Toast.makeText(context, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
