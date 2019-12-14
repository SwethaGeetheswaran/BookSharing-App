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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso

class searchBook_adapter(var userIdList: List<String>) : RecyclerView.Adapter<searchBook_adapter.UserIdsViewHolder>(){

    private lateinit var mUsersDB: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String
    private lateinit var currUserLat:String
    private lateinit var currUserLong:String
    private lateinit var bookHolderLat:String
    private lateinit var bookHolderLong:String


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
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        Log.v("adapter: ","position:" +position)
        val userId = userIdList.get(position)

        mUsersDB.child(currentUserID).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException() }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    if(p0.hasChild("Longitude") && p0.hasChild("Latitude")){
                        currUserLat = p0.child("Latitude").value.toString()
                        currUserLong = p0.child("Longitude").value.toString()
                    }
                }
            }

        })
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
                    if(dataSnapshot.hasChild("Longitude") && dataSnapshot.hasChild("Latitude")){
                        bookHolderLat = dataSnapshot.child("Latitude").value.toString()
                        bookHolderLong = dataSnapshot.child("Longitude").value.toString()
                        val calculatedDistance = distance(currUserLat.toDouble(),currUserLong.toDouble(),
                            bookHolderLat.toDouble(),bookHolderLong.toDouble())
                        Log.v("searchAdapter", "dist:" +calculatedDistance)
                        val dist= String.format("%.1f", calculatedDistance).toDouble()
                        holder.distance.text = dist.toString() + "mi"
                    }
                }
            }
        })
        holder.send_message.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val messageActivity = Intent(p0?.context,messageActivity::class.java)
                messageActivity.putExtra("friendUID",userId)
                messageActivity.putExtra("username",holder.user_fullName.text)
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