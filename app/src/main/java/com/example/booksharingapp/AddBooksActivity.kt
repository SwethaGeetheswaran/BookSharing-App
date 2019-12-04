package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.ArrayAdapter
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.appbar.AppBarLayout
import kotlinx.android.synthetic.main.post_books_appbar_layout.*
import kotlinx.android.synthetic.main.post_books_scrolling.*

class AddBooksActivity :AppCompatActivity(){

    private lateinit var menu: Menu
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_books_appbar_layout)
        setSupportActionBar(toolbar)
        supportActionBar?.setTitle(R.string.add_book)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener{
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if(scrollRange == -1){
                    scrollRange = appBarLayout!!.totalScrollRange
                }
                if(scrollRange + verticalOffset == 0) {
                    isShow = true
                    showOptions(R.id.user_profile_image)
                }
                else if(isShow) {
                    isShow = false
                   hideOptions(R.id.user_profile_image)
                }
            }

        })

        ArrayAdapter.createFromResource(this, R.array.user_book_choice_list, android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spinner.adapter = adapter
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu!!
        menuInflater.inflate(R.menu.add_newbook_menu,menu)
        hideOptions(R.id.user_profile_image)
        return super.onCreateOptionsMenu(menu)
    }

    fun hideOptions(id:Int){
        val menuItem = menu.findItem(id)
        menuItem?.isVisible = false
    }

    fun showOptions(id:Int){
        val menuItem = menu.findItem(id)
        menuItem?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            android.R.id.home -> startActivity(BooksCollection.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, AddBooksActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }
}