package com.missu.app.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.missu.app.databinding.FragmentProfileBinding

class ProfileFragment : Fragment() {
    
    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }
    
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        
        loadUserData()
        setupClickListeners()
    }
    
    private fun loadUserData() {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            binding.userName.text = user.displayName ?: "User"
            binding.userEmail.text = user.email ?: ""
            
            // Load username from Firestore
            db.collection("users").document(user.uid).get()
                .addOnSuccessListener { document ->
                    val username = document.getString("username")
                    if (username != null) {
                        binding.userUsername.text = "@$username"
                    }
                }
        }
    }
    
    private fun setupClickListeners() {
        binding.changeUsernameButton.setOnClickListener {
            showChangeUsernameDialog()
        }
        
        binding.settingsButton.setOnClickListener {
            // Navigate to settings (for now show a toast)
            Toast.makeText(requireContext(), "Settings coming soon!", Toast.LENGTH_SHORT).show()
        }
        
        binding.logoutButton.setOnClickListener {
            logout()
        }
    }
    
    private fun showChangeUsernameDialog() {
        val currentUsername = auth.currentUser?.displayName ?: ""
        
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Change Username")
            .setMessage("Enter new username")
            .setView(R.layout.dialog_change_username)
            .setPositiveButton("Change") { dialog, which ->
                // Username change logic would go here
                Toast.makeText(requireContext(), "Username change feature coming soon!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun logout() {
        MaterialAlertDialogBuilder(requireContext())
            .setTitle("Logout")
            .setMessage("Are you sure you want to logout?")
            .setPositiveButton("Logout") { dialog, which ->
                auth.signOut()
                requireActivity().finish()
                // App will restart and go to login screen
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}