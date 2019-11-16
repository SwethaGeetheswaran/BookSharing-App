package com.example.booksharingapp

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.users_message_layout.view.*

class MessageAdapter (var messageList : List<Messages>) : RecyclerView.Adapter<MessageAdapter.MessageViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.users_message_layout, parent, false))
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        mDatabase = FirebaseDatabase.getInstance()

        val curUserId = mAuth.currentUser?.uid
        val messages = messageList.get(position)
        val fromUserId = messages.from
        mDatabaseReference = mDatabase.reference.child("Users").child(fromUserId!!)
        mDatabaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                        val profileImage = dataSnapshot.child("ProfileImage").value.toString()
                        Picasso.get()
                            .load(profileImage)
                            .placeholder(R.drawable.ic_profile)
                            .into(holder.receiver_profile_image)
                }
            }

        })

        mDatabaseReference = mDatabase.reference.child("Users").child(curUserId!!)
        mDatabaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profileImage = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .into(holder.sender_profile_image)
                }
            }

        })

        if(fromUserId.equals(curUserId)){
            holder.receiver_text_msg.visibility = View.INVISIBLE
            holder.receiver_profile_image.visibility = View.INVISIBLE
            holder.sender_text_msg.setText(messages.message)
        } else {
            holder.sender_text_msg.visibility = View.INVISIBLE
            holder.sender_profile_image.visibility = View.INVISIBLE
            holder.receiver_text_msg.setText(messages.message)
        }



    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val sender_text_msg = view.sender_inMsg_text
        val sender_profile_image = view.sender_inMsg_profile_image
        val receiver_text_msg = view.receiver_inMsg_text
        val receiver_profile_image = view.receiver_inMsg_profile_image

    }
}