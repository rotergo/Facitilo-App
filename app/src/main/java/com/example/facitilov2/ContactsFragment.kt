package com.example.facitilov2

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class ContactsFragment : Fragment() {

    private lateinit var etName: TextInputEditText
    private lateinit var etPhone: TextInputEditText
    private lateinit var btnSaveContact: MaterialButton
    private lateinit var rvContacts: RecyclerView
    private lateinit var contactsAdapter: ContactsAdapter
    private val contacts = mutableListOf<Contact>()
    private var contactIdCounter = 0 // Contador simple para IDs únicos

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_contacts, container, false)

        // 1. Vincular las vistas
        etName = view.findViewById(R.id.etName)
        etPhone = view.findViewById(R.id.etPhone)
        btnSaveContact = view.findViewById(R.id.btnSaveContact)
        rvContacts = view.findViewById(R.id.rvContacts)

        // 2. Cargar datos guardados
        loadSavedData()

        // 3. Configurar la lista (RecyclerView)
        // Usamos tu ContactsAdapter que ya tenías en el proyecto
        contactsAdapter = ContactsAdapter(contacts) { contact ->
            deleteContact(contact)
        }
        rvContacts.layoutManager = LinearLayoutManager(requireContext())
        rvContacts.adapter = contactsAdapter

        // 4. Configurar el botón de guardar
        btnSaveContact.setOnClickListener {
            saveNewContact()
        }

        return view
    }

    private fun saveNewContact() {
        val name = etName.text.toString()
        val phone = etPhone.text.toString()

        if (name.isBlank() || phone.isBlank()) {
            Toast.makeText(context, "Completa ambos campos", Toast.LENGTH_SHORT).show()
            return
        }

        // Crear y añadir el contacto
        val newContact = Contact(contactIdCounter++, name, phone)
        contactsAdapter.addContact(newContact)

        // Guardar en memoria del teléfono
        saveContactsToPrefs()

        // Limpiar campos
        etName.text?.clear()
        etPhone.text?.clear()

        Toast.makeText(context, "Contacto guardado", Toast.LENGTH_SHORT).show()
    }

    private fun deleteContact(contact: Contact) {
        contactsAdapter.removeContact(contact)
        saveContactsToPrefs()
        Toast.makeText(context, "Contacto eliminado", Toast.LENGTH_SHORT).show()
    }

    private fun saveContactsToPrefs() {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val editor = prefs.edit()

        // Guardar la cantidad y cada contacto individualmente
        editor.putInt("contact_count", contacts.size)
        contacts.forEachIndexed { index, contact ->
            editor.putString("contact_${index}_name", contact.name)
            editor.putString("contact_${index}_phone", contact.phone)
        }
        editor.apply()
    }

    private fun loadSavedData() {
        val prefs = requireContext().getSharedPreferences("FacilitoPrefs", Context.MODE_PRIVATE)
        val count = prefs.getInt("contact_count", 0)

        contacts.clear()
        contactIdCounter = 0

        for (i in 0 until count) {
            val name = prefs.getString("contact_${i}_name", "") ?: ""
            val phone = prefs.getString("contact_${i}_phone", "") ?: ""

            if (name.isNotBlank() && phone.isNotBlank()) {
                contacts.add(Contact(contactIdCounter++, name, phone))
            }
        }
    }
}