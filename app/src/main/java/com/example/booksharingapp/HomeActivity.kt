package com.example.booksharingapp

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.*
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.android.material.navigation.NavigationView
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.google.firebase.database.*
import com.squareup.picasso.Picasso
import kotlinx.android.synthetic.main.home_activity_screen.*
import kotlinx.android.synthetic.main.navig_drawer_header.*
import android.widget.TextView
import com.firebase.ui.database.FirebaseRecyclerOptions


//Reference : https://www.youtube.com/watch?v=Ac4TUMJqfMY&list=PLxefhmF0pcPnTQ2oyMffo6QbWtztXu1W_&index=22
class HomeActivity : AppCompatActivity() {

    private lateinit var drawer_layout: DrawerLayout
    private lateinit var drawer_toggle: ActionBarDrawerToggle
    private lateinit var navig_view: NavigationView
    private lateinit var mDatabaseReference: DatabaseReference
    private lateinit var mUserPostDBRef: DatabaseReference
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDatabase: FirebaseDatabase
    private lateinit var currentUserID: String


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.home_activity_screen)

        // slide open/close Navigation drawer
        drawer_layout = findViewById(R.id.drawer_layout) as DrawerLayout
        drawer_toggle = ActionBarDrawerToggle(this, drawer_layout, R.string.Open, R.string.Close)
        drawer_layout.addDrawerListener(drawer_toggle)
        drawer_toggle.syncState()

        mDatabase = FirebaseDatabase.getInstance()
        mDatabaseReference = mDatabase.reference.child("Users")
        mUserPostDBRef = mDatabase.reference.child("Users Posts")
        mAuth = FirebaseAuth.getInstance()
        currentUserID = mAuth.currentUser!!.uid
        supportActionBar?.setTitle(R.string.home)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setIcon(R.drawable.ic_add_post)


        all_users_post_list.setHasFixedSize(true)
        val linearLayoutManager = LinearLayoutManager(this)
        linearLayoutManager.reverseLayout = true
        linearLayoutManager.stackFromEnd = true
        all_users_post_list.layoutManager = linearLayoutManager


        // Set the user name and profile image in Navigation Header
         mDatabaseReference.child(currentUserID)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(p0: DatabaseError) {
                    throw p0.toException()
                }

                override fun onDataChange(dataSnapshot: DataSnapshot) {
                    if (dataSnapshot.exists()) {
                        if (dataSnapshot.hasChild("firstName") && dataSnapshot.hasChild("lastName")) {
                            val profile_first_name =
                                dataSnapshot.child("firstName").value.toString()
                            val profile_last_name = dataSnapshot.child("lastName").value.toString()
                            val user_name = profile_first_name + profile_last_name
                             navig_header_user_name.text = user_name
                        }

                        if (dataSnapshot.hasChild("ProfileImage")) {
                            val profile_image = dataSnapshot.child("ProfileImage").value.toString()
                            Picasso.get().load(profile_image).placeholder(R.drawable.ic_profile)
                                .into(navig_header_profile_image)
                        } else {
                            Toast.makeText(
                                this@HomeActivity,
                                "Profile does not exists.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }

            })

        // Navigation Drawer items
        navig_view = findViewById(R.id.navig_header) as NavigationView
        navig_view.setNavigationItemSelectedListener(object :
            NavigationView.OnNavigationItemSelectedListener {
            override fun onNavigationItemSelected(item: MenuItem): Boolean {
                val id = item.getItemId()
                when (id) {
                    R.id.search_books -> startActivity(searchBook_activity.getLaunchIntent(this@HomeActivity))
                    R.id.books_collection -> startActivity(BooksCollection.getLaunchIntent(this@HomeActivity))
                    R.id.settings -> startActivity(editProfileActivity.getLaunchIntent(this@HomeActivity))
                    R.id.message -> startActivity(recyclerView_message_activity.getLaunchIntent(this@HomeActivity))
                    R.id.friends -> startActivity(DisplayFriendsListActivity.getLaunchIntent(this@HomeActivity))
                    R.id.profile -> startActivity(user_profile_activity.getLaunchIntent(this@HomeActivity))
                    R.id.find_friends -> startActivity(find_friends.getLaunchIntent(this@HomeActivity))
                    R.id.signout -> signOut()
                    else -> return true
                }

                return false

            }
        })
        displayUsersPostsList()
    }


    // To display all User's Post.
    fun displayUsersPostsList() {

        val option = FirebaseRecyclerOptions.Builder<Post>()
            .setQuery(mUserPostDBRef, Post::class.java)
            .setLifecycleOwner(this)
            .build()


        val firebaseRecyclerAdapter =
            object : FirebaseRecyclerAdapter<Post, allUsersPostViewHolder>(option) {
                override fun onCreateViewHolder(
                    parent: ViewGroup,
                    viewType: Int
                ): allUsersPostViewHolder {
                    return allUsersPostViewHolder(
                        (LayoutInflater.from(parent.context)
                            .inflate(R.layout.all_users_post, parent, false))
                    )
                }

                override fun onBindViewHolder(
                    holder: allUsersPostViewHolder,
                    position: Int,
                    model: Post
                ) {
                    val placeid = getRef(position).key.toString()

                     mUserPostDBRef.child(placeid)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(p0: DataSnapshot) {
                                holder.post_date.text = model.Date
                                holder.post_time.text = model.Time
                                holder.post_text.text = model.description
                                Picasso.get().load(model.postimage).into(holder.post_image)
                            }
                        })
                     mDatabaseReference.child(model.UID!!)
                        .addValueEventListener(object : ValueEventListener {
                            override fun onCancelled(p0: DatabaseError) {}
                            override fun onDataChange(dataSnapshot: DataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    if (dataSnapshot.hasChild("firstName") && dataSnapshot.hasChild(
                                            "lastName"
                                        )
                                    ) {
                                        val profile_first_name =
                                            dataSnapshot.child("firstName").value.toString()
                                        val profile_last_name =
                                            dataSnapshot.child("lastName").value.toString()
                                        val user_name = profile_first_name + " " + profile_last_name
                                        holder.post_fullName.text = user_name
                                    }
                                    if (dataSnapshot.hasChild("ProfileImage")) {
                                        val profile_image =
                                            dataSnapshot.child("ProfileImage").value.toString()
                                        Picasso.get().load(profile_image)
                                            .placeholder(R.drawable.ic_profile)
                                            .into(holder.post_profile_image)
                                    } else {
                                        Toast.makeText(
                                            this@HomeActivity,
                                            "Profile does not exists.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                            }
                        })
                    holder.itemView.setOnClickListener(object : View.OnClickListener {
                        override fun onClick(p0: View?) {
                            val detailedUserPostActivity =
                                Intent(this@HomeActivity, Detailed_user_post_Activity::class.java)
                            detailedUserPostActivity.putExtra("placeId", placeid)
                            startActivity(detailedUserPostActivity)
                        }

                    })
                }

            }
        all_users_post_list.adapter = firebaseRecyclerAdapter
        firebaseRecyclerAdapter.startListening()
    }


    class allUsersPostViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        internal var post_fullName = itemView.findViewById<TextView>(R.id.all_user_post_name)
        internal var post_date = itemView.findViewById<TextView>(R.id.all_user_post_date)
        internal var post_time = itemView.findViewById<TextView>(R.id.all_user_post_time)
        internal var post_text = itemView.findViewById<TextView>(R.id.all_user_post_text)
        internal var post_profile_image =
            itemView.findViewById<ImageView>(R.id.all_user_post_profile_image)
        internal var post_image = itemView.findViewById<ImageView>(R.id.all_users_post_image)

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.add_bookpost_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.getItemId()) {
            R.id.add_post -> {
                startActivity(user_post_activity.getLaunchIntent(this@HomeActivity))
            }
        }
        if (drawer_toggle.onOptionsItemSelected(item))
            return true

        return super.onOptionsItemSelected(item)
    }

    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    private fun signOut() {
        startActivity(launcherScreen_activity.getLaunchIntent(this))
        FirebaseAuth.getInstance().signOut()
    }
}