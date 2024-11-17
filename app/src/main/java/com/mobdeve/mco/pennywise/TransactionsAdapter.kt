package com.mobdeve.mco.pennywise

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class TransactionsAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionsAdapter.TransactionViewHolder>() {

    class TransactionViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val description: TextView = view.findViewById(R.id.transaction_description)
        val category: TextView = view.findViewById(R.id.transaction_category)
        val price: TextView = view.findViewById(R.id.transaction_price)
        val location: TextView = view.findViewById(R.id.transaction_location)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TransactionViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.transaction_item, parent, false)
        return TransactionViewHolder(view)
    }

    override fun onBindViewHolder(holder: TransactionViewHolder, position: Int) {
        val transaction = transactions[position]
        holder.description.text = transaction.description
        holder.category.text = transaction.category
        holder.price.text = "₱${transaction.price}"
        holder.location.text = transaction.location
    }

    override fun getItemCount() = transactions.size
}
