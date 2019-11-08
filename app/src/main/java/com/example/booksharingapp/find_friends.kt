package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_find_friends.*
import kotlinx.android.synthetic.main.home_activity_screen.*

class find_friends : AppCompatActivity() {

    var searchFriendsName:String? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)
        supportActionBar?.setTitle(R.string.find_friends)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        currentUserID = mAuth.currentUser!!.uid

        recycler_result_list.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        recycler_result_list.layoutManager = linearLayoutManager

        search_friends_button.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
               searchFriendsName = search_friends_name.text.toString()
                findFriendsinFirebaseDatabase()
            }

        })
    }

    private fun findFriendsinFirebaseDatabase() {

        val searchFriendsQuery = mDatabaseReference.orderByChild("firstName")
            .startAt(searchFriendsName).endAt(searchFriendsName + "uf8ff")


        val option = FirebaseRecyclerOptions.Builder<UsersList>()
            .setQuery(searchFriendsQuery, UsersList::class.java)
            .setLifecycleOwner(this)
            .build()


        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<UsersList,allUsersListViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): allUsersListViewHolder {
                return allUsersListViewHolder(
                    (LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_display_list, parent, false))
                )
            }

            override fun onBindViewHolder(holder: allUsersListViewHolder, position: Int, model: UsersList) {
                val placeid = getRef(position).key.toString()

                mDatabaseReference.child(placeid).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) { }
                    override fun onDataChange(p0: DataSnapshot) {
                        val fullName = model.firstName + model.lastName
                        holder.user_fullName.text= fullName
                        Picasso.get().load(model.ProfileImage).into(holder.user_profileImage)
                    }
                })
                holder.itemView.setOnClickListener(object :View.OnClickListener{
                    override fun onClick(p0: View?) {
                        val friendUID = getRef(position).key
                        val friendsProfileIntent = Intent(this@find_friends, friends_profile_activity::class.java)
                        friendsProfileIntent.putExtra("friendUID", friendUID)
                        startActivity(friendsProfileIntent)

                    }

                })
            }

        }
        recycler_result_list.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }


    class allUsersListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var user_fullName = itemView.findViewById<TextView>(R.id.search_profile_name)
       internal var user_profileImage = itemView.findViewById<ImageView>(R.id.search_profile_image)

    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, find_friends::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

}
