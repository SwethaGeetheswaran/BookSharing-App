package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.text.TextUtils
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_message.*
import kotlinx.android.synthetic.main.message_appbar_layout.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.ArrayList

//Reference : https://www.youtube.com/watch?v=CdgC7emR6j0&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=59
class messageActivity : AppCompatActivity() {

    private var friendsUID: String? = null
    private var username: String? = null
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var curSenderUserId: String
    lateinit var save_post_date:String
    lateinit var save_post_time:String
    var messageList: ArrayList<Messages> = ArrayList()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var messageAdapter: MessageAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_message)

        //Root reference
        mDatabaseReference = FirebaseDatabase.getInstance().reference
        //Display custom Action Bar
        supportActionBar?.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM)
        supportActionBar?.setDisplayShowCustomEnabled(true)
        supportActionBar?.setCustomView(R.layout.message_appbar_layout)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        curSenderUserId = mAuth.currentUser!!.uid
        friendsUID = intent.extras?.get("friendUID").toString()
        username = intent.extras?.get("username").toString()

        messageAdapter = MessageAdapter(messageList)
        linearLayoutManager = LinearLayoutManager(this)
        message_recycler_list.setHasFixedSize(true)
        message_recycler_list.layoutManager = linearLayoutManager
        message_recycler_list.adapter = messageAdapter


        displayAppBarLayout()


        // Save the Message in Firebase Database.
        send_message_button.setOnClickListener(object : View.OnClickListener{
            override fun onClick(p0: View?) {
                if(TextUtils.isEmpty(user_message?.text.toString().trim())){
                    Toast.makeText(this@messageActivity,"Please enter a text",Toast.LENGTH_SHORT).show()
                } else {
                    // Root path
                    val msg_sender_Id = "Messages/" +  curSenderUserId + "/" + friendsUID
                    val msg_receiver_Id = "Messages/" + friendsUID + "/" + curSenderUserId

                    // Unique key generation
                    val msg_ref_key = mDatabaseReference
                                      .child("Messages")
                                      .child(curSenderUserId)
                                      .child(friendsUID!!).push()
                    val msg_ref_key_id = msg_ref_key.key

                    val date = Calendar.getInstance()
                    val current_date = SimpleDateFormat("MMM-dd-yy")
                    save_post_date = current_date.format(date.getTime())

                    val current_time = SimpleDateFormat("HH:mm")
                    save_post_time = current_time.format(date.getTime())

                    // Add sender message to hashmap and add it to "Message" root.
                    val user_msg_hashMap = HashMap<String,Any>()
                    user_msg_hashMap.put("from",curSenderUserId)
                    user_msg_hashMap.put("date", save_post_date)
                    user_msg_hashMap.put("time", save_post_time)
                    user_msg_hashMap.put("message", user_message.text.toString())
                    user_msg_hashMap.put("to",friendsUID!!)

                    val entire_user_msg_hashMap = HashMap<String,Any>()
                    entire_user_msg_hashMap.put(msg_sender_Id + "/" + msg_ref_key_id , user_msg_hashMap)
                    entire_user_msg_hashMap.put(msg_receiver_Id + "/" + msg_ref_key_id,user_msg_hashMap)


                    mDatabaseReference.updateChildren(entire_user_msg_hashMap)
                        .addOnSuccessListener {
                            user_message.setText(" ")
                            Toast.makeText(this@messageActivity,"Your text has been sent",Toast.LENGTH_SHORT).show()
                        }
                        .addOnFailureListener {
                            user_message.setText(" ")
                            it.localizedMessage
                        }
                }
            }

        })

        fetchMessages()
    }

    private fun fetchMessages() {
        mDatabaseReference.child("Messages")
                          .child(curSenderUserId)
                          .child(friendsUID!!)
                          .addChildEventListener(object:ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                if(p0.exists()){
                    val messages = p0.getValue(Messages ::class.java)
                    messageList.add(messages!!)
                    messageAdapter.notifyDataSetChanged()
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }


    fun displayAppBarLayout(){
        message_user_name_id.text = username
        mDatabaseReference.child("Users").child(friendsUID!!).addValueEventListener(object: ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
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

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, messageActivity::class.java).apply {
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