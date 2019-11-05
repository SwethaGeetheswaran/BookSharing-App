package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile_activity.*

class user_profile_activity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_activity)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mDatabaseReference= FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)

        mDatabaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profile_first_name = dataSnapshot.child("firstName").value.toString()
                    val profile_last_name = dataSnapshot.child("lastName").value.toString()
                    val user_name = profile_first_name + " "  + profile_last_name
                    //var profile_image = dataSnapshot.child("ProfileImage").toString()

                    //Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile).into(user_profile)
                    user_profile_name.text = user_name
                }
            }

        })
    }


    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, user_profile_activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}
