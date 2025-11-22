package com.example.facitilov2

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        // 1. Aplicar Tema de Negrita ANTES de crear la vista
        val prefs = getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        if (prefs.getBoolean("bold_text", false)) {
            setTheme(R.style.Theme_Facitilov2_Bold) // Usa el estilo que creamos
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_home -> replaceFragment(HomeFragment())
                R.id.nav_contacts -> replaceFragment(ContactsFragment())
                R.id.nav_go_home -> replaceFragment(GoHomeFragment())
                R.id.nav_medicines -> replaceFragment(MedicinesFragment())
                R.id.nav_settings -> replaceFragment(SettingsFragment())
                else -> false
            }
            true
        }
    }

    // 2. Aplicar Escala de Texto (Tamaño)
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val scale = prefs.getFloat("text_scale", 1.0f) // Leemos el valor del Slider

        val config = Configuration(newBase.resources.configuration)
        config.fontScale = scale

        val context = newBase.createConfigurationContext(config)
        super.attachBaseContext(context)
    }

    private fun replaceFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }
}