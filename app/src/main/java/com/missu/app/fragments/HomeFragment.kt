package com.missu.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.missu.app.R
import com.missu.app.models.User

class HomeFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var heartButton: Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_home, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        heartButton = view.findViewById(R.id.heartButton)

        heartButton.setOnClickListener {
            sendMissToAllFriends()
        }

        return view
    }

    private fun sendMissToAllFriends() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Get current user's username
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val currentUsername = document.getString("username") ?: "Someone"
                        
                        // Get friends list
                        val friends = document.get("friends") as? List<String> ?: emptyList()
                        
                        if (friends.isEmpty()) {
                            Toast.makeText(requireContext(), "Add some friends first!", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        // Send miss to each friend
                        for (friendId in friends) {
                            sendMissNotification(friendId, currentUsername)
                        }
                        
                        Toast.makeText(requireContext(), "Miss sent to ${friends.size} friends!", Toast.LENGTH_SHORT).show()
                    }
                }
        }
    }

    private fun sendMissNotification(friendId: String, userName: String) {
        // Here we would send FCM notification to the friend
        // For now, we'll just log it
        println("Sending miss notification to $friendId from $userName")
        
        // TODO: Implement FCM notification sending
        // This would send a push notification to the friend's device
    }
}
