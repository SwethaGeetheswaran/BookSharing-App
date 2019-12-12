package com.example.booksharingapp

import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class searchBook_adapter(var userIdList: List<String>) : RecyclerView.Adapter<searchBook_adapter.UserIdsViewHolder>(){

    private lateinit var mUsersDB: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UserIdsViewHolder {
        return UserIdsViewHolder((LayoutInflater.from(parent.context)
                .inflate(R.layout.searchbooks_display_list, parent, false))
        )
    }

    override fun getItemCount(): Int {
        return userIdList.size
    }

    override fun onBindViewHolder(holder: UserIdsViewHolder, position: Int) {

        mDatabase = FirebaseDatabase.getInstance()
        mUsersDB = mDatabase.reference.child("Users")
        Log.v("adapter: ","position:" +position)
        val userId = userIdList.get(position)

        Log.v("adapter: ","userID" +userId)
        mUsersDB.child(userId).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("firstName") && dataSnapshot.hasChild("lastName")) {
                        val user_first_name = dataSnapshot.child("firstName").value.toString()
                        val user_last_name = dataSnapshot.child("lastName").value.toString()
                        val user_name = user_first_name + " " + user_last_name
                        holder.user_fullName.text = user_name
                    }
                    if (dataSnapshot.hasChild("ProfileImage")) {
                        val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                        Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile)
                            .into(holder.user_profile_image)
                    }
                }
            }
        })
        holder.send_message.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val messageActivity = Intent(p0?.context,messageActivity::class.java)
                messageActivity.putExtra("friendUID",userId)
                p0?.context!!.startActivity(messageActivity)
            }

        })
    }

    class UserIdsViewHolder(view: View) : RecyclerView.ViewHolder(view){
        internal var user_fullName = itemView.findViewById<TextView>(R.id.search_profile_name)
        internal var user_profile_image = itemView.findViewById<ImageView>(R.id.search_profile_image)
        internal var distance = itemView.findViewById<TextView>(R.id.distance_id)
        internal var send_message = itemView.findViewById<Button>(R.id.send_message_button)

    }

}