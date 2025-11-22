package com.example.facitilov2

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicinesAdapter(
    private val medicines: MutableList<Medicine>,
    private val onDeleteClick: (Medicine) -> Unit
) : RecyclerView.Adapter<MedicinesAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvMedicineName)
        val tvDose: TextView = view.findViewById(R.id.tvMedicineDose)
        val tvTime: TextView = view.findViewById(R.id.tvMedicineTime)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteMedicine)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_medicine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val medicine = medicines[position]
        holder.tvName.text = medicine.name
        holder.tvDose.text = medicine.dose
        holder.tvTime.text = medicine.time

        holder.btnDelete.setOnClickListener {
            onDeleteClick(medicine)
        }
    }

    override fun getItemCount() = medicines.size

    fun addMedicine(medicine: Medicine) {
        medicines.add(medicine)
        notifyItemInserted(medicines.size - 1)
    }

    fun removeMedicine(medicine: Medicine) {
        val position = medicines.indexOf(medicine)
        if (position != -1) {
            medicines.removeAt(position)
            notifyItemRemoved(position)
        }
    }
}