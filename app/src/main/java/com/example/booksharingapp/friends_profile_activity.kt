package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile_activity.*

class friends_profile_activity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private var friendsUID: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_activity)
        supportActionBar?.setTitle(R.string.profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mDatabaseReference =
            FirebaseDatabase.getInstance().reference.child("Users")

        friendsUID = intent.getStringExtra("friendUID").toString()

        remove_frd_req_button.visibility = View.INVISIBLE
        if(currentUserId.equals(friendsUID)){
            send_frd_req_button.visibility = View.INVISIBLE
        }

        mDatabaseReference.child(friendsUID!!).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profile_first_name = dataSnapshot.child("firstName").value.toString()
                    val profile_last_name = dataSnapshot.child("lastName").value.toString()
                    val user_name = profile_first_name + " "  + profile_last_name
                    user_profile_name.text = user_name

                    val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profile_image)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(user_profile)
                }
            }

        })
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, user_profile_activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}