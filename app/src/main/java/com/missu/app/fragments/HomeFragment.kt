package com.missu.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.missu.app.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {
    
    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupHeartButton()
    }
    
    private fun setupHeartButton() {
        binding.heartButton.setOnClickListener {
            sendMissToFriends()
        }
    }
    
    private fun sendMissToFriends() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Get current user's friends
            db.collection("users").document(user.uid)
                .collection("friends")
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(requireContext(), "No friends to send miss to", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }
                    
                    for (document in documents) {
                        val friendId = document.id
                        sendMissNotification(friendId, document.getString("username") ?: "User")
                    }
                    Toast.makeText(requireContext(), "Miss sent to all friends!", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener {
                    Toast.makeText(requireContext(), "Failed to send miss", Toast.LENGTH_SHORT).show()
                }
        }
    }
    
    private fun sendMissNotification(friendId: String, userName: String) {
        // Get friend's FCM token and send notification
        db.collection("users").document(friendId).get()
            .addOnSuccessListener { document ->
                val fcmToken = document.getString("fcmToken")
                val currentUserName = auth.currentUser?.displayName ?: "Someone"
                
                if (!fcmToken.isNullOrEmpty()) {
                    // Here you would send the FCM message
                    // For now, we'll just update Firestore
                    val missData = hashMapOf(
                        "fromUserId" to auth.currentUser?.uid,
                        "fromUserName" to currentUserName,
                        "timestamp" to System.currentTimeMillis(),
                        "read" to false
                    )
                    
                    db.collection("users").document(friendId)
                        .collection("misses")
                        .add(missData)
                }
            }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}