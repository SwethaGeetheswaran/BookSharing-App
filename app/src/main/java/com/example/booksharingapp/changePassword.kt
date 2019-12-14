package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.activity_edit_profile.*
import kotlinx.android.synthetic.main.change_password.*
import java.util.HashMap
import java.util.regex.Matcher
import java.util.regex.Pattern

class changePassword : AppCompatActivity() {

    private val TAG = "changePassword"
    private var userPassword: String? = null
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var currentUserID: String
    private lateinit var mAuth: FirebaseAuth
    private lateinit var getCurrentPasswordInput: String
    private lateinit var mUsersDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.change_password)
        supportActionBar?.setTitle(R.string.change_password)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        mDatabase = FirebaseDatabase.getInstance()
        userPassword = intent?.getStringExtra("password").toString()
        currentUserID = mAuth.currentUser!!.uid
        mUsersDbRef = mDatabase.reference.child("Users").child(currentUserID)


        // To carry out the password validations before updating the new password
        save_password_button.setOnClickListener {
            getCurrentPasswordInput = current_password_id.text.toString()
            Log.v(TAG, "currentPass:" + getCurrentPasswordInput)
            if (TextUtils.isEmpty(getCurrentPasswordInput)) {
                Toast.makeText(this, "Please enter your current password", Toast.LENGTH_SHORT)
                    .show()
            } else if (!userPassword.equals(getCurrentPasswordInput)) {
                Toast.makeText(
                    this,
                    "Mis-match between your current passwords!. Please re-try again.",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                val newPassword = new_password_id.text.toString()
                Log.v(TAG, "newPass:" + newPassword)
                val confirmNewPassword = new_confirm_password.text.toString()
                Log.v(TAG, "confirmPass:" + confirmNewPassword)
                if (!TextUtils.isEmpty(newPassword) || !TextUtils.isEmpty(confirmNewPassword)) {
                    if ((newPassword.length < 8 || confirmNewPassword.length < 8) &&
                        (!isValidPassword(newPassword) || !isValidPassword(confirmNewPassword))
                    ) {
                        Toast.makeText(
                            this,
                            "Password length is too short. Please enter a minimum of 8 characters.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else if (newPassword.equals(confirmNewPassword)) {
                        re_authenticate()
                        updateFirebaseDatabase()
                    }
                }

            }

        }

        cancel_password_button.setOnClickListener {
            startActivity(editProfileActivity.getLaunchIntent(this))
        }

    }

    // Update the new password to Firebase
    private fun updateFirebaseDatabase() {
        val userinfo_updates_hashMap = HashMap<String, Any>()
        userinfo_updates_hashMap.put("password", new_confirm_password.text.toString())

        mUsersDbRef.updateChildren(userinfo_updates_hashMap)
            .addOnSuccessListener {
                Log.v(TAG, "Updated in Firebase database successfully")
            }
            .addOnFailureListener {
                Log.v(TAG, it.printStackTrace().toString())
            }
    }

    // Re-authenticate the user after successful update.
    private fun re_authenticate() {
        Log.v(TAG, "re-authenticate")
        val user = FirebaseAuth.getInstance().currentUser
        val authentication = EmailAuthProvider.getCredential(user?.email!!, getCurrentPasswordInput)

        user.reauthenticate(authentication)
            .addOnCompleteListener(object : OnCompleteListener<Void> {
                override fun onComplete(task: Task<Void>) {
                    if (task.isSuccessful) {
                        user.updatePassword(new_confirm_password.text.toString())
                            .addOnCompleteListener(object : OnCompleteListener<Void> {
                                override fun onComplete(task: Task<Void>) {
                                    if (task.isSuccessful) {
                                        Toast.makeText(
                                            this@changePassword,
                                            "Password Updated successfully",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    } else {
                                        Toast.makeText(
                                            this@changePassword,
                                            "Error password not updated. Try again sometime later",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                            })

                    } else {
                        Log.d(TAG, "Error authentication failed")
                    }
                }

            })
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

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, changePassword::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> startActivity(editProfileActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}
