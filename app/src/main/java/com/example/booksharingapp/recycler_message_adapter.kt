package com.example.booksharingapp

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_recycler_list.view.*

class recycler_message_adapter (var messageList : List<Messages>) : RecyclerView.Adapter<recycler_message_adapter.MessageViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference
    var userId:String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {

        mAuth = FirebaseAuth.getInstance()
        return MessageViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.message_recycler_list, parent, false))
    }

    override fun getItemCount(): Int {
        return messageList.size
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {

        mDatabase = FirebaseDatabase.getInstance()

        val curUserId = mAuth.currentUser?.uid
        val messages = messageList.get(position)
        val fromUserId = messages.from

        if(curUserId.equals(fromUserId)) {
            userId = messages.to
        } else {
            userId = messages.from
        }

        mDatabaseReference = mDatabase.reference.child("Users").child(userId!!)
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profile_first_name = dataSnapshot.child("firstName").value.toString()
                    val profile_last_name = dataSnapshot.child("lastName").value.toString()
                    val user_name = profile_first_name + "" + profile_last_name
                    holder.receiver_text_name.setText(user_name)

                    val profileImage = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profileImage)
                        .placeholder(R.drawable.ic_profile)
                        .into(holder.receiver_profile_image)
                }
            }

        })
            holder.receiver_text_msg.setText(messages.message)

        holder.itemView.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                val detailedMessageActivity = Intent(p0?.context,messageActivity::class.java)
                detailedMessageActivity.putExtra("friendUID",userId)
                detailedMessageActivity.putExtra("username",holder.receiver_text_name.text.toString())
                p0?.context?.startActivity(detailedMessageActivity)
            }

        })
    }

    class MessageViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val receiver_text_name = view.recycler_sender_name
        val receiver_profile_image = view.recycler_sender_image
        val receiver_text_msg = view.recycler_sender_msg

    }
}