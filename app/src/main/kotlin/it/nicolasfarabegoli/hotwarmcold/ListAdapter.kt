package it.nicolasfarabegoli.hotwarmcold

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import it.nicolasfarabegoli.hotwarmcold.components.smartphone.NeighbourDistance

internal class ListAdapter : RecyclerView.Adapter<ListAdapter.MyViewHolder>() {
    private var distanceItems: List<NeighbourDistance> = emptyList()

    internal class MyViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val itemTextView: TextView = view.findViewById(R.id.distanceCell)
    }

    @SuppressLint("NotifyDataSetChanged")
    fun onUpdateItems(items: List<NeighbourDistance>) {
        distanceItems = items.sortedBy { it.deviceId }
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyViewHolder {
        val itemView =
            LayoutInflater.from(parent.context).inflate(R.layout.rssi_item, parent, false)
        return MyViewHolder(itemView)
    }

    override fun getItemCount(): Int = distanceItems.size

    override fun onBindViewHolder(holder: MyViewHolder, position: Int) {
        val item = distanceItems[position]
        holder.itemTextView.text = "Device: ${item.deviceId} -- Distance: ${item.distance}m"
    }
}
