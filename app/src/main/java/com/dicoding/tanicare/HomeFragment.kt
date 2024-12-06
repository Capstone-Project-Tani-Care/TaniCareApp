package com.dicoding.tanicare


import Zone
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentHomeBinding
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
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

    // Adapter untuk AutoCompleteTextView
    private lateinit var adapter: ArrayAdapter<String>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        // Inisialisasi ApiService dengan menggunakan ApiClient
        val retrofit = ApiClient.getClient() // Memanggil ApiClient untuk mendapatkan Retrofit instance
        apiService = retrofit.create(ApiService::class.java) // Mendapatkan ApiService instance

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupSearchView()

        // Navigasi dan interaksi UI lainnya
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

        // Memperbarui teks lokasi dari SharedPreferences
        updateLocationText()
    }

    private fun setupSearchView() {
        // Inisialisasi adapter kosong
        adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, mutableListOf())

        val autoCompleteTextView = binding.searchView as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        // Menangani saran nama kota ketika diketik
        autoCompleteTextView.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                // Tidak perlu apa-apa di sini
            }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Tidak perlu apa-apa di sini
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val query = s.toString()
                if (query.length >= 1) {
                    // Panggil API untuk mendapatkan suggestion berdasarkan query
                    getRegionSuggestions(query)
                } else {
                    // Clear hasil ketika input kosong
                    adapter.clear()
                    adapter.notifyDataSetChanged()
                }
            }
        })

        // Ketika pengguna memilih lokasi dari saran
        autoCompleteTextView.setOnItemClickListener { parent, _, position, _ ->
            val selectedCity = parent.getItemAtPosition(position).toString()
            // Simulasi: Simpan kode zona sesuai dengan kota yang dipilih
            getZoneCode(selectedCity) // Gantilah dengan logika yang sesuai
            Toast.makeText(requireContext(), "Zona disimpan: $selectedCity", Toast.LENGTH_SHORT).show()
            autoCompleteTextView.visibility = View.GONE
        }
    }

    // Fungsi untuk mendapatkan suggestion nama kota dari API
    private fun getRegionSuggestions(query: String) {
        apiService.getRegionName(query).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val data = response.body()?.get("data") as? List<Map<String, Any>>
                    val cityNames = data?.map { it["name"] as? String ?: "" } ?: emptyList()

                    // Update adapter dengan data yang diterima
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

    private fun getZoneCode(query: String) {
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        val apiService = ApiClient.getClient().create(ApiService::class.java)

        // Panggil getRegionCode dengan parameter query
        apiService.getRegionCode(query).enqueue(object : Callback<Map<String, Any>> {
            override fun onResponse(call: Call<Map<String, Any>>, response: Response<Map<String, Any>>) {
                if (response.isSuccessful) {
                    val zoneResponse = response.body()
                    if (zoneResponse != null) {
                        // Ambil data dari response yang sesuai dengan format yang diterima
                        val dataList = zoneResponse["data"] as? List<Map<String, Any>>
                        if (dataList != null && dataList.isNotEmpty()) {
                            // Ambil kode_wilayah dari data pertama dalam list
                            val zoneCode = dataList[0]["kode_wilayah"] as? String
                            if (zoneCode != null) {
                                // Simpan kode_wilayah ke sharedPreferences
                                sharedPreferencesManager.saveZoneCode(zoneCode)
                            } else {
                                Toast.makeText(requireContext(), "Kode wilayah tidak ditemukan", Toast.LENGTH_SHORT).show()
                            }
                        } else {
                            Toast.makeText(requireContext(), "Data tidak ditemukan atau kosong", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(requireContext(), "Fetch failed: Response body is null", Toast.LENGTH_SHORT).show()
                    }
                } else {
                    Toast.makeText(requireContext(), "Fetch failed: ${response.message()}", Toast.LENGTH_SHORT).show()
                }
            }

            override fun onFailure(call: Call<Map<String, Any>>, t: Throwable) {
                Toast.makeText(requireContext(), "Fetch failed: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }

    private fun updateLocationText() {
        // Mengambil kode zona yang disimpan di SharedPreferences
        val savedZoneCode = sharedPreferencesManager.getZoneCode()

        // Mencocokkan kode zona yang disimpan dengan zona yang sesuai (dapat diganti dengan API juga)
        if (savedZoneCode.isNotEmpty()) {
            binding.locationText.text = savedZoneCode  // Gantilah dengan nama kota sesuai kode zona
        } else {
            binding.locationText.text = "Lokasi tidak tersedia"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}

