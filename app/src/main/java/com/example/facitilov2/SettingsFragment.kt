package com.example.facitilov2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var switchLargeText: SwitchMaterial
    private lateinit var switchLoudSound: SwitchMaterial
    private lateinit var btnAbout: MaterialButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        switchLargeText = view.findViewById(R.id.switchLargeText)
        switchLoudSound = view.findViewById(R.id.switchLoudSound)
        btnAbout = view.findViewById(R.id.btnAbout)

        loadPreferences()

        switchLargeText.setOnCheckedChangeListener { _, isChecked ->
            savePreference("large_text", isChecked)
            if (isChecked) {
                Toast.makeText(context, "Modo Texto Grande activado", Toast.LENGTH_SHORT).show()
            }
        }

        switchLoudSound.setOnCheckedChangeListener { _, isChecked ->
            savePreference("loud_sound", isChecked)
        }

        btnAbout.setOnClickListener {
            Toast.makeText(context, "Facilito App v1.0\nCreado para ayudar", Toast.LENGTH_LONG).show()
        }

        switchLargeText.setOnCheckedChangeListener { _, isChecked ->
            savePreference("large_text", isChecked)

            // ESTA LÍNEA ES LA CLAVE: Reinicia la app para aplicar el cambio al instante
            requireActivity().recreate()
        }

        return view
    }

    private fun savePreference(key: String, value: Boolean) {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        prefs.edit().putBoolean(key, value).apply()
    }

    private fun loadPreferences() {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        switchLargeText.isChecked = prefs.getBoolean("large_text", false)
        switchLoudSound.isChecked = prefs.getBoolean("loud_sound", false)
    }
}