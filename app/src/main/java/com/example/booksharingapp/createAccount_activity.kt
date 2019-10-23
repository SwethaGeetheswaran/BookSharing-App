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
import java.util.regex.Matcher
import java.util.regex.Pattern


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

    fun isValidPassword(password: String): Boolean {

        val pattern: Pattern
        val matcher: Matcher
        val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
        pattern = Pattern.compile(PASSWORD_PATTERN)
        matcher = pattern.matcher(password)

        return matcher.matches()

    }
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
            else if(first_password == second_password) createUser()
            else { Toast.makeText(this, "Mis-match between passwords!. Please re-try again.",Toast.LENGTH_SHORT).show() }
        }
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