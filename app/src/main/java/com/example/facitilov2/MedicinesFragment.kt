package com.example.facitilov2

import android.Manifest
import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.util.Calendar

class MedicinesFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etDose: TextInputEditText
    private lateinit var tvTime: TextView
    private lateinit var btnSave: MaterialButton
    private lateinit var rvMedicines: RecyclerView
    private lateinit var adapter: MedicinesAdapter

    private val medicines = mutableListOf<Medicine>()
    private var selectedTime = ""
    private var medIdCounter = 0

    // Lanzador para pedir permiso de notificaciones
    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(context, "Permiso concedido. Vuelve a guardar.", Toast.LENGTH_SHORT).show()
        } else {
            Toast.makeText(context, "Sin permiso no podremos notificarte", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_medicines, container, false)

        etName = view.findViewById(R.id.etMedName)
        etDose = view.findViewById(R.id.etMedDose)
        tvTime = view.findViewById(R.id.tvSelectedTime)
        btnSave = view.findViewById(R.id.btnSaveMed)
        rvMedicines = view.findViewById(R.id.rvMedicines)

        loadMedicines()

        adapter = MedicinesAdapter(medicines) { med ->
            deleteMedicine(med)
        }
        rvMedicines.layoutManager = LinearLayoutManager(requireContext())
        rvMedicines.adapter = adapter

        tvTime.setOnClickListener { showTimePicker() }

        btnSave.setOnClickListener { saveMedicine() }

        // Pedir permiso al entrar si es Android 13+
        checkNotificationPermission()

        return view
    }

    private fun checkNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        TimePickerDialog(requireContext(), { _, selectedHour, selectedMinute ->
            val timeString = String.format("%02d:%02d", selectedHour, selectedMinute)
            selectedTime = timeString
            tvTime.text = timeString
        }, hour, minute, true).show()
    }

    private fun saveMedicine() {
        val name = etName.text.toString()
        val dose = etDose.text.toString()

        if (name.isBlank() || dose.isBlank() || selectedTime.isBlank()) {
            Toast.makeText(context, "Completa todos los campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear medicina y AGENDAR ALARMA
        val newMed = Medicine(medIdCounter++, name, dose, selectedTime)
        scheduleNotification(newMed) // <--- ¡AQUÍ ESTÁ LA CLAVE!

        adapter.addMedicine(newMed)
        saveToPrefs()

        etName.text?.clear()
        etDose.text?.clear()
        tvTime.text = "--:-- Toca para elegir hora"
        selectedTime = ""

        Toast.makeText(context, "¡Recordatorio programado!", Toast.LENGTH_SHORT).show()
    }

    private fun scheduleNotification(medicine: Medicine) {
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java).apply {
            putExtra("medicine_name", medicine.name)
            putExtra("message", "Dosis: ${medicine.dose}")
        }

        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            medicine.id, // ID único para cada alarma
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        // Parsear la hora (HH:mm)
        val parts = medicine.time.split(":")
        val hour = parts[0].toInt()
        val minute = parts[1].toInt()

        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, hour)
            set(Calendar.MINUTE, minute)
            set(Calendar.SECOND, 0)
        }

        // Si la hora ya pasó hoy, programarla para mañana
        if (calendar.timeInMillis <= System.currentTimeMillis()) {
            calendar.add(Calendar.DAY_OF_YEAR, 1)
        }

        try {
            alarmManager.setExactAndAllowWhileIdle(
                AlarmManager.RTC_WAKEUP,
                calendar.timeInMillis,
                pendingIntent
            )
        } catch (e: SecurityException) {
            Toast.makeText(context, "Error de permisos de alarma", Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteMedicine(med: Medicine) {
        // Cancelar alarma al borrar
        val alarmManager = requireContext().getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(requireContext(), NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            requireContext(),
            med.id,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        alarmManager.cancel(pendingIntent)

        adapter.removeMedicine(med)
        saveToPrefs()
        Toast.makeText(context, "Recordatorio cancelado", Toast.LENGTH_SHORT).show()
    }

    // ... (Mantén aquí las funciones saveToPrefs y loadMedicines igual que antes)
    private fun saveToPrefs() {
        val prefs = requireContext().getSharedPreferences("FacilitoMeds", Context.MODE_PRIVATE)
        val editor = prefs.edit()
        editor.putInt("med_count", medicines.size)
        medicines.forEachIndexed { index, med ->
            editor.putString("med_${index}_name", med.name)
            editor.putString("med_${index}_dose", med.dose)
            editor.putString("med_${index}_time", med.time)
        }
        editor.apply()
    }

    private fun loadMedicines() {
        val prefs = requireContext().getSharedPreferences("FacilitoMeds", Context.MODE_PRIVATE)
        val count = prefs.getInt("med_count", 0)
        medicines.clear()
        medIdCounter = 0
        for (i in 0 until count) {
            val name = prefs.getString("med_${i}_name", "") ?: ""
            val dose = prefs.getString("med_${i}_dose", "") ?: ""
            val time = prefs.getString("med_${i}_time", "") ?: ""
            if (name.isNotBlank()) {
                medicines.add(Medicine(medIdCounter++, name, dose, time))
            }
        }
    }
}