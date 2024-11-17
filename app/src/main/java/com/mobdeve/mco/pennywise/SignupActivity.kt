package com.mobdeve.mco.pennywise

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.activity.ComponentActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.FirebaseDatabase

class SignupActivity : ComponentActivity() {
    private lateinit var firebaseAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        firebaseAuth = FirebaseAuth.getInstance()

        val signupButton = findViewById<Button>(R.id.btn_signup)
        val emailEditText = findViewById<EditText>(R.id.signup_email)
        val passwordEditText = findViewById<EditText>(R.id.signup_password)
        val usernameEditText = findViewById<EditText>(R.id.signup_username)

        signupButton.setOnClickListener {
            val email = emailEditText.text.toString()
            val password = passwordEditText.text.toString()
            val username = usernameEditText.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty() && username.isNotEmpty()) {
                signup(email, password, username)
            } else {
                Toast.makeText(this, "Please fill in all fields!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun signup(email: String, password: String, username: String) {
        firebaseAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val uid = firebaseAuth.currentUser?.uid ?: return@addOnCompleteListener
                    saveUserData(uid, email, username)
                } else {
                    val errorMessage = task.exception?.localizedMessage ?: "Sign-up failed."
                    Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun saveUserData(uid: String, email: String, username: String, retries: Int = 3) {
        val database = FirebaseDatabase.getInstance("https://pennywise-f2ed7-default-rtdb.asia-southeast1.firebasedatabase.app")
        val usersRef = database.getReference("users").child(uid)

        val userInfo = mapOf(
            "email" to email,
            "profile" to mapOf(
                "name" to username,
                "age" to 0
            ),
            "transactions" to mapOf<String, Any>() // Empty transactions for now
        )

        usersRef.setValue(userInfo)
            .addOnSuccessListener {
                Toast.makeText(this, "Sign-Up Successful!", Toast.LENGTH_SHORT).show()
                redirectToLogin()
            }
            .addOnFailureListener { exception ->
                if (retries > 0) {
                    Log.w("SignupActivity", "Retrying to save data... Attempts left: $retries")
                    saveUserData(uid, email, username, retries - 1)
                } else {
                    Log.e("SignupActivity", "Failed to save data: ${exception.message}")
                    Toast.makeText(this, "Failed to save user data: ${exception.message}", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun redirectToLogin() {
        startActivity(Intent(this, LoginActivity::class.java))
        finish()
    }
}
