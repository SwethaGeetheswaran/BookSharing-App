package com.example.booksharingapp

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso


class booksSharing_fragment_adapter(var addbooksList : List<allUsersBooksList>, var key : String?) : RecyclerView.Adapter<booksSharing_fragment_adapter.BooksViewHolder>() {

    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var currentUserId: String
    private val TAG = "booksShare_frag_adapter"

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BooksViewHolder {
        mAuth = FirebaseAuth.getInstance()
        return BooksViewHolder(LayoutInflater.from(parent.context).inflate(R.layout.display_books_fragment,parent,false))
    }

    override fun getItemCount(): Int {
       // Log.v(TAG, "size:" +addbooksList.size)
        return addbooksList.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        mDatabase = FirebaseDatabase.getInstance()
        currentUserId = mAuth.currentUser!!.uid
        val booksList = addbooksList.get(position)

        mDatabaseReference = mDatabase.reference.child("BooksShare").child(currentUserId).child(key!!)
        mDatabaseReference.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(databaseError: DatabaseError) {
                throw databaseError.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if(dataSnapshot.exists()){
                   // Log.v(TAG, "title:" +booksList.title)

                    holder.book_title.setText(booksList.title)
                    holder.book_author.setText(booksList.Author)
                    Picasso.get()
                        .load(booksList.bookImage)
                        .placeholder(R.drawable.book)
                        .into(holder.book_image)
                }
            }

        })
    }


    class BooksViewHolder(view: View) : RecyclerView.ViewHolder(view){
        internal var book_title = itemView.findViewById<TextView>(R.id.book_title)
        internal var book_author = itemView.findViewById<TextView>(R.id.book_author)
        internal var book_image = itemView.findViewById<ImageView>(R.id.search_book_image)
    }

}