package com.example.booksharingapp

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*

class BooksSharingFragment : Fragment(){


    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserId: String
    private lateinit var recyclerList: RecyclerView
    private lateinit var emptyText: TextView
    private  var TAG = "BooksShareFragment"
    var addReadBooksArrayList : ArrayList<allUsersBooksList> = ArrayList()
    var key: String? = null
    lateinit var recyclerBooksAdapter: booksSharing_fragment_adapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {

        val rootView : View = inflater.inflate(R.layout.books_read_fragment, container,false)
        mAuth = FirebaseAuth.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        mDatabaseReference = FirebaseDatabase.getInstance().reference.child("BooksShare")

        recyclerBooksAdapter= booksSharing_fragment_adapter(addReadBooksArrayList,null)
        val linearLayoutManager = LinearLayoutManager(context)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        recyclerList = rootView.findViewById<RecyclerView>(R.id.display_books_read_list)
        recyclerList.setHasFixedSize(true)
        recyclerList.layoutManager = linearLayoutManager
        recyclerList.adapter = recyclerBooksAdapter
        recyclerList.addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL)
        )



        emptyText = rootView.findViewById(R.id.empty_list)
        mDatabaseReference.child(currentUserId).addListenerForSingleValueEvent(object:
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    getKeyforFetchingBooks()
                    emptyText.setText(" ")
                    emptyText.visibility = View.GONE
                    recyclerList.visibility = View.VISIBLE
                } else {
                    emptyText.setText(R.string.no_data)
                    emptyText.visibility = View.VISIBLE
                    recyclerList.visibility = View.GONE
                }
            }

        })
        return rootView
    }

    fun getKeyforFetchingBooks() {
        mDatabaseReference.child(currentUserId).addValueEventListener(object : ValueEventListener {
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
        mDatabaseReference.child(currentUserId).child(key).addValueEventListener(object :
            ValueEventListener {
            override fun onCancelled(p0: DatabaseError) { }
            override fun onDataChange(p0: DataSnapshot) {
                val addReadBooksList = p0.getValue(allUsersBooksList::class.java)
                addReadBooksArrayList.add(addReadBooksList!!)
                recyclerBooksAdapter = booksSharing_fragment_adapter(addReadBooksArrayList,key)
                recyclerList.adapter = recyclerBooksAdapter
            }
        })

    }

}