package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
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



class MainActivity : AppCompatActivity() {

    val RC_SIGN_IN: Int = 1
    lateinit var mGoogleSignInClient: GoogleSignInClient
    lateinit var mGoogleSignInOptions: GoogleSignInOptions
    private lateinit var firebaseAuth: FirebaseAuth

    var biometricPrompt: BiometricPrompt? = null
    val newExecutor = Executors.newSingleThreadExecutor()

    private val callback = object : BiometricPrompt.AuthenticationCallback() {
        override fun onAuthenticationError(errorCode: Int, errString: CharSequence) {
            super.onAuthenticationError(errorCode, errString)
            if (errorCode == ERROR_NEGATIVE_BUTTON && biometricPrompt != null)
                biometricPrompt!!.cancelAuthentication()
            runOnUiThread {
                Toast.makeText(this@MainActivity,errString,Toast.LENGTH_SHORT).show()
            }

        }

        override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
            super.onAuthenticationSucceeded(result)
            runOnUiThread{
                Toast.makeText(this@MainActivity,"Authentication succeed",Toast.LENGTH_SHORT).show()
            }
            startActivity(HomeActivity.getLaunchIntent(this@MainActivity))
        }

        override fun onAuthenticationFailed() {
            super.onAuthenticationFailed()
            runOnUiThread {
                Toast.makeText(
                    this@MainActivity,
                    "Application did not recognize the placed finger print. Please try again!",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.login_activity)
        configureGoogleSignIn()
        setupUI()
        firebaseAuth = FirebaseAuth.getInstance()

        if (biometricPrompt == null)
            biometricPrompt = BiometricPrompt(this, newExecutor, callback)


        fingerprint_authentication.setOnClickListener {
                Log.v("MainActivty","Fingerprint")
                val promptInfo = buildBiometricPrompt()
                biometricPrompt!!.authenticate(promptInfo)


            Log.v("MainActivty","Fingerprint-2")
        }

    }


    private fun buildBiometricPrompt(): BiometricPrompt.PromptInfo {
        return BiometricPrompt.PromptInfo.Builder()
            .setTitle("Login")
            .setSubtitle("Login into your account")
            .setDescription("Touch your finger on the finger print sensor to authorise your account.")
            .setNegativeButtonText("Cancel")
            .build()
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
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun firebaseAuthWithGoogle(acct: GoogleSignInAccount) {
        val credential = GoogleAuthProvider.getCredential(acct.idToken, null)
        firebaseAuth.signInWithCredential(credential).addOnCompleteListener {
            if (it.isSuccessful) {

                startActivity(HomeActivity.getLaunchIntent(this))
            } else {
                Toast.makeText(this, "Google sign in failed:(", Toast.LENGTH_LONG).show()
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



