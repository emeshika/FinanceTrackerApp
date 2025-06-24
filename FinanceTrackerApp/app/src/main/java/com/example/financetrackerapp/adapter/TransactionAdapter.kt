package com.example.financetrackerapp.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.recyclerview.widget.RecyclerView
import com.example.financetrackerapp.R
import com.example.financetrackerapp.model.Transaction

class TransactionAdapter(
    private val transactionList: List<Transaction>,
    private val listener: OnTransactionActionListener
) : RecyclerView.Adapter<TransactionAdapter.TransactionViewHolder>() {

    interface OnTransactionActionListener {
        fun onEdit(position: Int)
        fun onDelete(position: Int)
    }

    inner class TransactionViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleText: TextView = itemView.findViewById(R.id.textTitle)
        val dateText: TextView = itemView.findViewById(R.id.textDate)
        val amountText: TextView = itemView.findViewById(R.id.textAmount)
        val typeText: TextView = itemView.findViewById(R.id.textType)
        val categoryText: TextView = itemView.findViewById(R.id.textCategory)
        val editButton: ImageButton = itemView.findViewById(R.id.buttonEdit)
        val deleteButton: ImageButton = itemView.findViewById(R.id.buttonDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactionList[position]
        holder.titleText.text = transaction.title
        holder.dateText.text = transaction.date
        holder.amountText.text = "Rs. ${transaction.amount}"
        holder.typeText.text = "Type: ${transaction.type}"
        holder.categoryText.text = "Category: ${transaction.category}"

        holder.editButton.setOnClickListener {
            listener.onEdit(position)
        }

        holder.deleteButton.setOnClickListener {
            listener.onDelete(position)
        }
    }

    override fun getItemCount(): Int = transactionList.size
}
