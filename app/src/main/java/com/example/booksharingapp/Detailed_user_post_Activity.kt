package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detailed_user_post_.*

class Detailed_user_post_Activity : AppCompatActivity() {

    private var placeId:String? = null
    private var currentUserId:String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_detailed_user_post_)
        supportActionBar?.setTitle(R.string.edit_del_user_post)

        detailed_edit_post_button.visibility = View.INVISIBLE
        detailed_delete_post_button.visibility = View.INVISIBLE

        placeId = intent.extras?.get("placeId").toString()
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser?.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("Users Posts").child(placeId!!)

        mDatabaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(p0: DataSnapshot) {
                val user_post_text = p0.child("description").value.toString()
                val user_post_image = p0.child("postimage").value.toString()

                detailed_edit_post_text.text = user_post_text
                Picasso.get().load(user_post_image).fit().into(detailed_edit_post_image)

                val databaseUserId = p0.child("UID").value.toString()

                if(currentUserId.equals(databaseUserId)){
                    detailed_edit_post_button.visibility = View.VISIBLE
                    detailed_delete_post_button.visibility = View.VISIBLE
                }

            }

        })
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, Detailed_user_post_Activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


}
