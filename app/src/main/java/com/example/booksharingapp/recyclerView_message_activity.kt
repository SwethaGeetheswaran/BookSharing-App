package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_recycler_view_message_activity.*
import kotlinx.android.synthetic.main.message_appbar_layout.*

class recyclerView_message_activity : AppCompatActivity() {

    private val TAG = "recyclerView_message"
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var curSenderUserId: String
    var messageList: ArrayList<Messages> = ArrayList()
    lateinit var recyclerMessageAdapter: recycler_message_adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_recycler_view_message_activity)

        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.message_appbar_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        curSenderUserId = mAuth.currentUser!!.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference

        recyclerMessageAdapter = recycler_message_adapter(messageList)
        lateinit var linearLayoutManager: LinearLayoutManager
        linearLayoutManager = LinearLayoutManager(this)
        recycler_message_list.setHasFixedSize(true)
        recycler_message_list.layoutManager = linearLayoutManager
        recycler_message_list.adapter = recyclerMessageAdapter
        recycler_message_list.addItemDecoration(DividerItemDecoration(this,DividerItemDecoration.VERTICAL))

        displayAppBarLayout()
        getKeyforFetchingMessages()
    }

    fun displayAppBarLayout(){
        mDatabaseReference.child("Users").child(curSenderUserId).addValueEventListener(object:
            ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val profile_first_name = dataSnapshot.child("firstName").value.toString()
                val profile_last_name = dataSnapshot.child("lastName").value.toString()
                val user_name = profile_first_name + "" + profile_last_name
                val app_bar_username = user_name + "'s Messages"
                message_user_name_id.text = app_bar_username

                val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                Picasso.get()
                    .load(profile_image)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile_name)
                    .fit()
                    .into(message_user_image_id)
            }

        })
    }

    fun getKeyforFetchingMessages() {
        mDatabaseReference.child("Messages")
                          .child(curSenderUserId)
                          .addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
                }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val children = dataSnapshot.children
                println("count: "+dataSnapshot.children.count().toString())
                children.forEach {
                    val key = it.key
                    Log.v(TAG,"key: " + key)
                    fetchMessages(key!!)
                }

            }
        })
    }


     private fun fetchMessages(key : String){
         Log.v(TAG,"key-1: " + key)
         mDatabaseReference.child("Messages").child(curSenderUserId).child(key)
             .orderByKey().limitToLast(1)
             .addChildEventListener(object:ChildEventListener{
                 override fun onCancelled(p0: DatabaseError) {
                     throw p0.toException()
                 }

                 override fun onChildMoved(p0: DataSnapshot, p1: String?) {
                 }

                 override fun onChildChanged(p0: DataSnapshot, p1: String?) {
                     val messages = p0.getValue(Messages ::class.java)
                     messageList.add(messages!!)
                 }

                 override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                     if(p0.exists()){
                         val text = p0.child("message").value.toString()
                         Log.v(TAG,"fromId: " +text)
                         val messages = p0.getValue(Messages ::class.java)
                         messageList.add(messages!!)
                         recyclerMessageAdapter.notifyDataSetChanged()
                     }
                 }

                 override fun onChildRemoved(p0: DataSnapshot) {
                 }

             })
            }
    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, recyclerView_message_activity::class.java).apply {
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
