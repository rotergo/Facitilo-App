package com.example.facitilov2

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class GoHomeFragment : Fragment() {

    private lateinit var etAddress: TextInputEditText
    private lateinit var btnSaveHome: MaterialButton
    private lateinit var tvCurrentAddress: TextView
    private lateinit var btnGoHome: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_go_home, container, false)

        etAddress = view.findViewById(R.id.etAddress)
        btnSaveHome = view.findViewById(R.id.btnSaveHome)
        tvCurrentAddress = view.findViewById(R.id.tvCurrentAddress)
        btnGoHome = view.findViewById(R.id.btnGoHome)

        loadSavedAddress()

        btnSaveHome.setOnClickListener {
            saveAddress()
        }

        btnGoHome.setOnClickListener {
            navigateToHome()
        }

        return view
    }

    private fun saveAddress() {
        val address = etAddress.text.toString()

        if (address.isBlank()) {
            Toast.makeText(context, "Por favor escribe una dirección", Toast.LENGTH_SHORT).show()
            return
        }

        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)

        // CORRECCIÓN IMPORTANTE:
        // Al guardar texto, borramos (.remove) las coordenadas antiguas para que no interfieran.
        prefs.edit()
            .putString("home_address_text", address)
            .remove("home_lat")
            .remove("home_lng")
            .apply()

        tvCurrentAddress.text = address
        etAddress.text?.clear()
        Toast.makeText(context, "¡Dirección guardada!", Toast.LENGTH_SHORT).show()
    }

    private fun loadSavedAddress() {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val savedAddress = prefs.getString("home_address_text", "Ninguna")
        tvCurrentAddress.text = savedAddress
    }

    private fun navigateToHome() {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val addressText = prefs.getString("home_address_text", "")

        if (addressText.isNullOrBlank() || addressText == "Ninguna") {
            Toast.makeText(context, "Primero guarda una dirección arriba", Toast.LENGTH_LONG).show()
            return
        }

        // Codificamos la dirección para que no haya problemas con espacios
        val encodedAddress = Uri.encode(addressText)

        // CAMBIO: Usamos el formato "Universal URL" de Google Maps.
        // destination = Tu dirección
        // travelmode = walking (Caminar)
        // dir_action = navigate (Forzar navegación paso a paso)
        val gmmIntentUri = Uri.parse("https://www.google.com/maps/dir/?api=1&destination=$encodedAddress&travelmode=walking&dir_action=navigate")

        val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
        mapIntent.setPackage("com.google.android.apps.maps")

        try {
            startActivity(mapIntent)
        } catch (e: Exception) {
            Toast.makeText(context, "Instala Google Maps", Toast.LENGTH_SHORT).show()
        }
    }
}