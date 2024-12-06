package com.dicoding.tanicare


import Zone
import android.content.Context
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.dicoding.tanicare.databinding.FragmentHomeBinding
import android.widget.ArrayAdapter
import androidx.appcompat.widget.SearchView
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.dicoding.tanicare.helper.SharedPreferencesManager
import loadZonesFromCsv

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferencesManager: SharedPreferencesManager

    private lateinit var zones: List<Zone>  // Daftar zona yang dibaca dari CSV

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        sharedPreferencesManager = SharedPreferencesManager(requireContext())
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Membaca data zona dari CSV
        zones = loadZonesFromCsv(requireContext())

        // Setup AutoCompleteTextView sebagai pengganti SearchView


        // Navigasi lainnya tetap sama
        binding.cardDiseasePrediction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_inputClassificationFragment)
        }

        binding.cardWeatherPrediction.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_weatherFragment)
        }

        binding.profileImage.setOnClickListener {
            findNavController().navigate(R.id.action_homeFragment_to_profileFragment)
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

        // Menampilkan SearchView saat home_location diklik
        binding.homeLocation.setOnClickListener {
            setupSearchView()
            binding.searchView.visibility = View.VISIBLE  // Menampilkan AutoCompleteTextView
            binding.searchView.requestFocus()  // Fokus pada AutoCompleteTextView
        }

        // Mengubah teks lokasi dengan nama kota yang sesuai dengan zona yang disimpan di SharedPreferences
        updateLocationText()
    }

    private fun setupSearchView() {
        // Memanggil fungsi untuk memuat data zona dari file CSV
        val zones = loadZonesFromCsv(requireContext())  // Pastikan zones dimuat sebelum digunakan

        // Membuat adapter untuk AutoCompleteTextView
        val cities = zones.map { it.cityName }
        val adapter = ArrayAdapter(requireContext(), android.R.layout.simple_dropdown_item_1line, cities)

        // Ganti SearchView dengan AutoCompleteTextView di layout
        val autoCompleteTextView = binding.searchView as AutoCompleteTextView
        autoCompleteTextView.setAdapter(adapter)

        // Menampilkan AutoCompleteTextView ketika home_location diklik
        binding.homeLocation.setOnClickListener {
            autoCompleteTextView.visibility = View.VISIBLE  // Menampilkan AutoCompleteTextView
            autoCompleteTextView.requestFocus()  // Fokus pada AutoCompleteTextView
        }

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
                // Menyaring kota yang sesuai dengan input pengguna
                val filteredCities = cities.filter { it.contains(query, ignoreCase = true) }
                adapter.clear()
                adapter.addAll(filteredCities)
                adapter.notifyDataSetChanged()
            }
        })

        // Ketika pengguna memilih lokasi dari saran
        autoCompleteTextView.setOnItemClickListener { parent, view, position, id ->
            val selectedCity = parent.getItemAtPosition(position).toString()
            val selectedZone = zones.find { it.cityName.equals(selectedCity, ignoreCase = true) }
            selectedZone?.let { zone ->
                // Simpan kode zona ke SharedPreferences
                sharedPreferencesManager.saveZoneCode(zone.code)
                Toast.makeText(requireContext(), "Zona disimpan: ${zone.cityName}", Toast.LENGTH_SHORT).show()
            }
            // Menyembunyikan AutoCompleteTextView setelah memilih
            autoCompleteTextView.visibility = View.GONE
        }
    }

    // Fungsi untuk memperbarui teks lokasi berdasarkan data zona yang disimpan di SharedPreferences
    private fun updateLocationText() {
        // Mengambil kode zona yang disimpan di SharedPreferences
        val savedZoneCode = sharedPreferencesManager.getZoneCode()

        // Mencocokkan kode zona yang disimpan dengan daftar zona
        val zone = zones.find { it.code == savedZoneCode }

        // Jika ditemukan, ubah teks lokasi dengan nama kota yang sesuai
        zone?.let {
            binding.locationText.text = it.cityName  // Ganti location_text dengan nama kota
        } ?: run {
            binding.locationText.text = "Lokasi tidak tersedia"  // Jika tidak ditemukan, beri pesan default
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}