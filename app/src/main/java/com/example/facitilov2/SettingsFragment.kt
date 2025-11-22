package com.example.facitilov2

import android.content.Context
import android.media.AudioManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.slider.Slider
import com.google.android.material.switchmaterial.SwitchMaterial

class SettingsFragment : Fragment() {

    private lateinit var sliderTextSize: Slider
    private lateinit var sliderVolume: Slider
    private lateinit var switchVibration: SwitchMaterial
    private lateinit var switchBoldText: SwitchMaterial

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_settings, container, false)

        sliderTextSize = view.findViewById(R.id.sliderTextSize)
        sliderVolume = view.findViewById(R.id.sliderVolume)
        switchVibration = view.findViewById(R.id.switchVibration)
        switchBoldText = view.findViewById(R.id.switchBoldText)

        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val audioManager = requireContext().getSystemService(Context.AUDIO_SERVICE) as AudioManager

        // 1. Configurar Slider de Texto (Cargar valor guardado)
        val savedScale = prefs.getFloat("text_scale", 1.0f)
        sliderTextSize.value = savedScale

        sliderTextSize.addOnChangeListener { _, value, _ ->
            prefs.edit().putFloat("text_scale", value).apply()
            // Reiniciamos la actividad para aplicar el cambio de tamaño
            // Solo lo hacemos cuando el usuario suelta el dedo (opcional, aquí lo hace al mover)
        }
        // Truco: Aplicar el cambio solo cuando suelta el slider para no parpadear tanto
        sliderTextSize.addOnSliderTouchListener(object : Slider.OnSliderTouchListener {
            override fun onStartTrackingTouch(slider: Slider) {}
            override fun onStopTrackingTouch(slider: Slider) {
                requireActivity().recreate()
            }
        })

        // 2. Configurar Slider de Volumen (Controlar volumen real)
        val maxVolume = audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC).toFloat()
        val currentVolume = audioManager.getStreamVolume(AudioManager.STREAM_MUSIC).toFloat()

        sliderVolume.valueFrom = 0f
        sliderVolume.valueTo = maxVolume
        sliderVolume.value = currentVolume

        sliderVolume.addOnChangeListener { _, value, _ ->
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, value.toInt(), 0)
        }

        // 3. Configurar Interruptores
        switchVibration.isChecked = prefs.getBoolean("vibration_enabled", false)
        switchVibration.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("vibration_enabled", isChecked).apply()
        }

        switchBoldText.isChecked = prefs.getBoolean("bold_text", false)
        switchBoldText.setOnCheckedChangeListener { _, isChecked ->
            prefs.edit().putBoolean("bold_text", isChecked).apply()
            requireActivity().recreate() // Reiniciar para aplicar negrita
        }

        return view
    }
}