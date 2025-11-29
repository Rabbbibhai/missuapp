package com.missu.app.fragments

import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.missu.app.R
import com.missu.app.UsernameActivity

class ProfileFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var userUsername: TextView
    private lateinit var logoutButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_profile, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()
        sharedPreferences = requireContext().getSharedPreferences("MissuPrefs", android.content.Context.MODE_PRIVATE)

        userUsername = view.findViewById(R.id.userUsername)
        logoutButton = view.findViewById(R.id.logoutButton)

        loadUserData()

        logoutButton.setOnClickListener {
            logout()
        }

        return view
    }

    private fun loadUserData() {
        val username = sharedPreferences.getString("username", "No username set")
        userUsername.text = "Username: $username"
    }

    private fun logout() {
        // Sign out from Firebase (deletes anonymous account)
        auth.signOut()
        
        // Clear local data
        sharedPreferences.edit().remove("username").apply()
        
        // Go back to username setup
        val intent = Intent(requireContext(), UsernameActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }
}
