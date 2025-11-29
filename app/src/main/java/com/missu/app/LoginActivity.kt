package com.missu.app

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ActionCodeSettings
import com.google.firebase.firestore.FirebaseFirestore
import com.missu.app.models.User

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var emailEditText: EditText
    private lateinit var sendLinkButton: Button
    private val TAG = "LoginActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        emailEditText = findViewById(R.id.emailEditText)
        sendLinkButton = findViewById(R.id.sendLinkButton)

        sendLinkButton.setOnClickListener {
            val email = emailEditText.text.toString().trim()
            if (email.isEmpty()) {
                Toast.makeText(this, "Please enter your email", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            sendSignInLink(email)
        }

        // Check if user is already signed in
        if (auth.currentUser != null) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        // Check if the app was opened from an email link
        checkEmailLink(intent)
    }

    override fun onNewIntent(intent: Intent?) {
        super.onNewIntent(intent)
        checkEmailLink(intent)
    }

    private fun sendSignInLink(email: String) {
        val actionCodeSettings = ActionCodeSettings.newBuilder()
            .setUrl("https://missu-app-3245a.firebaseapp.com")
            .setHandleCodeInApp(true)
            .setAndroidPackageName("com.missu.app", false, null)
            .build()

        auth.sendSignInLinkToEmail(email, actionCodeSettings)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "Sign-in link sent to $email")
                    Toast.makeText(this, "Sign-in link sent to your email!", Toast.LENGTH_LONG).show()
                } else {
                    Log.e(TAG, "Failed to send sign-in link", task.exception)
                    Toast.makeText(this, "Failed to send sign-in link", Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun checkEmailLink(intent: Intent?) {
        val emailLink = intent?.data.toString()

        if (emailLink.isNotEmpty()) {
            val email = emailEditText.text.toString().trim()
            
            if (email.isEmpty()) {
                // If we don't have the email, ask user to enter it
                Toast.makeText(this, "Please enter your email first", Toast.LENGTH_SHORT).show()
                return
            }

            auth.signInWithEmailLink(email, emailLink)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        Log.d(TAG, "Successfully signed in with email link")
                        val user = auth.currentUser
                        user?.let {
                            // Check if user exists in Firestore
                            db.collection("users").document(it.uid).get()
                                .addOnSuccessListener { document ->
                                    if (document.exists()) {
                                        startActivity(Intent(this, MainActivity::class.java))
                                        finish()
                                    } else {
                                        // New user, create in Firestore
                                        val newUser = User(
                                            uid = it.uid,
                                            email = it.email ?: "",
                                            username = it.email?.substringBefore("@") ?: "User",
                                            fcmToken = ""
                                        )
                                        db.collection("users").document(it.uid).set(newUser)
                                            .addOnSuccessListener {
                                                startActivity(Intent(this, MainActivity::class.java))
                                                finish()
                                            }
                                            .addOnFailureListener { e ->
                                                Log.e(TAG, "Failed to create user", e)
                                                Toast.makeText(this, "Failed to create user profile", Toast.LENGTH_SHORT).show()
                                            }
                                    }
                                }
                        }
                    } else {
                        Log.e(TAG, "Failed to sign in with email link", task.exception)
                        Toast.makeText(this, "Authentication failed", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }
}
