package com.dicoding.tanicare

import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.setupWithNavController
import com.bumptech.glide.Glide
import com.dicoding.tanicare.databinding.FragmentHomeBinding
import com.dicoding.tanicare.helper.ApiClient
import com.dicoding.tanicare.helper.ApiService
import com.dicoding.tanicare.helper.SharedPreferencesManager
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferencesManager: SharedPreferencesManager
    private lateinit var apiService: ApiService
    private lateinit var adapter: ArrayAdapter<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        apiService = ApiClient.getClient().create(ApiService::class.java) // Inisialisasi API Service
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.bottomNav.selectedItemId = R.id.homeFragment
        setupSearchView() // Atur AutoCompleteTextView

        // Setup navigasi
        setupNavigation()

        // Update info pengguna
        updateUserInfo()

    }

    private fun setupNavigation() {
        binding.cardDiseasePrediction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_inputClassificationFragment)
        }

        binding.cardWeatherPrediction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_weatherFragment)
        }

        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
        }

        binding.threadSeeAll.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_threadFragment)
        }

        binding.bottomNav.setOnNavigationItemSelectedListener { item ->
            when (item.itemId) {
                R.id.historyFragment -> {
                    findNavController().navigate(R.id.action_homeFragment_to_historyFragment)
                    true
                }
                R.id.profileFragment -> {
                    findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
                    true
                }
                else -> false
            }
        }


        binding.homeLocation.setOnClickListener {
            binding.searchView.visibility = View.VISIBLE
            binding.searchView.requestFocus()
        }
    }

    private fun setupSearchView() {
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())
        val autoCompleteTextView = binding.searchView as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        // Listener untuk menangani input teks
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {}
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.length >= 1) {
                    getRegionSuggestions(query) // Panggil API untuk mendapatkan saran
                } else {
                    adapter.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        })

        // Listener untuk menangani item yang dipilih
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position).toString()
            getZoneCodeAndUpdateLocation(selectedCity) // Ambil zone code dan update lokasi
            autoCompleteTextView.visibility = View.GONE
        }
    }

    private fun getRegionSuggestions(query: String) {
        apiService.getRegionName(query).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? List<Map<String, Any>>
                    val cityNames = data?.map { it["name"] as? String ?: "" } ?: emptyList()
                    adapter.clear()
                    adapter.addAll(cityNames)
                    adapter.notifyDataSetChanged()
                } else {
                    Log.e("API Error", "Response not successful")
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Log.e("API Failure", t.message ?: "Unknown error")
            }
        })
    }

    private fun getZoneCodeAndUpdateLocation(selectedCity: String) {
        apiService.getRegionCode(selectedCity).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? List<Map<String, Any>>
                    val zoneCode = data?.firstOrNull()?.get("kode_wilayah") as? String
                    if (zoneCode != null) {
                        sharedPreferencesManager.saveZoneCode(zoneCode)
                        updateLocation(zoneCode) // Kirim zone code ke API setelah diperoleh
                    } else {
                        Toast.makeText(requireContext(), "Kode wilayah tidak ditemukan", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Gagal mendapatkan kode wilayah: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLocation(zoneCode: String) {
        val token = sharedPreferencesManager.getAuthToken() ?: run {
            Toast.makeText(requireContext(), "Authorization token is missing", Toast.LENGTH_SHORT).show()
            return
        }

        val requestBody = mapOf("kode_wilayah" to zoneCode)
        apiService.editProfileLocation("Bearer $token", requestBody).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    Toast.makeText(requireContext(), "Location updated successfully", Toast.LENGTH_SHORT).show()
                } else {
                    Toast.makeText(requireContext(), "Failed to update location: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateUserInfo() {
        val userId = sharedPreferencesManager.getUserId() ?: run {
            Toast.makeText(requireContext(), "User ID is missing", Toast.LENGTH_SHORT).show()
            return
        }

        apiService.getUserInfo(userId).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? Map<String, Any>
                    if (data != null) {
                        val about = data["about"] as? String ?: ""
                        val zoneCode = data["location"] as? String ?: ""
                        val name = data["name"] as? String ?: "Unknown"
                        val zoneName = data["region_name"] as? String ?: ""
                        val image = data["profile_photo"] as? String ?: ""

                        sharedPreferencesManager.saveZoneCode(zoneCode)
                        sharedPreferencesManager.saveUserInfo(zoneName, name, about, image)
                        binding.welcomeText.text = "Welcome $name"
                        binding.locationText.text = zoneName
                        val imageUrl = sharedPreferencesManager.getImageUrl()
                        Glide.with(requireContext())
                            .load(imageUrl)
                            .placeholder(R.drawable.ic_profile_placeholder) // Gambar placeholder
                            .error(R.drawable.ic_profile_placeholder) // Gambar jika gagal memuat
                            .into(binding.profileImage)
                    } else {
                        Toast.makeText(requireContext(), "Failed to parse user data", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Failed to fetch user info: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
