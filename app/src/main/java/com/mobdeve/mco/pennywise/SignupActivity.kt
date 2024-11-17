package com.mobdeve.mco.pennywise

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth

class SignupActivity : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firebaseAuth = FirebaseAuth.getInstance()

        // Handle sign-up button click
        findViewById<Button>(R.id.btn_signup).setOnClickListener {
            val email = findViewById<EditText>(R.id.signup_email).text.toString()
            val password = findViewById<EditText>(R.id.signup_password).text.toString()
            val confirmPassword = findViewById<EditText>(R.id.signup_confirm_password).text.toString()

            if (password == confirmPassword) {
                signUp(email, password)
            } else {
                Toast.makeText(this, "Passwords do not match!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signUp(email: String, password: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Toast.makeText(this, "Sign Up Successful!", Toast.LENGTH_SHORT).show()
                    // Redirect back to LoginActivity
                    finish()
                } else {
                    Toast.makeText(this, "Error: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }
}
