package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.text.method.HideReturnsTransformationMethod
import android.text.method.PasswordTransformationMethod
import android.util.Log
import android.widget.CompoundButton
import android.widget.ProgressBar
import android.widget.Toast
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.android.synthetic.main.login_activity.*
import androidx.biometric.BiometricPrompt
import java.util.concurrent.Executors
import androidx.biometric.BiometricConstants.ERROR_NEGATIVE_BUTTON
import androidx.core.view.isVisible


class MainActivity : AppCompatActivity() {

    private val TAG = "MainActivity"
    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth
    private var mProgressBar: ProgressBar? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        configureGoogleSignIn()
        setupUI()
        firebaseAuth = FirebaseAuth.getInstance()

        mProgressBar = ProgressBar(this)
        intializeForgotPassword()
        initializeForCreateAccount()
        login_button.setOnClickListener { loginWithEmailandPassword() }

        checkbox.setOnCheckedChangeListener(object: CompoundButton.OnCheckedChangeListener {
            override fun onCheckedChanged(compoundButton : CompoundButton, isChecked : Boolean) {
                if (isChecked) {
                    // hide password
                    checkbox.text = "Hide password"
                    login_password.setTransformationMethod(HideReturnsTransformationMethod.getInstance())
                } else {
                    // show password
                    checkbox.text = "Show password"
                    login_password.setTransformationMethod(PasswordTransformationMethod.getInstance())
                }
            }
        })

    }


    private fun initializeForCreateAccount(){
        create_account_button.setOnClickListener {
            startActivity(createAccount_Activity.getLaunchIntent(this))
        }

    }

    private fun intializeForgotPassword(){
        forgot_password.setOnClickListener {
            startActivity(Intent(this,Forgot_password_activity::class.java))
        }
    }



    private fun loginWithEmailandPassword(){
        val email = login_email?.text.toString()
        val password = login_password?.text.toString()
        if (!TextUtils.isEmpty(email) && !TextUtils.isEmpty(password)) {
            //mProgressBar!!.text
            mProgressBar!!.isVisible = true
            Log.d(TAG, "Logging in user.")
            firebaseAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    mProgressBar!!.isVisible = false
                    if (task.isSuccessful) {
                        Log.d(TAG, "signInWithEmail:success")
                        startActivity(Intent(this,HomeActivity::class.java))
                    } else {
                        Log.e(TAG, "signInWithEmail:failure", task.exception)
                        Toast.makeText(this, "Authentication failed.",
                            Toast.LENGTH_SHORT).show()
                    }
                }
        } else {
            Toast.makeText(this, "Enter all details", Toast.LENGTH_SHORT).show()
        }
    }

    private fun configureGoogleSignIn() {
        mGoogleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        mGoogleSignInClient = GoogleSignIn.getClient(this, mGoogleSignInOptions)
    }

    private fun setupUI() {
        google_button.setOnClickListener {
            signIn()
        }
    }

    private fun signIn() {
        val signInIntent: Intent = mGoogleSignInClient.signInIntent
        startActivityForResult(signInIntent, RC_SIGN_IN)
    }


    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == RC_SIGN_IN) {
            val task: Task<GoogleSignInAccount> = GoogleSignIn.getSignedInAccountFromIntent(data)
            try {
                val account = task.getResult(ApiException::class.java)
                firebaseAuthWithGoogle(account!!)
            } catch (e: ApiException) {
                Toast.makeText(this, "Google sign in failed.Please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {
                Log.v(TAG,"email:" +acct.email)
                Log.v(TAG,"displayName:" +acct.displayName)
                val googleSignInIntent = Intent(this,editProfileActivity::class.java)
                googleSignInIntent.putExtra("email",acct.email)
                startActivity(googleSignInIntent)
            } else {
                Toast.makeText(this, "Google sign in failed. Please try again", Toast.LENGTH_LONG).show()
            }
        }
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, MainActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onStart() {
        super.onStart()
        val user = FirebaseAuth.getInstance().currentUser
        if (user != null) {
            startActivity(HomeActivity.getLaunchIntent(this))
            finish()
        }
    }
}



