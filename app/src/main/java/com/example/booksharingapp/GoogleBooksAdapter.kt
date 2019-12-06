package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.google_books_display.view.*

class GoogleBooksAdapter(var booksList : List<GoogleBooks>) :RecyclerView.Adapter<GoogleBooksAdapter.BooksViewHolder>(){


    private lateinit var context:Context
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int):BooksViewHolder {
        context = parent.context
        return BooksViewHolder(LayoutInflater.from(parent.context).inflate(
                R.layout.google_books_display, parent, false))

    }

    override fun getItemCount(): Int {
        return  booksList.size
    }

    override fun onBindViewHolder(holder: BooksViewHolder, position: Int) {
        val selectedBook = booksList.get(position)
        holder.title.setText(selectedBook.title)
        holder.author.setText(selectedBook.author)
        Log.v("Adapter","imageUrl:" +selectedBook.imageUrl)

        Glide.with(context).load(selectedBook.imageUrl).into(holder.image)


        holder.itemView.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val book = booksList.get(position)
                val bookUrl = book.infoUrl
                val webViewIntent = Intent(p0?.context, GoogleBookWebView::class.java)
                webViewIntent.putExtra("bookUrl", bookUrl)
                p0?.context?.startActivity(webViewIntent)

            }
        })

        holder.add.setOnClickListener(object :View.OnClickListener{
            override fun onClick(p0: View?) {
                val options = arrayOf<CharSequence>("Add to Books Read Collection", "Add to Books Share Collection","Both","Cancel")

                val builder = AlertDialog.Builder(p0?.context!!)
                builder.setTitle("Choose an option to this book")

                builder.setItems(options, { dialog, item ->
                    if (options[item] == "Add to Books Read Collection") {
                        Toast.makeText(p0.context!!, "1",Toast.LENGTH_SHORT).show()

                    } else if (options[item] == "Add to Books Share Collection") {
                        Toast.makeText(p0.context!!, "2",Toast.LENGTH_SHORT).show()

                    } else if (options[item] == "Both"){
                        Toast.makeText(p0.context!!, "3",Toast.LENGTH_SHORT).show()
                    }
                    else if (options[item] == "Cancel") {
                        Toast.makeText(p0.context!!, "4",Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                    }
                })
                builder.show()
            }

        })
    }

    class BooksViewHolder(view: View) : RecyclerView.ViewHolder(view){
        val title = view.title
        val author = view.author
        val image = view.book_image
        val add = view.add_book
    }

}