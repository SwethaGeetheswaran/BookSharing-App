package com.example.booksharingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_edit_profile.*
import java.util.*
import kotlin.collections.HashMap

class editProfileActivity : AppCompatActivity() {

    private val TAG = "editProfileActivity"
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID:String
    private lateinit var firstName:String
    private lateinit var lastName:String
    private lateinit var password:String
    private lateinit var mUsersDbRef: DatabaseReference
    private lateinit var mUserStorageRefs : StorageReference
    var ImageUri : Uri? = null
    private val mGalleryNo = 1

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
        mUserStorageRefs = FirebaseStorage.getInstance().reference.child("UserProfileImages")


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

                    password = dataSnapshot.child("password").value.toString()


                   val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                   Picasso.get()
                       .load(profile_image)
                       .placeholder(R.drawable.ic_profile)
                       .error(R.drawable.ic_profile_name)
                       .fit()
                       .into(edit_profile_image)

                   if(! dataSnapshot.child("about_myself").exists()) {
                       edit_profile_about_myself_2.setText(R.string.describe_yourself)
                   } else {
                       val about_myself = dataSnapshot.child("about_myself").value.toString()
                       edit_profile_about_myself_2.setText(about_myself)
                   }
               }
            }

        })


        edit_profile_username_button.setOnClickListener {
            addViewsToEditUsername()
        }

        edit_profile_about_myself_button.setOnClickListener {
            addViewsToEditAboutMyself()
        }

        edit_profile_password_button.setOnClickListener {
            val startChangePassIntent = Intent(this, changePassword::class.java)
            startChangePassIntent.putExtra("password", password)
            startActivity(startChangePassIntent)
        }

        edit_profile_image.setOnClickListener {
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_PICK
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, mGalleryNo)
        }

    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if(requestCode == mGalleryNo && resultCode == Activity.RESULT_OK && data != null)
        {
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode == Activity.RESULT_OK ) {
                    ImageUri = result.uri
                    Picasso.get()
                        .load(ImageUri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(edit_profile_image)
                    uploadProfileImagetoFirebase(ImageUri)
                }
            }


    }


    fun uploadProfileImagetoFirebase(resultUri : Uri?){
        if (resultUri == null) {
            Log.v(TAG, "post_it_image")
            val snackbar = Snackbar
                .make(relative_layout_head, "Please select an image", Snackbar.LENGTH_LONG)
                .setAction("OK", object : View.OnClickListener {
                    override fun onClick(view: View) {}
                })
            snackbar.setActionTextColor(Color.WHITE)
            snackbar.show()
        } else {
            val filePath: StorageReference =
                mUserStorageRefs.child(UUID.randomUUID().toString()+ ".jpg")
            filePath.putFile(ImageUri!!).addOnSuccessListener {
                Log.d(TAG,"Successfully uploaded image: ${it.metadata?.path}")
                filePath.downloadUrl.addOnSuccessListener {
                    mUsersDbRef.child("ProfileImage").setValue(it.toString())
                }
            }
        }


    }

    private fun addViewsToEditAboutMyself() {

        edit_profile_about_myself_2.visibility = View.INVISIBLE

        val edit_about_myself = EditText(this)
        edit_about_myself.id = ViewCompat.generateViewId()
        edit_about_myself.setBackgroundResource(R.drawable.receiver_msg_txt)
        edit_about_myself.setHint(" ")
        val layoutParams1= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams1.addRule(RelativeLayout.BELOW, edit_profile_about_myself_2.id)
        layoutParams1.addRule(RelativeLayout.ALIGN_LEFT, edit_profile_about_myself_2.id)
        edit_about_myself.setPadding(20, 40, 20, 20)
        edit_profile_myself_layout.addView(edit_about_myself,layoutParams1)


        // Dynamically created a SAVE button to save the user's About myself value.
        val save_button = Button(this)
        save_button.id = ViewCompat.generateViewId()
        save_button.setText(R.string.save_text)
        val layoutParams2= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams2.addRule(RelativeLayout.BELOW, edit_about_myself.id)
        layoutParams2.addRule(RelativeLayout.ALIGN_PARENT_RIGHT, edit_about_myself.id)
        save_button.setPadding(20, 20, 20, 20)
        edit_profile_myself_layout.addView(save_button,layoutParams2)

        //CANCEL button to cancel the dynamically created views
        val cancel_button = Button(this)
        cancel_button.setText(R.string.cancel_text)
        val layoutParams3= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams3.addRule(RelativeLayout.BELOW, edit_about_myself.id)
        layoutParams3.addRule(RelativeLayout.START_OF, save_button.id)
        cancel_button.setPadding(20, 20, 20, 20)
        edit_profile_myself_layout.addView(cancel_button,layoutParams3)

        cancel_button.setOnClickListener {
            edit_profile_about_myself_2.visibility = View.VISIBLE
            edit_profile_myself_layout.removeView(save_button)
            edit_profile_myself_layout.removeView(edit_about_myself)
            edit_profile_myself_layout.removeView(cancel_button)
        }

        // SAVE the edited About_myself value in Firebase Database.
        save_button.setOnClickListener {

            val about_myself = edit_about_myself.text.toString()
            if(TextUtils.isEmpty(about_myself)){
                Toast.makeText(this,"About Myself field cannot be saved as blank", Toast.LENGTH_SHORT).show()
            } else {
                val userinfo_updates_hashMap = HashMap<String,Any>()
                userinfo_updates_hashMap.put("about_myself", about_myself)

                mUsersDbRef.updateChildren(userinfo_updates_hashMap)
                    .addOnSuccessListener {
                        edit_profile_about_myself_2.setText(about_myself)
                        edit_profile_about_myself_2.visibility = View.VISIBLE
                        edit_profile_myself_layout.removeView(save_button)
                        edit_profile_myself_layout.removeView(edit_about_myself)
                        edit_profile_myself_layout.removeView(cancel_button)
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
                    }
            }
        }
    }


    // Added multiple views using Dynamic layout to edit Username
    fun addViewsToEditUsername(){
        Log.v("editProfile", "username-2")
        edit_profile_username_2.visibility = View.INVISIBLE

        // Edit first name
        val edit_first_name = EditText(this)
        edit_first_name.id = ViewCompat.generateViewId()
        edit_first_name.setHint(firstName)
        edit_first_name.setBackgroundResource(R.drawable.receiver_msg_txt)
        val layoutParams= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams.addRule(RelativeLayout.BELOW, edit_profile_username_1.id)
        edit_first_name.setPadding(20, 40, 20, 20)
        edit_profile_relative_layout.addView(edit_first_name,layoutParams)

        // Edit last name
        val edit_last_name = EditText(this)
        edit_last_name.id = ViewCompat.generateViewId()
        edit_last_name.setBackgroundResource(R.drawable.receiver_msg_txt)
        edit_last_name.setHint(lastName)
        val layoutParams1= RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.WRAP_CONTENT)
        layoutParams1.addRule(RelativeLayout.BELOW, edit_first_name.id)
        edit_last_name.setPadding(20, 40, 20, 20)
        edit_profile_relative_layout.addView(edit_last_name,layoutParams1)

        // Add save button
        val save_button = Button(this)
        save_button.id = ViewCompat.generateViewId()
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


        // SAVE the edited Username value in Firebase Database.
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
                    }
                    .addOnFailureListener {
                        Log.v(TAG, it.printStackTrace().toString())
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
