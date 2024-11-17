package com.mobdeve.mco.pennywise

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class AddTransactionActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_transaction)

        findViewById<Button>(R.id.btn_done).setOnClickListener {
            val description = findViewById<EditText>(R.id.transaction_description).text.toString()
            val category = findViewById<EditText>(R.id.transaction_category).text.toString()
            val price = findViewById<EditText>(R.id.transaction_price).text.toString().toDoubleOrNull()
            val location = findViewById<EditText>(R.id.transaction_location).text.toString()

            if (price == null) {
                Toast.makeText(this, "Enter a valid price!", Toast.LENGTH_SHORT).show()
            } else {
                addTransaction(description, category, price, location)
            }
        }
    }

    private fun addTransaction(description: String, category: String, price: Double, location: String) {
        val userId = FirebaseAuth.getInstance().currentUser?.uid ?: return
        val dbRef = FirebaseDatabase.getInstance().getReference("users").child(userId).child("transactions")

        val transactionId = dbRef.push().key
        val transaction = mapOf(
            "id" to transactionId,
            "description" to description,
            "category" to category,
            "price" to price,
            "location" to location
        )

        transactionId?.let {
            dbRef.child(it).setValue(transaction)
                .addOnSuccessListener {
                    Toast.makeText(this, "Transaction Added Successfully!", Toast.LENGTH_SHORT).show()
                    finish() // Return to previous screen
                }
                .addOnFailureListener { error ->
                    Toast.makeText(this, "Error: ${error.message}", Toast.LENGTH_SHORT).show()
                }
        }
    }
}
