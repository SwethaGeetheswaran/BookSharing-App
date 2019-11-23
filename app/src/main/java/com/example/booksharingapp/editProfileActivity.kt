package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.*
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.HashMap

class editProfileActivity : AppCompatActivity() {

    private val TAG = "editProfileActivity"
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String
    private lateinit var firstName:String
    private lateinit var lastName:String
    private lateinit var mUsersDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_edit_profile)
        supportActionBar?.setTitle(R.string.settings)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        mUsersDbRef = mDatabase.reference.child("Users").child(currentUserID)


        mUsersDbRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
               if(dataSnapshot.exists()){
                    firstName = dataSnapshot.child("firstName").value.toString()
                    lastName = dataSnapshot.child("lastName").value.toString()
                    val username = firstName + lastName
                    edit_profile_username_2.setText(username)

                    val email = dataSnapshot.child("email").value.toString()
                    edit_profile_email_2.setText(email)

                    val password = dataSnapshot.child("password").value.toString()
                    edit_profile_password_2.setText(password)

                   val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                   Picasso.get()
                       .load(profile_image)
                       .placeholder(R.drawable.ic_profile)
                       .error(R.drawable.ic_profile_name)
                       .fit()
                       .into(edit_profile_image)
               }
            }

        })

        edit_profile_username_button.setOnClickListener {

            edit_profile_username_2.visibility = View.INVISIBLE

            // Edit first name
            val edit_id = 1
            val edit_first_name = EditText(this)
            edit_first_name.id = edit_id
            edit_first_name.setHint(firstName)
            edit_first_name.setBackgroundResource(R.drawable.receiver_msg_txt)
            val layoutParams= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams.addRule(RelativeLayout.BELOW, edit_profile_username_1.id)
            edit_first_name.setPadding(20, 40, 20, 20)
            edit_profile_relative_layout.addView(edit_first_name,layoutParams)

            // Edit last name
            val edit_last_name_id = 2
            val edit_last_name = EditText(this)
            edit_last_name.id = edit_last_name_id
            edit_last_name.setBackgroundResource(R.drawable.receiver_msg_txt)
            edit_last_name.setHint(lastName)
            val layoutParams1= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams1.addRule(RelativeLayout.BELOW, edit_first_name.id)
            edit_last_name.setPadding(20, 40, 20, 20)
            edit_profile_relative_layout.addView(edit_last_name,layoutParams1)

            // Add save button
            val save_button_id = 3
            val save_button = Button(this)
            save_button.id = save_button_id
            save_button.setText(R.string.save_text)
            val layoutParams2= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams2.addRule(RelativeLayout.BELOW, edit_last_name.id)
            layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_last_name.id)
            save_button.setPadding(20, 20, 20, 20)
            edit_profile_relative_layout.addView(save_button,layoutParams2)

            // Add Cancel button
            val cancel_button = Button(this)
            cancel_button.setText(R.string.cancel_text)
            val layoutParams3= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
            layoutParams3.addRule(RelativeLayout.BELOW, edit_last_name.id)
            layoutParams3.addRule(RelativeLayout.START_OF, save_button.id)
            cancel_button.setPadding(20, 20, 20, 20)
            edit_profile_relative_layout.addView(cancel_button,layoutParams3)



            cancel_button.setOnClickListener {
                edit_profile_username_2.visibility = View.VISIBLE
                edit_profile_relative_layout.removeView(save_button)
                edit_profile_relative_layout.removeView(edit_first_name)
                edit_profile_relative_layout.removeView(edit_last_name)
                edit_profile_relative_layout.removeView(cancel_button)
            }


            save_button.setOnClickListener {
                if(TextUtils.isEmpty(edit_first_name.text.toString()) || TextUtils.isEmpty(edit_last_name.text.toString())){
                    Toast.makeText(this,"User name cannot be blank", Toast.LENGTH_SHORT).show()
                } else {

                    val edited_firstName = edit_first_name.text.toString()
                    val edited_lastName = edit_last_name.text.toString()
                    val userinfo_updates_hashMap = HashMap<String,Any>()
                    userinfo_updates_hashMap.put("firstName", edited_firstName)
                    userinfo_updates_hashMap.put("lastName", edited_lastName)

                    val edited_username = edited_firstName + edited_lastName
                    mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                        .addOnSuccessListener {
                            edit_profile_username_2.setText(edited_username)
                            edit_profile_username_2.visibility = View.VISIBLE
                            edit_profile_relative_layout.removeView(save_button)
                            edit_profile_relative_layout.removeView(edit_first_name)
                            edit_profile_relative_layout.removeView(edit_last_name)
                            edit_profile_relative_layout.removeView(cancel_button)
                            edit_profile_relative_layout.removeView(save_button)
                        }
                        .addOnFailureListener {
                            Log.v(TAG, it.printStackTrace().toString())
                        }
                }
            }

        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, editProfileActivity::class.java).apply {
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
