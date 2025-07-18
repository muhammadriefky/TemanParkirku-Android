package com.example.projectskripsi.Penjaga

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.projectskripsi.R
import com.example.projectskripsi.model.Parkir

class LihatParkirAdapter(
    private val context: Context,
    private val dataList: List<Parkir>
) : RecyclerView.Adapter<LihatParkirAdapter.ParkirViewHolder>() {

    inner class ParkirViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvNoPlat: TextView = itemView.findViewById(R.id.tvNoPlat)
        val tvJamMasuk: TextView = itemView.findViewById(R.id.tvJamMasuk)
        val tvJenisKendaraan: TextView = itemView.findViewById(R.id.tvJenisKendaraan)
        val tvTarif: TextView = itemView.findViewById(R.id.tvTarif)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ParkirViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_lihat_parkir_adapter, parent, false)
        return ParkirViewHolder(view)
    }

    override fun onBindViewHolder(holder: ParkirViewHolder, position: Int) {
        val item = dataList[position]
        holder.tvNoPlat.text = item.no_plat
        holder.tvJamMasuk.text = item.jam_masuk
        holder.tvJenisKendaraan.text = item.jenis_kendaraan
        holder.tvTarif.text = "Rp ${item.tarif}"

        holder.itemView.setOnClickListener {
            val intent = Intent(context, GenerateQrcode::class.java).apply {
                putExtra("NO_PLAT", item.no_plat)
                putExtra("WAKTU_MASUK", item.jam_masuk)
                putExtra("TARIF", item.tarif.toString())
                putExtra("ORDER_ID", item.order_id)
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount(): Int = dataList.size
}
