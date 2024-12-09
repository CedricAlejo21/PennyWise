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
import android.widget.TextView
import com.google.firebase.database.*
import com.github.mikephil.charting.charts.BarChart
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.utils.ColorTemplate

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

            findViewById<Button>(R.id.btn_delete_account).setOnClickListener {
                deleteAccount()
            }

            findViewById<Button>(R.id.btn_logout).setOnClickListener {
                logout()
            }

            databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val userData = snapshot.value as? Map<String, Any>
                    val profile = userData?.get("profile") as? Map<String, Any>
                    val name = profile?.get("name") as? String

                    if (name != null) {
                        Log.d("DashboardActivity", "Current User's Name: $name")
                        val welcomeTextView: TextView = findViewById(R.id.welcome_text)
                        welcomeTextView.text = "Welcome to PennyWise, $name!"
                    } else {
                        Log.e("DashboardActivity", "Name not found in profile")
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Toast.makeText(this@DashboardActivity, "Failed to load data: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            })

            fetchTransactions()
            calculateTotalSpendingByCategory()
            Log.d("DashboardActivity", "User logged in!")
        } else {
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
            finish()
        }

        val barChart = findViewById<BarChart>(R.id.category_summary2)

        val entries = listOf(
            BarEntry(1f, 10f),
            BarEntry(2f, 20f),
            BarEntry(3f, 30f),
            BarEntry(4f, 40f)
        )

        val dataSet = BarDataSet(entries, "Category Totals")
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toList()

        val barData = BarData(dataSet)
        barChart.data = barData
        barChart.invalidate() // Refresh the chart
    }

    private fun fetchTransactions() {
        databaseRef.child("transactions").addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val transactionList = ArrayList<Transaction>()
                var totalExpenses = 0.0
                val categoryTotals = mutableMapOf<String, Double>()

                for (transactionSnapshot in snapshot.children) {
                    val transaction = transactionSnapshot.getValue(Transaction::class.java)
                    if (transaction != null) {
                        transactionList.add(transaction)

                        // Accumulate total expenses
                        val price = transaction.price ?: 0.0
                        totalExpenses += price

                        // Accumulate totals per category
                        val category = transaction.category ?: "Uncategorized"
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + price
                    }
                }

                // Check if transactionsRecyclerView is initialized before using it
                if (this@DashboardActivity::transactionsRecyclerView.isInitialized) {
                    transactionsRecyclerView.adapter = TransactionsAdapter(transactionList)
                } else {
                    Log.e("DashboardActivity", "transactionsRecyclerView not initialized")
                }

                // Update RecyclerView
                transactionsRecyclerView.adapter = TransactionsAdapter(transactionList)

                // Update totals and category display
                updateBalanceAndTransactions(totalExpenses, transactionList.size)
                displayCategoryTotals(categoryTotals)
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

    private fun calculateTotalSpendingByCategory() {
        val uid = firebaseAuth.currentUser?.uid ?: return
        val databaseRef = FirebaseDatabase.getInstance("https://pennywise-f2ed7-default-rtdb.asia-southeast1.firebasedatabase.app")
            .getReference("users/$uid/transactions")

        databaseRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryTotals = mutableMapOf<String, Double>()

                for (transactionSnapshot in snapshot.children) {
                    val category = transactionSnapshot.child("category").getValue(String::class.java)
                    val price = transactionSnapshot.child("price").getValue(Double::class.java)

                    if (category != null && price != null) {
                        categoryTotals[category] = categoryTotals.getOrDefault(category, 0.0) + price
                    } else {
                        Log.w("DashboardActivity", "Invalid transaction data: $transactionSnapshot")
                    }
                }

                displayCategoryTotals(categoryTotals)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("DashboardActivity", "Failed to fetch transactions: ${error.message}")
            }
        })
    }

    private fun displayCategoryTotals(categoryTotals: Map<String, Double>) {
        val categorySummaryTextView: TextView = findViewById(R.id.category_summary)
        val stringBuilder = StringBuilder()

        if (categoryTotals.isEmpty()) {
            stringBuilder.append("No transactions to display.")
        } else {
            categoryTotals.forEach { (category, total) ->
                stringBuilder.append("$category: ₱${String.format("%.2f", total)}\n")
            }
        }

        categorySummaryTextView.text = stringBuilder.toString()
    }

    private fun updateBalanceAndTransactions(totalExpenses: Double, transactionCount: Int) {
        val balanceTextView: TextView = findViewById(R.id.balance)
        val totalExpensesTextView: TextView = findViewById(R.id.total_expenses)
        val totalTransactionsTextView: TextView = findViewById(R.id.total_transactions)

        // Update UI elements
        balanceTextView.text = "₱${String.format("%.2f", 13227.85 - totalExpenses)}"
        totalExpensesTextView.text = "Total Expenses: ₱${String.format("%.2f", totalExpenses)}"
        totalTransactionsTextView.text = "Total Transactions: $transactionCount"
    }

    private fun deleteAccount() {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid

            // Delete user data from the database
            databaseRef.removeValue().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Delete user from Firebase Authentication
                    currentUser.delete().addOnCompleteListener { deleteTask ->
                        if (deleteTask.isSuccessful) {
                            Toast.makeText(this, "Account deleted successfully!", Toast.LENGTH_SHORT).show()
                            redirectToLogin()
                        } else {
                            Toast.makeText(this, "Failed to delete account: ${deleteTask.exception?.message}", Toast.LENGTH_SHORT).show()
                        }
                    }
                } else {
                    Toast.makeText(this, "Failed to delete user data: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun logout() {
        firebaseAuth.signOut()
        Toast.makeText(this, "Logged out successfully!", Toast.LENGTH_SHORT).show()
        redirectToLogin()
    }

    private fun redirectToLogin() {
        val intent = Intent(this, LoginActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finish()
    }
}
