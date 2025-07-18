package com.example.projectskripsi.Pelanggan

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.projectskripsi.R
import com.example.projectskripsi.model.RiwayatResponse

class rv_riwayat_pembayaran(private val historyList: List<RiwayatResponse>) :
    RecyclerView.Adapter<rv_riwayat_pembayaran.HistoryViewHolder>() {

    class HistoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val ivPaymentLogo: ImageView = itemView.findViewById(R.id.ivPaymentLogo)
        val tvPaymentMethodName: TextView = itemView.findViewById(R.id.tvPaymentMethodName)
        val tvPaymentType: TextView = itemView.findViewById(R.id.tvPaymentType)
        val tvAmount: TextView = itemView.findViewById(R.id.tvAmount)
        val tvStatus: TextView = itemView.findViewById(R.id.tvStatus)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.activity_rv_riwayat_pembayaran, parent, false)
        return HistoryViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val currentItem = historyList[position]

        holder.tvDate.text = currentItem.tanggal
        holder.tvPaymentMethodName.text = currentItem.metode
        holder.tvPaymentType.text = "Pembayaran"
        holder.tvAmount.text = "Rp ${currentItem.nominal}"
        holder.tvStatus.text = currentItem.status

        val iconResId = when (currentItem.metode.lowercase()) {
            "gopay" -> R.drawable.gopay
            "bca_va" -> R.drawable.logo_bca
            "bri_va" -> R.drawable.logo_bri
            "dana" -> R.drawable.logo_dana
            "echannel" -> R.drawable.logo_mandiri
            else -> R.drawable.parking_logo
        }
        holder.ivPaymentLogo.setImageResource(iconResId)

        val statusColorResId = when (currentItem.status.lowercase()) {
            "selesai" -> R.color.status_completed
            "menunggu" -> R.color.status_pending
            "gagal" -> R.color.status_failed
            else -> android.R.color.darker_gray
        }
        holder.tvStatus.background.setTint(
            ContextCompat.getColor(holder.itemView.context, statusColorResId)
        )
    }


    override fun getItemCount(): Int = historyList.size
}
