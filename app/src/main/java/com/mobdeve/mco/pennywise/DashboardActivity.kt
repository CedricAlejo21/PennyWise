package com.mobdeve.mco.pennywise

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import com.mobdeve.mco.pennywise.Transaction
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.ValueEventListener
import android.util.Log
import android.view.View
import android.widget.Button
import com.google.firebase.database.*

class DashboardActivity : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth
    private lateinit var databaseRef: DatabaseReference
    private lateinit var transactionsRecyclerView: RecyclerView
    private lateinit var transactionsAdapter: TransactionsAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        val toggleButton: Button = findViewById(R.id.btn_toggle_transactions)
        transactionsRecyclerView = findViewById(R.id.transactions_recycler_view)
        var isTransactionsVisible = false

        firebaseAuth = FirebaseAuth.getInstance()
        val currentUser = firebaseAuth.currentUser

        transactionsRecyclerView.layoutManager = LinearLayoutManager(this)

        val addTransactionButton = findViewById<Button>(R.id.btn_add_transaction)
        addTransactionButton.setOnClickListener {
            Log.d("DashboardActivity", "Add Transaction button clicked!")
            val intent = Intent(this, AddTransactionActivity::class.java)
            startActivity(intent)
        }

        toggleButton.setOnClickListener {
            isTransactionsVisible = !isTransactionsVisible
            if (isTransactionsVisible) {
                transactionsRecyclerView.visibility = View.VISIBLE
                toggleButton.text = "Hide Transactions"
            } else {
                transactionsRecyclerView.visibility = View.GONE
                toggleButton.text = "Show Transactions"
            }
        }

        if (currentUser != null) {
            val uid = currentUser.uid
            databaseRef = FirebaseDatabase.getInstance("https://pennywise-f2ed7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users").child(uid)

            fetchTransactions()
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun fetchTransactions() {
        databaseRef.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionList = ArrayList<Transaction>()
                for (transactionSnapshot in snapshot.children) {
                    val transaction = transactionSnapshot.getValue(Transaction::class.java)
                    if (transaction != null) {
                        transactionList.add(transaction)
                    }
                }
                // Check if transactionsRecyclerView is initialized before using it
                if (this@DashboardActivity::transactionsRecyclerView.isInitialized) {
                    transactionsRecyclerView.adapter = TransactionsAdapter(transactionList)
                } else {
                    Log.e("DashboardActivity", "transactionsRecyclerView not initialized")
                }
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardActivity", "Database error: ${error.message}")
            }
        })
    }

    private fun loadUserData() {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val database = FirebaseDatabase.getInstance()
        val userRef = database.getReference("users").child(userId)

        userRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userData = snapshot.value as? Map<String, Any>
                val email = userData?.get("email") as? String
                val profile = userData?.get("profile") as? Map<String, Any>
                val name = profile?.get("name") as? String
                val age = profile?.get("age") as? Long

                Toast.makeText(this@DashboardActivity, "Welcome, $name!", Toast.LENGTH_SHORT).show()
            }

            override fun onCancelled(error: DatabaseError) {
                Toast.makeText(this@DashboardActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
