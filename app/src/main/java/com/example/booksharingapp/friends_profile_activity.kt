package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.gms.tasks.Task
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile_activity.*
import java.text.SimpleDateFormat
import java.util.*

class friends_profile_activity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mFrdRequestRef: DatabaseReference
    private lateinit var mFriendsRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private var friendsUID: String? = null
    private var currentState: String? = "Unknown"
    private var savecurrentdate : String? = null
    private  var TAG = "friends_profile_activity"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_activity)
        supportActionBar?.setTitle(R.string.profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("Users")
        mFrdRequestRef= FirebaseDatabase.getInstance().reference.child("FriendRequest")
        mFriendsRef= FirebaseDatabase.getInstance().reference.child("Friends")


        friendsUID = intent?.getStringExtra("friendUID").toString()
        Log.v(TAG,"friendUID: " +friendsUID)

        // TO display Friend's profile.
        mDatabaseReference.child(friendsUID!!).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profile_first_name = dataSnapshot.child("firstName").value.toString()
                    val profile_last_name = dataSnapshot.child("lastName").value.toString()
                    val user_name = profile_first_name + " "  + profile_last_name
                    user_profile_name.text = user_name

                    val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(profile_image)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(user_profile)
                }
            }

        })

        mFriendsRef.child(currentUserId).child(friendsUID!!).addListenerForSingleValueEvent(object  : ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()) {
                    send_frd_req_button.setText(R.string.unfriend_request)
                    send_frd_req_button.isEnabled = true
                }
            }
        })

        decline_frd_req_button.visibility = View.INVISIBLE
        decline_frd_req_button.isEnabled = false

        // To send friend request
        if(!currentUserId.equals(friendsUID)){
            send_frd_req_button.setOnClickListener(object: View.OnClickListener{
                override fun onClick(p0: View?) {
                    send_frd_req_button.isEnabled = false
                    if(currentState.equals("Unknown")){
                        decline_frd_req_button.visibility = View.INVISIBLE
                        decline_frd_req_button.isEnabled = false
                        sendFrdRequestToThatPerson()
                    }
                    if(currentState.equals("sent")){
                        decline_frd_req_button.visibility = View.INVISIBLE
                        decline_frd_req_button.isEnabled = false
                        CancelFriendRequest(mFrdRequestRef)
                    }
                    if(currentState.equals("received")){
                        decline_frd_req_button.visibility = View.VISIBLE
                        decline_frd_req_button.isEnabled = true
                        acceptFriendRequest()
                        decline_frd_req_button.visibility = View.INVISIBLE
                        decline_frd_req_button.isEnabled = false
                    }
                    if(currentState.equals("friends")){
                        CancelFriendRequest(mFriendsRef) // Unfriend
                    }
                }

            })

            decline_frd_req_button.setOnClickListener(object : View.OnClickListener{
                override fun onClick(p0: View?) {
                    CancelFriendRequest(mFrdRequestRef)
                    decline_frd_req_button.visibility = View.INVISIBLE
                    decline_frd_req_button.isEnabled = false
                }

            })
        } else {
            send_frd_req_button.visibility = View.INVISIBLE
            decline_frd_req_button.visibility = View.INVISIBLE
            decline_frd_req_button.isEnabled = false
        }

        changeButtonTextfromSendToCancel()

    }

    private fun acceptFriendRequest() {
        val date = Calendar.getInstance()
        val current_date = SimpleDateFormat("MMM-dd-yy")
        savecurrentdate = current_date.format(date.getTime())

        mFriendsRef.child(currentUserId).child(friendsUID!!).child("date")
            .setValue(savecurrentdate).addOnCompleteListener(object : OnCompleteListener<Void>{
                override fun onComplete(task: Task<Void>) {
                    if(task.isSuccessful){
                        mFriendsRef.child(friendsUID!!).child(currentUserId).child("date")
                            .setValue(savecurrentdate).addOnCompleteListener(object: OnCompleteListener<Void>{
                                override fun onComplete(p0: Task<Void>) {
                                    mFrdRequestRef.child(currentUserId).child(friendsUID!!)
                                        .removeValue()
                                        .addOnCompleteListener(object :OnCompleteListener<Void>{
                                            override fun onComplete(task: Task<Void>) {
                                                if(task.isSuccessful){
                                                    mFrdRequestRef.child(friendsUID!!).child(currentUserId)
                                                        .removeValue()
                                                        .addOnCompleteListener(object : OnCompleteListener<Void>{
                                                            override fun onComplete(task: Task<Void>) {
                                                                currentState = "friends"
                                                                send_frd_req_button.setText(R.string.unfriend_request)
                                                                send_frd_req_button.isEnabled = true
                                                            }

                                                        })
                                                }

                                            }

                                        })

                                }

                            })
                    }
                }

            })
    }

    //Unfriend OR Cancel Friend Request
    private fun CancelFriendRequest(databaseReference: DatabaseReference) {
        databaseReference.child(currentUserId).child(friendsUID!!)
            .removeValue()
            .addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(task: Task<Void>) {
                    if(task.isSuccessful){
                        databaseReference.child(friendsUID!!).child(currentUserId)
                            .removeValue()
                            .addOnCompleteListener(object : OnCompleteListener<Void>{
                                override fun onComplete(task: Task<Void>) {
                                    currentState = "Unknown"
                                    send_frd_req_button.setText(R.string.send_frd_request)
                                    send_frd_req_button.isEnabled = true
                                }

                            })
                    }

                }

            })
    }

    // Change the text from "Send Request" to "Cancel Request" based on the request type
    private fun changeButtonTextfromSendToCancel() {
        mFrdRequestRef.child(currentUserId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.hasChild(friendsUID!!)) {
                    val request_type = dataSnapshot.child(friendsUID!!).child("request_type").value.toString()

                    if(request_type.equals("sent")){
                            send_frd_req_button.setText(R.string.cancel_request)
                            currentState = "sent"
                    } else if (request_type.equals("received")){
                            send_frd_req_button.setText(R.string.accept_request)
                            currentState = "received"
                            decline_frd_req_button.visibility = View.VISIBLE
                            decline_frd_req_button.isEnabled = true
                    }else{
                        mFriendsRef.child(currentUserId)
                            .addListenerForSingleValueEvent(object : ValueEventListener{
                                override fun onCancelled(databaseError: DatabaseError) {
                                    throw databaseError.toException()
                                }

                                override fun onDataChange(dataSnapshot: DataSnapshot) {
                                   if(dataSnapshot.hasChild(friendsUID!!)){
                                       currentState = "friends"
                                       send_frd_req_button.setText(R.string.unfriend_request)
                                       decline_frd_req_button.visibility = View.INVISIBLE
                                       decline_frd_req_button.isEnabled = false
                                   }
                                }

                            })
                    }
                }

            }

        })
    }

    private fun sendFrdRequestToThatPerson() {
        mFrdRequestRef.child(currentUserId).child(friendsUID!!)
            .child("request_type").setValue("sent")
            .addOnCompleteListener(object :OnCompleteListener<Void>{
                override fun onComplete(task: Task<Void>) {
                        if(task.isSuccessful){
                            mFrdRequestRef.child(friendsUID!!).child(currentUserId)
                                .child("request_type").setValue("received")
                                .addOnCompleteListener(object : OnCompleteListener<Void>{
                                    override fun onComplete(task: Task<Void>) {
                                        send_frd_req_button.isEnabled = true
                                        currentState = "sent"
                                        send_frd_req_button.setText(R.string.cancel_request)
                                    }

                                })
                        }

                }

            })
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, user_profile_activity::class.java).apply {
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