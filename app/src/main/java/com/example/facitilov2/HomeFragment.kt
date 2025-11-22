package com.example.facitilov2

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.VibrationEffect
import android.os.Vibrator
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
        val prefs = context.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)

        // 1. Vibración (Feedback para el usuario)
        if (prefs.getBoolean("vibration_enabled", false)) {
            val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                vibrator.vibrate(VibrationEffect.createOneShot(500, VibrationEffect.DEFAULT_AMPLITUDE))
            } else {
                @Suppress("DEPRECATION")
                vibrator.vibrate(500)
            }
        }

        // 2. Verificar Contactos
        val contactCount = prefs.getInt("contact_count", 0)
        if (contactCount == 0) {
            Toast.makeText(context, "¡Error! No tienes contactos guardados.", Toast.LENGTH_LONG).show()
            return
        }

        // 3. Verificar Permisos y Enviar
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(context, "Obteniendo ubicación...", Toast.LENGTH_SHORT).show()

            val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                // CORRECCIÓN 1: Enlace limpio sin el "0" que rompía el mapa
                val mapLink = if (location != null) {
                    "http://maps.google.com/?q=${location.latitude},${location.longitude}"
                } else {
                    "Ubicación no disponible (GPS sin señal)"
                }

                val message = "¡AYUDA! Emergencia. Mi ubicación: $mapLink"

                // CORRECCIÓN 2: ¡Esta es la línea que faltaba! Ahora sí enviamos el mensaje.
                sendEmergencyAlerts(message)
            }
                .addOnFailureListener {
                    // Si falla el GPS, enviamos alerta sin ubicación para que al menos llegue algo
                    sendEmergencyAlerts("¡AYUDA! Emergencia (No se pudo obtener ubicación GPS)")
                }

        } else {
            Toast.makeText(context, "Falta permiso de ubicación", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 101)
        }
    }

    private fun sendEmergencyAlerts(message: String) {
        val context = requireContext()

        if (ContextCompat.checkSelfPermission(context, Manifest.permission.SEND_SMS) == PackageManager.PERMISSION_GRANTED) {

            // Soporte para versiones modernas de Android
            val smsManager = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                context.getSystemService(SmsManager::class.java)
            } else {
                @Suppress("DEPRECATION")
                SmsManager.getDefault()
            }

            val prefs = context.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
            val contactCount = prefs.getInt("contact_count", 0)
            var sentCount = 0
            var errorCount = 0

            for (i in 0 until contactCount) {
                val phone = prefs.getString("contact_${i}_phone", "")
                if (!phone.isNullOrBlank()) {
                    try {
                        // Divide el mensaje si es muy largo (importante para links)
                        val parts = smsManager.divideMessage(message)
                        smsManager.sendMultipartTextMessage(phone, null, parts, null, null)
                        sentCount++
                    } catch (e: Exception) {
                        e.printStackTrace()
                        errorCount++
                    }
                }
            }

            if (sentCount > 0) {
                Toast.makeText(context, "✅ Alerta enviada a $sentCount contactos", Toast.LENGTH_LONG).show()
            } else if (errorCount > 0) {
                Toast.makeText(context, "❌ Error al enviar SMS. Verifica tu saldo.", Toast.LENGTH_LONG).show()
            }

        } else {
            Toast.makeText(context, "Falta permiso de SMS", Toast.LENGTH_LONG).show()
            ActivityCompat.requestPermissions(requireActivity(), arrayOf(Manifest.permission.SEND_SMS), 100)
        }
    }
}