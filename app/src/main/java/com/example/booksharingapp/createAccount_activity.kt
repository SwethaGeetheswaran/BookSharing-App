package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.create_account.*


class createAccount_Activity : AppCompatActivity() {

    private val TAG = "createAccount_activity"
    private var mDatabaseReference: DatabaseReference? = null
    private var mDatabase: FirebaseDatabase? = null
    private var mAuth: FirebaseAuth? = null
    private var mProgressBar: ProgressBar? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.create_account)

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase!!.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        mProgressBar = ProgressBar(this)

        create_account_button.setOnClickListener { createUserAccount() }

    }

    fun createUserAccount(){
        if (!TextUtils.isEmpty(first_name?.toString()) && !TextUtils.isEmpty(last_name.toString())
            && !TextUtils.isEmpty(email.toString()) && !TextUtils.isEmpty(password.toString())
            && !TextUtils.isEmpty(sec_password.toString())) { createUser() }
        else {
            Toast.makeText(this, "Please enter all details to create an account", Toast.LENGTH_SHORT).show()
        }
    }

    fun createUser(){
        mAuth!!
            .createUserWithEmailAndPassword(email.text.toString().trim(), password.text.toString().trim())
            .addOnCompleteListener(this) { task ->
                mProgressBar!!.isVisible = false
                if (task.isSuccessful) {
                    Log.d(TAG, "createUserWithEmail:success")
                    val userId = mAuth!!.currentUser!!.uid
                    verifyEmail()
                    val currentUserDb = mDatabaseReference!!.child(userId)
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
    val mUser = mAuth!!.currentUser
    mUser!!.sendEmailVerification()
        .addOnCompleteListener(this) { task ->
            if (task.isSuccessful) {
                Toast.makeText(this,
                    "Verification email sent to " + mUser.getEmail(),
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
        //start next activity
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