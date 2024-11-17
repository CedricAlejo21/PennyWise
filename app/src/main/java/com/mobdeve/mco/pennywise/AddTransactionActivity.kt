package com.mobdeve.mco.pennywise

import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase
import java.text.SimpleDateFormat
import java.util.*

class AddTransactionActivity : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        firebaseAuth = FirebaseAuth.getInstance()
        val addButton = findViewById<Button>(R.id.btn_done)

        addButton.setOnClickListener {
            val description = findViewById<EditText>(R.id.transaction_description).text.toString()
            val category = findViewById<EditText>(R.id.transaction_category).text.toString()
            val price = findViewById<EditText>(R.id.transaction_price).text.toString().toDoubleOrNull() ?: 0.0
            val location = findViewById<EditText>(R.id.transaction_location).text.toString()

            if (description.isNotEmpty() && category.isNotEmpty() && price > 0 && location.isNotEmpty()) {
                saveTransaction(description, category, price, location)
            } else {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun saveTransaction(description: String, category: String, price: Double, location: String) {
        val currentUser = firebaseAuth.currentUser
        if (currentUser != null) {
            val uid = currentUser.uid
            val transactionId = FirebaseDatabase.getInstance().getReference("users/$uid/transactions").push().key

            val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val currentDate = sdf.format(Date())

            val transaction = mapOf(
                "description" to description,
                "category" to category,
                "price" to price,
                "location" to location,
                "date" to currentDate
            )

            Log.d("AddTransactionActivity", "Transaction Data Prepared: $transaction")

            val databaseRef = FirebaseDatabase.getInstance("https://pennywise-f2ed7-default-rtdb.asia-southeast1.firebasedatabase.app")
                .getReference("users/$uid/transactions/$transactionId")

            databaseRef.setValue(transaction)
                .addOnSuccessListener {
                    Log.d("AddTransactionActivity", "Transaction saved successfully: $transaction")
                    Toast.makeText(this, "Transaction added successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to the previous screen
                }
                .addOnFailureListener { exception ->
                    Log.e("AddTransactionActivity", "Failed to save transaction: ${exception.message}")
                    Toast.makeText(this, "Failed to add transaction.", Toast.LENGTH_SHORT).show()
                }
        }else {
            Log.e("AddTransactionActivity", "User is not logged in!")
            Toast.makeText(this, "User not logged in!", Toast.LENGTH_SHORT).show()
        }
    }
}
