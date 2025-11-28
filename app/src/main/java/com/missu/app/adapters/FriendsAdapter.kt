package com.missu.app.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.missu.app.models.User
import com.missu.app.R

class FriendsAdapter(
    private val context: Context,
    private var friendsList: MutableList<User>,
    private val onRemoveClick: (String) -> Unit
) : RecyclerView.Adapter<FriendsAdapter.FriendViewHolder>() {

    inner class FriendViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val friendName: TextView = itemView.findViewById(R.id.friendName)
        val friendUsername: TextView = itemView.findViewById(R.id.friendUsername)
        val removeButton: TextView = itemView.findViewById(R.id.removeButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FriendViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_friend, parent, false)
        return FriendViewHolder(view)
    }

    override fun onBindViewHolder(holder: FriendViewHolder, position: Int) {
        val friend = friendsList[position]
        
        holder.friendName.text = friend.name
        holder.friendUsername.text = "@${friend.username}"
        
        holder.removeButton.setOnClickListener {
            onRemoveClick(friend.userId)
        }
    }

    override fun getItemCount(): Int {
        return friendsList.size
    }

    fun updateList(newList: MutableList<User>) {
        friendsList.clear()
        friendsList.addAll(newList)
        notifyDataSetChanged()
    }
}