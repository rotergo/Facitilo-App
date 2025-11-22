package com.example.facitilov2

import android.content.Context
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNav = findViewById<BottomNavigationView>(R.id.bottom_navigation)

        // Cargar el fragmento de Inicio por defecto al abrir la app
        if (savedInstanceState == null) {
            replaceFragment(HomeFragment())
        }

        // Configurar los clics en la barra inferior
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

    private fun replaceFragment(fragment: Fragment) {
        val fragmentManager = supportFragmentManager
        val fragmentTransaction = fragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container, fragment)
        fragmentTransaction.commit()
    }

    // AGREGA ESTO PARA QUE FUNCIONE EL CAMBIO DE LETRA
    override fun attachBaseContext(newBase: Context) {
        val prefs = newBase.getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val isLargeText = prefs.getBoolean("large_text", false)

        val newConfig = android.content.res.Configuration(newBase.resources.configuration)
        if (isLargeText) {
            newConfig.fontScale = 1.30f // Aumenta el tamaño un 30%
        } else {
            newConfig.fontScale = 1.0f // Tamaño normal
        }

        applyOverrideConfiguration(newConfig)
        super.attachBaseContext(newBase)
    }
}