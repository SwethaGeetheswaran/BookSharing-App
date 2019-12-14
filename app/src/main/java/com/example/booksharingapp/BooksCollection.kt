package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import kotlinx.android.synthetic.main.books_tablayout.*

class BooksCollection : AppCompatActivity() {

    private lateinit var mBooksReadDbRef: DatabaseReference
    private lateinit var mBooksShareDbRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private var TAG = "BooksCollection"
    private var bookTitle: String? = null
    private var bookAuthor: String? = null
    private var bookImage: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.books_tablayout)
        supportActionBar?.setTitle(R.string.book)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mBooksReadDbRef = FirebaseDatabase.getInstance().reference.child("BooksRead")
        mBooksShareDbRef = FirebaseDatabase.getInstance().reference.child("BooksShare")

        // Get the option value through intent from GoogleBooksAdapter class.
        // 1 -> Add books to Books Read Collection
        // 2 -> Add books to Books Share Collection
        // 3 -> Both
        val option = intent?.getStringExtra("option").toString()
        if (option.equals("1")) {
            getIntentValuesForBooksRead()
        } else if (option.equals("2")) {
            getIntentValuesForBooksShare()
        } else {
            getIntentValuesForBooksRead()
            getIntentValuesForBooksShare()
        }

        val fragmentAdapter = ViewPagerAdapter(supportFragmentManager)
        view_pager.adapter = fragmentAdapter
        tab_layout.setupWithViewPager(view_pager)
    }

    private fun getIntentValuesForBooksRead() {
        bookTitle = intent?.getStringExtra("title").toString()
        bookAuthor = intent?.getStringExtra("author").toString()
        bookImage = intent?.getStringExtra("bookImage").toString()
        if (intent != null && intent.extras != null) {
            addReadBooksToFirebase()
        } else {
            Log.v(TAG, "All empty-1")
        }
    }

    private fun getIntentValuesForBooksShare() {
        bookTitle = intent?.getStringExtra("title").toString()
        bookAuthor = intent?.getStringExtra("author").toString()
        bookImage = intent?.getStringExtra("bookImage").toString()
        if (intent != null && intent.extras != null) {
            addShareBooksToFirebase()
        } else {
            Log.v(TAG, "All empty-2")
        }
    }

    private fun addReadBooksToFirebase() {
        val currentUserId = mBooksReadDbRef.child(currentUserId).push()
        currentUserId.child("title").setValue(bookTitle)
        currentUserId.child("Author").setValue(bookAuthor)
        currentUserId.child("bookImage").setValue(bookImage)
    }

    private fun addShareBooksToFirebase() {
        val currentUserId = mBooksShareDbRef.child(currentUserId).push()
        currentUserId.child("title").setValue(bookTitle)
        currentUserId.child("Author").setValue(bookAuthor)
        currentUserId.child("bookImage").setValue(bookImage)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, BooksCollection::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.add_bookpost_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.add_post -> {
                startActivity(AddBooksActivity.getLaunchIntent(this))
                Log.v("BooksCollection", "BookPost")
            }
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

}