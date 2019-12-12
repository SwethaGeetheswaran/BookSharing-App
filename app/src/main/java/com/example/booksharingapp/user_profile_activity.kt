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
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId:String
    var addReadBooksArrayList : ArrayList<Book> = ArrayList()
    var key: String? = null
    private  var TAG = "User_profile_activity"
    lateinit var recyclerBooksAdapter: booksRead_fragment_adapter

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

        recyclerBooksAdapter= booksRead_fragment_adapter(addReadBooksArrayList,null)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        display_books_read_list.setHasFixedSize(true)
        display_books_read_list.layoutManager = linearLayoutManager
        display_books_read_list.adapter = recyclerBooksAdapter
        display_books_read_list.addItemDecoration(DividerItemDecoration(this, DividerItemDecoration.VERTICAL))


        mDatabaseReference.addValueEventListener(object :ValueEventListener{
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

        mBooksReadDbRef.child(currentUserId).addListenerForSingleValueEvent(object: ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }
            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    getKeyforFetchingBooks()
                    display_books_read_list.visibility = View.VISIBLE
                } else {
                    display_books_read_list.visibility = View.GONE
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
                    fetchBooksForUser(key!!)
                }

            }
        })
    }

    private fun fetchBooksForUser(key: String) {
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
