package com.example.booksharingapp

import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import kotlinx.android.synthetic.main.forgot_password.*

class Forgot_password_activity : AppCompatActivity() {

    private var TAG = "Forgot_password_activity"
    private var mFirebaseAuth: FirebaseAuth? = null
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.forgot_password)

        mFirebaseAuth = FirebaseAuth.getInstance()

        password_reset_button.setOnClickListener { sendPasswordResetEmail() }
    }


    fun sendPasswordResetEmail(){
        val email = reset_email?.text.toString()
        if (!TextUtils.isEmpty(email)) {
            mFirebaseAuth!!.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val message = "Email sent."
                        Log.d(TAG, message)
                        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this,MainActivity::class.java))
                    } else {
                        Log.w(TAG, task.exception!!.message)
                        Toast.makeText(this, "No user found with this email.", Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter Email", Toast.LENGTH_SHORT).show()
        }

    }
}