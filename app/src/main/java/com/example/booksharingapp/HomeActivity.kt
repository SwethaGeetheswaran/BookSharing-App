package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.navig_drawer_header.*

class HomeActivity : AppCompatActivity() {

    private lateinit var drawer_layout: DrawerLayout
    private lateinit var drawer_toggle: ActionBarDrawerToggle
    private lateinit var navig_view: NavigationView
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var currentUserID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity_screen)
        drawer_layout = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer_toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.Open, R.string.Close)

        drawer_layout.addDrawerListener(drawer_toggle)
        drawer_toggle.syncState()

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        supportActionBar?.setTitle(R.string.home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        mDatabaseReference.child(currentUserID).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {

            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()) {
                    if (dataSnapshot.hasChild("firstName") && dataSnapshot.hasChild("lastName")) {
                        val profile_first_name = dataSnapshot.child("firstName").value.toString()
                        val profile_last_name = dataSnapshot.child("lastName").value.toString()
                        val user_name = profile_first_name + " " + profile_last_name
                        navig_header_user_name.text = user_name
                    }

                    if (dataSnapshot.hasChild("ProfileImage")) {
                        val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                        Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile)
                            .into(navig_header_profile_image)
                    }else{
                        Toast.makeText(this@HomeActivity,"Profile does not exists.",Toast.LENGTH_SHORT).show()
                    }
                }
            }

        })
        navig_view = findViewById(R.id.navig_header) as NavigationView
        navig_view.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val id = item.getItemId()
                when (id) {
                    R.id.profile -> startActivity(user_profile_activity.getLaunchIntent(this@HomeActivity))
                    R.id.signout -> signOut()
                    else -> return true
                }

                return false

            }
        })

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
       if(drawer_toggle.onOptionsItemSelected(item)) return true

        return super.onOptionsItemSelected(item)
    }
    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    private fun signOut() {
        startActivity(MainActivity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut()
    }
}