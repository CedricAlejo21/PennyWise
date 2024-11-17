package com.mobdeve.mco.pennywise

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mobdeve.mco.pennywise.Transaction

class DashboardActivity : ComponentActivity() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        recyclerView = findViewById(R.id.transactions_recycler_view)
        recyclerView.layoutManager = LinearLayoutManager(this)

        fetchTransactions()
    }

    private fun fetchTransactions() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        dbRef.get().addOnSuccessListener { dataSnapshot ->
            val transactionsList = mutableListOf<Transaction>()
            for (snapshot in dataSnapshot.children) {
                val transaction = snapshot.getValue(Transaction::class.java)
                transaction?.let { transactionsList.add(it) }
            }

            adapter = TransactionsAdapter(transactionsList)
            recyclerView.adapter = adapter
        }.addOnFailureListener { error ->
            Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
        }
    }
}
