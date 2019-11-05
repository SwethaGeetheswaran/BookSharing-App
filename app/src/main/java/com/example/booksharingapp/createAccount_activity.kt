package com.example.booksharingapp

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.UploadTask
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.create_account.*
import java.util.regex.Matcher
import java.util.regex.Pattern

class createAccount_Activity : AppCompatActivity() {

    private val TAG = "createAccount_activity"
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private val mGalleryNo = 1
    private lateinit var mUserProfileImage : StorageReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        mUserProfileImage = FirebaseStorage.getInstance().reference.child("UserProfileImages")
        mAuth = FirebaseAuth.getInstance()

        create_account_button.setOnClickListener { createUserAccount() }

        // Profile image
        create_account_profile_image.setOnClickListener{
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, mGalleryNo)
        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if(requestCode == mGalleryNo && resultCode == Activity.RESULT_OK && data != null){

            //val ImageUri  : Uri? = data.data
            // start picker to get image for cropping and then use the image in cropping activity
            CropImage.activity()
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }

        if (requestCode === CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE) {
            val result = CropImage.getActivityResult(data)
            if (resultCode === Activity.RESULT_OK) {
                val resultUri = result.uri
                val filePath: StorageReference =
                    mUserProfileImage.child(mAuth.currentUser!!.uid + ".jpg")
                filePath.putFile(resultUri).addOnCompleteListener(object :
                    OnCompleteListener<UploadTask.TaskSnapshot> {
                    override fun onComplete(task: Task<UploadTask.TaskSnapshot>) {
                        if(task.isSuccessful){
                            val downloadUrl :String = task.result.toString()
                            mDatabaseReference.child("ProfileImage").setValue(downloadUrl)
                                .addOnCompleteListener(object :OnCompleteListener<Void>{
                                    override fun onComplete(task: Task<Void>) {
                                        if(task.isSuccessful){
                                            val selfIntent = Intent(this@createAccount_Activity, createAccount_Activity::class.java)
                                            startActivity(selfIntent)
                                            Toast.makeText(this@createAccount_Activity,"Profile image has been successfully stored to Firebase Database",Toast.LENGTH_SHORT).show()
                                        }
                                        if(!task.isSuccessful){
                                            val errorMessage = task.exception?.localizedMessage
                                            Toast.makeText(this@createAccount_Activity,"Error while storing image to Firebase Database" + errorMessage,Toast.LENGTH_SHORT).show()
                                        }
                                    }

                                })
                        }
                    }
                })

            } else{
                Toast.makeText(this@createAccount_Activity,"Error while cropping image. Please try again.",Toast.LENGTH_SHORT).show()
            }
        }
    }

    // To validate password
    fun isValidPassword(password: String): Boolean {

        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)

        return matcher.matches()

    }

    //  Password validation condition checks - Should not be less than 8 characters. Both the passwords must match.
    fun createUserAccount(){
        val first_password = password.text.toString().trim()
        val second_password = sec_password.text.toString().trim()
        if (!TextUtils.isEmpty(first_name?.text.toString()) && !TextUtils.isEmpty(last_name?.text.toString())
            && !TextUtils.isEmpty(email?.text.toString()) && !TextUtils.isEmpty(first_password)
            && !TextUtils.isEmpty(second_password)) {

            Log.v(TAG, "1: " + first_password)
            Log.v(TAG, "2: " +second_password.length)
            if((first_password.length < 8 || second_password.length < 8)&& (!isValidPassword(first_password ) ||!isValidPassword(second_password)) ) {
                Toast.makeText(this, "Password length is too short. Please enter a minimum of 8 characters.", Toast.LENGTH_SHORT).show()
            }
            else if(first_password.equals(second_password)) createUser()
            else { Toast.makeText(this, "Mis-match between passwords!. Please re-try again.",Toast.LENGTH_SHORT).show() }
        }
        else {
            Toast.makeText(this, "Please enter all details to create an account", Toast.LENGTH_SHORT).show()
        }
    }

    // To fetch the user data and save it in Firebase database.
    fun createUser(){
        mAuth
            .createUserWithEmailAndPassword(email.text.toString().trim(), password.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val userId = mAuth.currentUser!!.uid
                    verifyEmail()
                    val currentUserDb = mDatabaseReference.child(userId)
                    currentUserDb.child("firstName").setValue(first_name.text.toString().trim())
                    currentUserDb.child("lastName").setValue(last_name.text.toString().trim())
                    updateUserInfoAndUI()
                } else {
                    Log.w(TAG, "createUserWithEmail:failure", task.exception)
                    Toast.makeText(this, "Authentication failed.",
                        Toast.LENGTH_SHORT).show()
                }
            }
    }

    private fun verifyEmail() {
    val mUser = mAuth.currentUser
        Log.v(TAG, "email-1: " + mUser?.email)
    mUser!!.sendEmailVerification()
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Log.v(TAG, "email: " + mUser.email)
                Toast.makeText(this,
                    "Account vertification was successful.Verification email sent to " + mUser.email,
                    Toast.LENGTH_SHORT).show()
            } else {
                Log.e("createAccount_activity", "sendEmailVerification", task.exception)
                Toast.makeText(this,
                    "Failed to send verification email.",
                    Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserInfoAndUI() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, createAccount_Activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

}