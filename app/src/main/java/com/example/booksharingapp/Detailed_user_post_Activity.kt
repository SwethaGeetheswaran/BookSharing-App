package com.example.booksharingapp

import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_detailed_user_post_.*

class Detailed_user_post_Activity : AppCompatActivity() {

    private var placeId:String? = null
    private var currentUserId:String? = null
    private var user_post_text:String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabaseReference: DatabaseReference
    private var TAG = "Detailed_user_post_Activity"


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
                if(p0.exists()){
                    user_post_text = p0.child("description").value.toString()
                    val user_post_image = p0.child("postimage").value.toString()

                    detailed_edit_post_text.text = user_post_text
                    Picasso.get().load(user_post_image).fit().into(detailed_edit_post_image)

                    val databaseUserId = p0.child("UID").value.toString()

                    if(currentUserId.equals(databaseUserId)){
                        detailed_edit_post_button.visibility = View.VISIBLE
                        detailed_delete_post_button.visibility = View.VISIBLE
                    }
                }
            }

        })

        //Delete post
        detailed_delete_post_button.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                Log.v(TAG,"delete post first")
                deleteUserPost()
            }

        })

            //Edit user text in Post
        detailed_edit_post_button.setOnClickListener(object : View.OnClickListener {
            override fun onClick(p0: View?) {
                EditUserPost(user_post_text)
            }
        })
    }

    private fun deleteUserPost(){
        Log.v(TAG, "delete post")
        mDatabaseReference.removeValue()
        startActivity(HomeActivity.getLaunchIntent(this@Detailed_user_post_Activity))
        Toast.makeText(this@Detailed_user_post_Activity, "Post has been deleted", Toast.LENGTH_SHORT).show()
    }

    //Using ALert Dialog display the existing text in Edit Text.
    //1. Performs two operations - "Update" and "Cancel"
    private fun EditUserPost(userEditText: String?) {
        val builder = AlertDialog.Builder(this)
        builder.setTitle("Edit your post")

        val inputField = EditText(this)
        inputField.setText(userEditText)
        builder.setView(inputField)

        builder.setPositiveButton("Update", object: DialogInterface.OnClickListener{
            override fun onClick(p0: DialogInterface?, p1: Int) {
                mDatabaseReference.child("description").setValue(inputField.text.toString())
            }
        })

        builder.setNegativeButton("Cancel", object :DialogInterface.OnClickListener{
            override fun onClick(dialog: DialogInterface?, p1: Int) {
                dialog?.cancel()
            }
        })

        builder.show()


    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, Detailed_user_post_Activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }


}
