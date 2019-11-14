package com.example.booksharingapp

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.message_appbar_layout.*

class messageActivity : AppCompatActivity() {

    private var friendsUID: String? = null
    private var username: String? = null
    private lateinit var mDatabaseReference: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        mDatabaseReference = FirebaseDatabase.getInstance().reference
        //Display custom Action Bar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.message_appbar_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
       // var view = supportActionBar?.customView
        friendsUID = intent.extras?.get("friendUID").toString()
        username = intent.extras?.get("username").toString()

        displayAppBarLayout()
    }


    fun displayAppBarLayout(){
        message_user_name_id.text = username
        mDatabaseReference.child("Users").child(friendsUID!!).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                Picasso.get()
                    .load(profile_image)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile_name)
                    .fit()
                    .into(message_user_image_id)
            }

        })
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}