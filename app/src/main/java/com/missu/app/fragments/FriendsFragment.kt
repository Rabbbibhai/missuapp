package com.missu.app.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.ktx.Firebase
import com.missu.app.R
import com.missu.app.adapters.FriendsAdapter
import com.missu.app.models.User

class FriendsFragment : Fragment() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var friendsRecyclerView: RecyclerView
    private lateinit var noFriendsText: TextView
    private lateinit var usernameEditText: EditText
    private lateinit var addFriendButton: Button
    
    private val friendsList = mutableListOf<User>()
    private lateinit var adapter: FriendsAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_friends, container, false)

        auth = Firebase.auth
        db = FirebaseFirestore.getInstance()

        friendsRecyclerView = view.findViewById(R.id.friendsRecyclerView)
        noFriendsText = view.findViewById(R.id.noFriendsText)
        usernameEditText = view.findViewById(R.id.usernameEditText)
        addFriendButton = view.findViewById(R.id.addFriendButton)

        setupRecyclerView()
        loadFriends()

        addFriendButton.setOnClickListener {
            addFriend()
        }

        return view
    }

    private fun setupRecyclerView() {
        adapter = FriendsAdapter(friendsList) { friendId ->
            removeFriend(friendId)
        }
        friendsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        friendsRecyclerView.adapter = adapter
    }

    private fun loadFriends() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friends = document.get("friends") as? List<String> ?: emptyList()
                        if (friends.isNotEmpty()) {
                            fetchFriendsDetails(friends)
                        } else {
                            showNoFriends()
                        }
                    }
                }
        }
    }

    private fun fetchFriendsDetails(friendIds: List<String>) {
        friendsList.clear()
        for (friendId in friendIds) {
            db.collection("users").document(friendId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val friend = document.toObject(User::class.java)
                        friend?.let {
                            friendsList.add(it)
                            adapter.notifyDataSetChanged()
                            updateUI()
                        }
                    }
                }
        }
    }

    private fun addFriend() {
        val username = usernameEditText.text?.toString()?.trim() ?: ""
        if (username.isEmpty()) {
            Toast.makeText(requireContext(), "Please enter a username", Toast.LENGTH_SHORT).show()
            return
        }

        val currentUser = auth.currentUser
        currentUser?.let { user ->
            // Find user by username
            db.collection("users")
                .whereEqualTo("username", username)
                .get()
                .addOnSuccessListener { documents ->
                    if (documents.isEmpty) {
                        Toast.makeText(requireContext(), "User not found", Toast.LENGTH_SHORT).show()
                    } else {
                        val friendDoc = documents.documents[0]
                        val friendId = friendDoc.id
                        
                        if (friendId == user.uid) {
                            Toast.makeText(requireContext(), "Cannot add yourself", Toast.LENGTH_SHORT).show()
                            return@addOnSuccessListener
                        }

                        // Add friend to current user's friends list
                        db.collection("users").document(user.uid)
                            .update("friends", com.google.firebase.firestore.FieldValue.arrayUnion(friendId))
                            .addOnSuccessListener {
                                usernameEditText.text?.clear()
                                loadFriends() // Reload friends list
                                Toast.makeText(requireContext(), "Friend added successfully", Toast.LENGTH_SHORT).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(requireContext(), "Failed to add friend", Toast.LENGTH_SHORT).show()
                            }
                    }
                }
        }
    }

    private fun removeFriend(friendId: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            db.collection("users").document(user.uid)
                .update("friends", com.google.firebase.firestore.FieldValue.arrayRemove(friendId))
                .addOnSuccessListener {
                    loadFriends() // Reload friends list
                    Toast.makeText(requireContext(), "Friend removed", Toast.LENGTH_SHORT).show()
                }
                .addOnFailureListener { e ->
                    Toast.makeText(requireContext(), "Failed to remove friend", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUI() {
        if (friendsList.isEmpty()) {
            showNoFriends()
        } else {
            noFriendsText.visibility = View.GONE
            friendsRecyclerView.visibility = View.VISIBLE
        }
    }

    private fun showNoFriends() {
        noFriendsText.visibility = View.VISIBLE
        friendsRecyclerView.visibility = View.GONE
    }
}
