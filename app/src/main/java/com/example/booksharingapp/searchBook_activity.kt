package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import kotlinx.android.synthetic.main.recycler_search_book.*

class searchBook_activity : AppCompatActivity() {

    private var bookName:String? = null
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mBooksShareDB: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private val TAG = "searchBook_activity"
    var titleMatchUsers : ArrayList<String> = ArrayList()
    lateinit var linearLayoutManager: LinearLayoutManager
    lateinit var adapter : searchBook_adapter
    private lateinit var currentUserID:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.recycler_search_book)
        supportActionBar?.setTitle(R.string.search_books_navig)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        mBooksShareDB = mDatabase.reference.child("BooksShare")

        adapter = searchBook_adapter(titleMatchUsers)
        linearLayoutManager = LinearLayoutManager(this)
        recycler_books_list.setHasFixedSize(true)
        adapter.setHasStableIds(true)
        recycler_books_list.layoutManager = linearLayoutManager
        recycler_books_list.adapter = adapter

        search_book_button.setOnClickListener {
            bookName = search_book_name.text.toString()
            if(bookName == null){ Log.v(TAG, "No book name:") }
            else {
                titleMatchUsers.clear()
                findBookinFirebase() }
        }
    }

    private fun findBookinFirebase() {

        mBooksShareDB.addValueEventListener(object :ValueEventListener{
            override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
            override fun onDataChange(dataSnapshot: DataSnapshot) {
                val children = dataSnapshot.children
                println("count: "+dataSnapshot.children.count().toString())
                children.forEach {
                    val key = it.key
                    Log.v(TAG,"key: " + key)
                    findUsersthatMatch(key!!)
                }
            }
        })
    }

    fun findUsersthatMatch(key: String)
    {
        if(!key.equals(currentUserID))
        {
            mBooksShareDB.child(key).orderByChild("title").equalTo(bookName)
                .addListenerForSingleValueEvent(object : ValueEventListener
                {
                    override fun onCancelled(p0: DatabaseError) { throw p0.toException() }
                    override fun onDataChange(dataSnapshot: DataSnapshot)
                    {
                        Log.v(TAG, "bookname: " + bookName)
                        for (ds in dataSnapshot.getChildren()) {
                            val userId = dataSnapshot.key
                            titleMatchUsers.add(userId!!)
                            Log.v(TAG, "key-1: " + userId)
                        }
                        Log.v(TAG,"list: " +titleMatchUsers.size)
                        adapter.notifyDataSetChanged()
                    }

                })
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, searchBook_activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}