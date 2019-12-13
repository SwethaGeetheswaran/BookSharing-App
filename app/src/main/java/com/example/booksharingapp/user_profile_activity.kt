package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.activity_user_profile_activity.*
import android.view.MenuItem
import android.view.View
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager


class user_profile_activity : AppCompatActivity() {

    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mBooksReadDbRef: DatabaseReference
    private lateinit var mFriendsDbRef: DatabaseReference
    private lateinit var mBooksShareDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId:String
    var addReadBooksArrayList : ArrayList<Book> = ArrayList()
    var addBooksShareArrayList : ArrayList<Book> = ArrayList()
    var key: String? = null
    private  var TAG = "User_profile_activity"
    lateinit var recyclerBooksAdapter: booksRead_fragment_adapter
    lateinit var recyclerBooksShareAdapter: booksSharing_fragment_adapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile_activity)
        supportActionBar?.setTitle(R.string.profile)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        send_frd_req_button.visibility = View.INVISIBLE
        decline_frd_req_button.visibility = View.INVISIBLE
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mDatabaseReference= FirebaseDatabase.getInstance().reference.child("Users").child(currentUserId)
        mBooksReadDbRef = FirebaseDatabase.getInstance().reference.child("BooksRead")
        mBooksShareDbRef = FirebaseDatabase.getInstance().reference.child("BooksShare").child(currentUserId)
        mFriendsDbRef= FirebaseDatabase.getInstance().reference.child("Friends").child(currentUserId)

        recyclerBooksAdapter= booksRead_fragment_adapter(addReadBooksArrayList,null)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        display_books_read_list.setHasFixedSize(true)
        display_books_read_list.layoutManager = linearLayoutManager
        display_books_read_list.adapter = recyclerBooksAdapter
        val dividerItemDecoration = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration.setDrawable(baseContext.getDrawable(R.drawable.recycler_decorator)!!)
        display_books_read_list.addItemDecoration(dividerItemDecoration)

        recyclerBooksShareAdapter = booksSharing_fragment_adapter(addBooksShareArrayList,null)
        val linearLayoutManager1 = LinearLayoutManager(this)
        linearLayoutManager1.reverseLayout = true
        linearLayoutManager1.stackFromEnd = true
        display_books_share_list.setHasFixedSize(true)
        display_books_share_list.layoutManager = linearLayoutManager1
        display_books_share_list.adapter = recyclerBooksShareAdapter
        val dividerItemDecoration1 = DividerItemDecoration(this, DividerItemDecoration.VERTICAL)
        dividerItemDecoration1.setDrawable(baseContext.getDrawable(R.drawable.recycler_decorator)!!)
        display_books_share_list.addItemDecoration(dividerItemDecoration1)

        mDatabaseReference.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw  p0.toException()}
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                    val profile_first_name = dataSnapshot.child("firstName").value.toString()
                    val profile_last_name = dataSnapshot.child("lastName").value.toString()
                    val user_name = profile_first_name + " "  + profile_last_name
                    user_profile_name.text = user_name

                    val aboutMyself = dataSnapshot.child("about_myself").value.toString()
                    if(aboutMyself.isNullOrEmpty()){
                        Log.v(TAG,"aboutMyself:" +aboutMyself)
                        about_myself.setText(" ")
                    } else {
                        Log.v(TAG,"aboutMyself-2:" +aboutMyself)
                        about_myself.setText(aboutMyself)
                    }

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

        mBooksReadDbRef.child(currentUserId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val booksReadCount = p0.childrenCount
                    books_read_count.setText(booksReadCount.toString())
                    getKeyforFetchingBooks()
                    display_books_read_list.visibility = View.VISIBLE
                } else {
                    display_books_read_list.visibility = View.GONE
                }
            }

        })

        mFriendsDbRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val frds_count = p0.childrenCount
                    Log.v(TAG,"frdCount: " +frds_count)
                    friends_count.setText(frds_count.toString())
                    getKeyforFetchingSharedBooks()
                    display_books_share_list.visibility = View.VISIBLE
                } else {
                    display_books_share_list.visibility = View.GONE
                }
            }

        })

        mBooksShareDbRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val booksShareCount = p0.childrenCount
                    Log.v(TAG,"booksShareCount: " +booksShareCount)
                    books_share_count.setText(booksShareCount.toString())
                }
            }

        })
    }

    fun getKeyforFetchingBooks() {
        mBooksReadDbRef.child(currentUserId).addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val children = dataSnapshot.children
                println("count: "+dataSnapshot.children.count().toString())
                children.forEach {
                    key = it.key
                    Log.v(TAG,"key: " + key)
                    fetchUsersForBooksRead(key!!)
                }

            }
        })
    }

    private fun fetchUsersForBooksRead(key: String) {
        mBooksReadDbRef.child(currentUserId).child(key).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(p0: DataSnapshot) {
                val addReadBooksList = p0.getValue(Book::class.java)
                addReadBooksArrayList.add(addReadBooksList!!)
                recyclerBooksAdapter = booksRead_fragment_adapter(addReadBooksArrayList,key)
                display_books_read_list.adapter = recyclerBooksAdapter
            }
        })

    }

    fun getKeyforFetchingSharedBooks() {
        mBooksShareDbRef.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val children = dataSnapshot.children
                println("count: "+dataSnapshot.children.count().toString())
                children.forEach {
                    key = it.key
                    Log.v(TAG,"key: " + key)
                    fetchUsersForBooksShare(key!!)
                }

            }
        })
    }

    private fun fetchUsersForBooksShare(key: String) {
        mBooksShareDbRef.child(key).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(p0: DataSnapshot) {
                val addBooksShare = p0.getValue(Book::class.java)
                addBooksShareArrayList.add(addBooksShare!!)
                recyclerBooksShareAdapter = booksSharing_fragment_adapter(addBooksShareArrayList,key)
                display_books_share_list.adapter = recyclerBooksShareAdapter
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
