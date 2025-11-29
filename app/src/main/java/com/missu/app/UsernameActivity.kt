package com.missu.app

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.missu.app.R
import com.missu.app.models.User

class UsernameActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private val TAG = "UsernameActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_username)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sharedPreferences = getSharedPreferences("MissuPrefs", MODE_PRIVATE)

        val usernameEditText = findViewById<EditText>(R.id.usernameEditText)
        val continueButton = findViewById<Button>(R.id.continueButton)

        // Check if user already set up
        if (sharedPreferences.getString("username", null) != null && auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        // Update button text immediately
        continueButton.text = "Continue"
        continueButton.isEnabled = true

        continueButton.setOnClickListener {
            val username = usernameEditText.text.toString().trim()
            if (username.length < 3) {
                Toast.makeText(this, "Username must be 3+ characters", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val user = auth.currentUser
            if (user != null) {
                // Disable button during save
                continueButton.isEnabled = false
                continueButton.text = "Saving..."
                
                // SAVE TO FIRESTORE
                val userData = User(
                    uid = user.uid,
                    userId = user.uid, 
                    email = "",
                    username = username,
                    name = username,
                    fcmToken = ""
                )
                
                db.collection("users").document(user.uid).set(userData)
                    .addOnSuccessListener {
                        sharedPreferences.edit().putString("username", username).apply()
                        Toast.makeText(this, "Welcome to Missu!", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, MainActivity::class.java))
                        finish()
                    }
                    .addOnFailureListener { e ->
                        continueButton.isEnabled = true
                        continueButton.text = "Continue"
                        Toast.makeText(this, "Failed to save: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this, "Not authenticated - please wait", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
