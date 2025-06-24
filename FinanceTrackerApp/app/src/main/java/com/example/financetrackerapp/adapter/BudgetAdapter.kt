package com.example.financetrackerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapp.R
import com.example.financetrackerapp.model.BudgetEntry

class BudgetAdapter(
    private val entries: List<BudgetEntry>,
    private val onDeleteClick: (Int) -> Unit
) : RecyclerView.Adapter<BudgetAdapter.BudgetViewHolder>() {

    inner class BudgetViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textDate: TextView = view.findViewById(R.id.textDate)
        val textReason: TextView = view.findViewById(R.id.textReason)
        val textType: TextView = view.findViewById(R.id.textType)
        val textAmount: TextView = view.findViewById(R.id.textAmount)

        init {
            view.setOnLongClickListener {
                onDeleteClick(adapterPosition)
                true
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BudgetViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.budget_item_layout, parent, false)
        return BudgetViewHolder(view)
    }

    override fun onBindViewHolder(holder: BudgetViewHolder, position: Int) {
        val entry = entries[position]
        holder.textDate.text = entry.date
        holder.textReason.text = entry.reason
        holder.textType.text = entry.type
        holder.textAmount.text = "Rs. %.2f".format(entry.amount)
    }

    override fun getItemCount(): Int = entries.size
}
