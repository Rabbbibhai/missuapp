package com.missu.app

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class MyFirebaseMessagingService : FirebaseMessagingService() {
    
    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Update user's FCM token in Firestore
        updateFCMToken(token)
    }
    
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        
        // Handle the notification
        remoteMessage.notification?.let { notification ->
            sendNotification(notification.title ?: "", notification.body ?: "")
        }
    }
    
    private fun sendNotification(title: String, body: String) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        
        // Create notification channel for Android O and above
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "missu_channel",
                "Missu Notifications",
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(channel)
        }
        
        val notificationBuilder = NotificationCompat.Builder(this, "missu_channel")
            .setContentTitle(title)
            .setContentText(body)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setAutoCancel(true)
        
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
    
    private fun updateFCMToken(token: String) {
        val currentUser = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser
        currentUser?.let { user ->
            com.google.firebase.firestore.FirebaseFirestore.getInstance()
                .collection("users")
                .document(user.uid)
                .update("fcmToken", token)
                .addOnFailureListener { e ->
                    Log.e("FCM", "Error updating FCM token", e)
                }
        }
    }
}