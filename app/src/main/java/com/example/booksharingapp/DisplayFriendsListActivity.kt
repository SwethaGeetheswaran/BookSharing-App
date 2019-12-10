package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_display_friends_list.*

class DisplayFriendsListActivity : AppCompatActivity() {

    private lateinit var mFriendsDbRef: DatabaseReference
    private lateinit var mUsersDbRef: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String
    private  var TAG = "DisplayFriendsListActivity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_display_friends_list)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        mUsersDbRef = mDatabase.reference.child("Users")
        mFriendsDbRef = mDatabase.reference.child("Friends").child(currentUserID)

        supportActionBar?.setTitle(R.string.friends_list)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        display_friends_list_recycler_id.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        display_friends_list_recycler_id.layoutManager = linearLayoutManager

        displayAllFriendsFromFirebaseDatabase()
    }

    private fun displayAllFriendsFromFirebaseDatabase() {
        val option = FirebaseRecyclerOptions.Builder<UsersList>()
            .setQuery(mFriendsDbRef, UsersList::class.java)
            .setLifecycleOwner(this)
            .build()


        val firebaseRecyclerAdapter = object : FirebaseRecyclerAdapter<UsersList,allFriendsListViewHolder>(option) {
            override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):allFriendsListViewHolder {
                return allFriendsListViewHolder(
                    (LayoutInflater.from(parent.context)
                        .inflate(R.layout.search_display_list, parent, false))
                )
            }

            override fun onBindViewHolder(holder: allFriendsListViewHolder, position: Int, model: UsersList) {
                val placeid = getRef(position).key.toString()

                mUsersDbRef.child(placeid).addValueEventListener(object : ValueEventListener {
                    override fun onCancelled(p0: DatabaseError) { }
                    override fun onDataChange(dataSnapshot: DataSnapshot) {
                        if(dataSnapshot.exists()){
                            val firstName = dataSnapshot.child("firstName").value.toString()
                            val lastName = dataSnapshot.child("lastName").value.toString()
                            val fullName = firstName + lastName
                            holder.user_fullName.text= fullName
                            Picasso.get().load(dataSnapshot.child("ProfileImage").value.toString()).into(holder.user_profileImage)
                        }

                    }
                })
                holder.itemView.setOnClickListener(object :View.OnClickListener{
                    override fun onClick(p0: View?) {
                        val options = arrayOf<CharSequence>("View " + holder.user_fullName.text +"'s Profile", "Send a message", "Cancel")

                        val builder = AlertDialog.Builder(this@DisplayFriendsListActivity)
                        builder.setTitle("Select Options")

                        builder.setItems(options) { dialog, item ->
                            if (options[item] == "View " + holder.user_fullName.text +"'s Profile") {
                                val displayFriendsProfile = Intent (this@DisplayFriendsListActivity, friends_profile_activity::class.java)
                                displayFriendsProfile.putExtra("friendUID", placeid)
                                Log.v(TAG,"friendUID: " +placeid)
                                startActivity(displayFriendsProfile)

                            } else if (options[item] == "Send a message") {
                                val sendMessageToFriends = Intent (this@DisplayFriendsListActivity, messageActivity::class.java)
                                sendMessageToFriends.putExtra("friendUID", placeid)
                                sendMessageToFriends.putExtra("username", holder.user_fullName.text)
                                startActivity(sendMessageToFriends)

                            } else if (options[item] == "Cancel") {
                                dialog.dismiss()
                            }
                        }
                        builder.show()
                    }

                })
            }

        }
        display_friends_list_recycler_id.adapter= firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }

    class allFriendsListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
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
        fun getLaunchIntent(from: Context) = Intent(from, DisplayFriendsListActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}
