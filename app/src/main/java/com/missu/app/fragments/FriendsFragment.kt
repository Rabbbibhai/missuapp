package com.missu.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.missu.app.adapters.FriendsAdapter
import com.missu.app.databinding.FragmentFriendsBinding
import com.missu.app.models.User

class FriendsFragment : Fragment() {
    
    private var _binding: FragmentFriendsBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: FriendsAdapter
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentFriendsBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        setupRecyclerView()
        setupAddFriendButton()
        loadFriends()
    }
    
    private fun setupRecyclerView() {
        adapter = FriendsAdapter(requireContext(), mutableListOf()) { friendId ->
            removeFriend(friendId)
        }
        binding.friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        binding.friendsRecyclerView.adapter = adapter
    }
    
    private fun setupAddFriendButton() {
        binding.addFriendButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString().trim()
            if (username.isNotEmpty()) {
                addFriend(username)
            } else {
                Toast.makeText(requireContext(), "Please enter username", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addFriend(username: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        // Search for user by username
        db.collection("users")
            .whereEqualTo("username", username)
            .get()
            .addOnSuccessListener { documents ->
                if (documents.isEmpty) {
                    Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                
                val friendDoc = documents.documents[0]
                val friendId = friendDoc.id
                
                if (friendId == currentUserId) {
                    Toast.makeText(requireContext(), "Cannot add yourself", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                
                // Check if already friends
                db.collection("users").document(currentUserId)
                    .collection("friends")
                    .document(friendId)
                    .get()
                    .addOnSuccessListener { existingFriend ->
                        if (existingFriend.exists()) {
                            Toast.makeText(requireContext(), "Already friends with this user", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }
                        
                        // Add friend to current user's friends list
                        val friendData = hashMapOf(
                            "userId" to friendId,
                            "username" to friendDoc.getString("username"),
                            "name" to friendDoc.getString("name")
                        )
                        
                        db.collection("users").document(currentUserId)
                            .collection("friends")
                            .document(friendId)
                            .set(friendData)
                            .addOnSuccessListener {
                                Toast.makeText(requireContext(), "Friend added successfully", Toast.LENGTH_SHORT).show()
                                binding.usernameEditText.text.clear()
                                loadFriends()
                            }
                            .addOnFailureListener {
                                Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show()
                            }
                    }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Error searching for user", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun removeFriend(friendId: String) {
        val currentUserId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(currentUserId)
            .collection("friends")
            .document(friendId)
            .delete()
            .addOnSuccessListener {
                Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show()
                loadFriends()
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to remove friend", Toast.LENGTH_SHORT).show()
            }
    }
    
    private fun loadFriends() {
        val currentUserId = auth.currentUser?.uid ?: return
        
        db.collection("users").document(currentUserId)
            .collection("friends")
            .get()
            .addOnSuccessListener { documents ->
                val friendsList = mutableListOf<User>()
                for (document in documents) {
                    val friend = User(
                        userId = document.getString("userId") ?: "",
                        name = document.getString("name") ?: "",
                        username = document.getString("username") ?: ""
                    )
                    friendsList.add(friend)
                }
                
                adapter.updateList(friendsList)
                
                if (friendsList.isEmpty()) {
                    binding.noFriendsText.visibility = View.VISIBLE
                    binding.friendsRecyclerView.visibility = View.GONE
                } else {
                    binding.noFriendsText.visibility = View.GONE
                    binding.friendsRecyclerView.visibility = View.VISIBLE
                }
            }
            .addOnFailureListener {
                Toast.makeText(requireContext(), "Failed to load friends", Toast.LENGTH_SHORT).show()
            }
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}