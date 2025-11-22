package com.example.facitilov2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Bundle
import android.telephony.SmsManager
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton

class HomeFragment : Fragment() {

    private lateinit var btnEmergency: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        btnEmergency = view.findViewById(R.id.btnEmergency)

        btnEmergency.setOnClickListener {
            handleEmergency()
        }

        return view
    }

    private fun handleEmergency() {
        val context = requireContext()

        // 1. Verificar si hay contactos guardados
        val prefs = context.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val contactCount = prefs.getInt("contact_count", 0)

        if (contactCount == 0) {
            Toast.makeText(context, "Agrega contactos primero en la pestaña 'Contactos'", Toast.LENGTH_LONG).show()
            return
        }

        // 2. Verificar permiso de ubicación
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Toast.makeText(context, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // AQUÍ ESTABA EL ERROR: Usamos un link limpio y directo con las coordenadas
                val mapLink = if (location != null) {
                    "https://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    "Ubicación no disponible"
                }

                val message = "¡AYUDA! Emergencia. Mi ubicación: $mapLink"
                sendEmergencyAlerts(message)
            }
        } else {
            Toast.makeText(context, "Falta permiso de ubicación", Toast.LENGTH_LONG).show()
            // Opcional: pedir permiso aquí
        }
    }

    private fun sendEmergencyAlerts(message: String) {
        val context = requireContext()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {
            val smsManager = SmsManager.getDefault()
            val prefs = context.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
            val contactCount = prefs.getInt("contact_count", 0)
            var sentCount = 0

            for (i in 0 until contactCount) {
                val phone = prefs.getString("contact_${i}_phone", "")
                if (!phone.isNullOrBlank()) {
                    try {
                        smsManager.sendTextMessage(phone, null, message, null, null)
                        sentCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                    }
                }
            }

            if (sentCount > 0) {
                Toast.makeText(context, "¡ALERTA ENVIADA A $sentCount CONTACTOS!", Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(context, "Error al enviar. Revisa el saldo o la señal.", Toast.LENGTH_SHORT).show()
            }

        } else {
            Toast.makeText(context, "Falta permiso de SMS", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 100)
        }
    }
}