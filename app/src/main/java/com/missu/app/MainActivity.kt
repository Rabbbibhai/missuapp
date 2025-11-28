package com.missu.app

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.google.firebase.auth.FirebaseAuth
import com.missu.app.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityMainBinding
    private lateinit var auth: FirebaseAuth
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        auth = FirebaseAuth.getInstance()
        
        // Check if user is logged in
        if (auth.currentUser == null) {
            startLoginActivity()
            return
        }
        
        setupNavigation()
    }
    
    private fun setupNavigation() {
        val navView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment)
        
        // Set up navigation
        navView.setupWithNavController(navController)
    }
    
    private fun startLoginActivity() {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
    }
}