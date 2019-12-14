package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import android.app.LoaderManager
import android.content.Loader

import androidx.recyclerview.widget.DefaultItemAnimator
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.SimpleTarget
import com.bumptech.glide.request.transition.Transition
import com.google.android.material.appbar.AppBarLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.post_books_appbar_layout.*
import kotlinx.android.synthetic.main.post_books_scrolling.*

//Reference : https://github.com/bhavya-arora/BookListingApp
class AddBooksActivity : AppCompatActivity(), LoaderManager.LoaderCallbacks<List<GoogleBooks>> {

    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var mAuth: FirebaseAuth
    private lateinit var currentUserID: String
    private lateinit var menu: Menu
    private lateinit var profile_image: String
    private lateinit var mUsersDbRef: DatabaseReference
    private val GoogleBookUrl = "https://www.googleapis.com/books/v1/volumes"
    private lateinit var googleBooksAdapter: GoogleBooksAdapter
    var googleBooksList: ArrayList<GoogleBooks> = ArrayList()
    private val TAG = "AddBooksActivity"
    private val BOOKS_LOADER_ID = 1
    private lateinit var valueEventListener: ValueEventListener

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.post_books_appbar_layout)
        setSupportActionBar(toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        mUsersDbRef = mDatabase.reference.child("Users").child(currentUserID)

        books_progressBar.isIndeterminate = true
        books_progressBar.visibility = View.GONE

        //Checking the Network State
        val connectivityManager =
            getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        if (networkInfo == null) {
            empty_state.setText(R.string.no_internet)
            empty_state.visibility = View.VISIBLE
            (findViewById(R.id.searchButton) as Button).isEnabled = false
        }

        collapsingtoolbar_layout.title = " "
        //Show/Hide profile image in Collapsing toolbar
        app_bar.addOnOffsetChangedListener(object : AppBarLayout.OnOffsetChangedListener {
            var isShow = false
            var scrollRange = -1
            override fun onOffsetChanged(appBarLayout: AppBarLayout?, verticalOffset: Int) {
                if (scrollRange == -1) {
                    scrollRange = appBarLayout!!.totalScrollRange
                }
                if (scrollRange + verticalOffset == 0) {
                    isShow = true
                    collapsingtoolbar_layout.setTitle(getString(R.string.add_book))
                    showOptions(R.id.user_profile_image)
                    fab.visibility = View.GONE
                } else if (isShow) {
                    isShow = false
                    collapsingtoolbar_layout.title = " "
                    hideOptions(R.id.user_profile_image)
                    fab.visibility = View.VISIBLE
                }
            }

        })


        // Add books to Adapter
        if (savedInstanceState == null || !savedInstanceState.containsKey("booksList")) {
            googleBooksList = ArrayList<GoogleBooks>()
            googleBooksAdapter = GoogleBooksAdapter(googleBooksList)
        } else {
            savedInstanceState.getParcelableArrayList<GoogleBooks>("booksList")?.let {
                googleBooksList.addAll(it)
            }
            googleBooksAdapter = GoogleBooksAdapter(googleBooksList)
            googleBooksAdapter.notifyDataSetChanged()
        }


        // Set a Grid View Layout to display books that match the given title as input.
        val mLayoutManager = GridLayoutManager(this, 2)
        recycler_view.setLayoutManager(mLayoutManager)
        recycler_view.addItemDecoration(GridSpacingItemDecoration(2, dpToPx(10), true))
        recycler_view.setItemAnimator(DefaultItemAnimator())
        recycler_view.setAdapter(googleBooksAdapter)


        // Display Profile image at the bottom of  Collapsing toolbar
        valueEventListener = mUsersDbRef.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                throw p0.toException()
            }

            override fun onDataChange(dataSnapshot: DataSnapshot) {
                if (dataSnapshot.exists()) {
                    profile_image = dataSnapshot.child("ProfileImage").value.toString()
                    Glide.with(this@AddBooksActivity).load(profile_image).into(fab)

                }
            }
        })

    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        this.menu = menu!!
        menuInflater.inflate(R.menu.add_newbook_menu, menu)
        hideOptions(R.id.user_profile_image)
        return super.onCreateOptionsMenu(menu)
    }

    fun hideOptions(id: Int) {
        val menuItem = menu.findItem(id)
        menuItem?.isVisible = false
    }

    fun showOptions(id: Int) {
        val menuItem = menu.findItem(id)
        menuItem?.isVisible = true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> startActivity(BooksCollection.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }

    // Display profile image at the top right corner of the toolbar.
    //Note: This image appears only when the toolbar collapses.
    override fun onPrepareOptionsMenu(menu: Menu?): Boolean {
        val menuItem = menu?.findItem(R.id.user_profile_image)
        Glide.with(this).asBitmap().load(profile_image)
            .apply(RequestOptions.circleCropTransform())
            .into(object : SimpleTarget<Bitmap?>(100, 100) {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap?>?
                ) {
                    menuItem?.setIcon(BitmapDrawable(resources, resource))
                }
            })
        return super.onPrepareOptionsMenu(menu)
    }


    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, AddBooksActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putParcelableArrayList("booksList", googleBooksList)
        super.onSaveInstanceState(outState)
    }

    fun searchButton(view: View?) {
        books_progressBar.visibility = View.VISIBLE
        googleBooksList.clear()
        googleBooksAdapter.notifyDataSetChanged()
        loaderManager.restartLoader(BOOKS_LOADER_ID, null, this)
        loaderManager.initLoader(BOOKS_LOADER_ID, null, this)
    }

    // Use Loader to fetch the list of books from GoogleBooks Api
    override fun onCreateLoader(id: Int, args: Bundle?): Loader<List<GoogleBooks>> {
        val query = searchBox_text.text.toString()
        if (query.isEmpty() || query.length == 0) {
            searchBox_text.error = "Please Enter Any Book"
            return GoogleBooksLoader(this, null) as Loader<List<GoogleBooks>>
        }


        val baseUri = Uri.parse(GoogleBookUrl)
        val uriBuilder = baseUri.buildUpon()

        uriBuilder.appendQueryParameter("q", query)


        //Hide Keyboard
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(
            currentFocus!!.windowToken,
            InputMethodManager.HIDE_NOT_ALWAYS
        )
        searchBox_text.setText("")

        Log.v("uriBuilder", uriBuilder.toString())
        //Returning a new Loader Object
        return GoogleBooksLoader(this, uriBuilder.toString()) as Loader<List<GoogleBooks>>
    }

    override fun onLoadFinished(loader: Loader<List<GoogleBooks>>, list: List<GoogleBooks>?) {
        books_progressBar.visibility = View.GONE
        if (list != null && !list.isEmpty()) {
            prepareBooks(list)
            Log.i(TAG, "onLoadFinished: ")
        } else {
            empty_state.setText(R.string.no_data)
            empty_state.visibility = View.VISIBLE
        }
    }

    override fun onLoaderReset(loader: Loader<List<GoogleBooks>>) {
        Log.i(QueryUtils.TAG, "onLoaderReset: ")
        googleBooksList.clear()
        googleBooksAdapter.notifyDataSetChanged()
    }

    private fun prepareBooks(booksList: List<GoogleBooks>) {
        googleBooksList.addAll(booksList)
        googleBooksAdapter.notifyDataSetChanged()
    }


    class GridSpacingItemDecoration(
        private val spanCount: Int,
        private val spacing: Int,
        private val includeEdge: Boolean
    ) :
        RecyclerView.ItemDecoration() {
        override fun getItemOffsets(
            outRect: Rect,
            view: View,
            parent: RecyclerView,
            state: RecyclerView.State
        ) {
            val position: Int = parent.getChildAdapterPosition(view)
            val column = position % spanCount
            if (includeEdge) {
                outRect.left =
                    spacing - column * spacing / spanCount
                outRect.right =
                    (column + 1) * spacing / spanCount
                if (position < spanCount) {
                    outRect.top = spacing
                }
                outRect.bottom = spacing
            } else {
                outRect.left = column * spacing / spanCount
                outRect.right =
                    spacing - (column + 1) * spacing / spanCount
                if (position >= spanCount) {
                    outRect.top = spacing
                }
            }
        }

    }

    private fun dpToPx(dp: Int): Int {
        val r = resources
        return Math.round(
            TypedValue.applyDimension(
                TypedValue.COMPLEX_UNIT_DIP,
                dp.toFloat(),
                r.displayMetrics
            )
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        mUsersDbRef.removeEventListener(valueEventListener)
    }
}