package com.example.booksharingapp

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.MenuItem
import kotlinx.android.synthetic.main.activity_user_post_activity.*
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.Menu
import android.view.View
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.squareup.picasso.Picasso
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.*


class user_post_activity : AppCompatActivity() {


    private lateinit var mUserStorageRefs : StorageReference
    private lateinit var mUserDBRef: DatabaseReference
    private lateinit var mUserPostRef: DatabaseReference
    private lateinit var mDatabase: FirebaseDatabase
    private val TAG ="user_post_activity"
    var ImageUri : Uri? = null
    lateinit var save_post_date:String
    lateinit var save_post_time:String
    lateinit var post_date_time:String
    lateinit var downloadUrl:String
    lateinit var currentUserID:String
    private lateinit var mAuth: FirebaseAuth
    val  MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE = 1

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_post_activity)
        supportActionBar?.setTitle(R.string.add_post)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)

        mDatabase = FirebaseDatabase.getInstance()
        mAuth = FirebaseAuth.getInstance()
        mUserStorageRefs = FirebaseStorage.getInstance().reference
        mUserDBRef =  mDatabase.reference.child("Users")
        mUserPostRef =  mDatabase.reference.child("Users Posts")
        currentUserID = mAuth.currentUser!!.uid

        requestRead()

        displayProfileImage()
        camera_image.setOnClickListener {
            val takePicture = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
            startActivityForResult(takePicture, 0)
        }

        gallery_image.setOnClickListener {
            val pickPhoto = Intent(
                Intent.ACTION_PICK,
                MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
            startActivityForResult(pickPhoto, 1)

        }

        checkForDefaultBackgroundImages()

    }

    private fun checkForDefaultBackgroundImages() {
        default_image1.setOnClickListener {
                Picasso.get()
                    .load(R.drawable.default1)
                    .placeholder(R.drawable.ic_profile)
                    .error(R.drawable.ic_profile_name)
                    .fit()
                    .into(user_post_image)

            ImageUri = Uri.parse("android.resource://" + this.packageName+"/drawable/default1")
        }

        default_image2.setOnClickListener {
            Picasso.get()
                .load(R.drawable.default2)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile_name)
                .fit()
                .into(user_post_image)
            ImageUri = Uri.parse("android.resource://com.example.booksharingapp/drawable/default2")
        }

        default_image3.setOnClickListener {
            Picasso.get()
                .load(R.drawable.default3)
                .placeholder(R.drawable.ic_profile)
                .error(R.drawable.ic_profile_name)
                .fit()
                .into(user_post_image)
            ImageUri = Uri.parse("android.resource://com.example.booksharingapp/drawable/default3")
        }
    }

    private fun displayProfileImage() {
        mUserDBRef.child(currentUserID).addValueEventListener(object : ValueEventListener {
            override fun onCancelled(p0: DatabaseError) {
                Log.v(TAG, "saveUserPostDataToFirebase" + p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if (p0.exists()) {
                    val user_Profile_Image = p0.child("ProfileImage").value.toString()
                    Picasso.get()
                        .load(user_Profile_Image)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(profile_image)
                }
            }
        })
    }

    // Display appropriate snackbar message when textfield or image is blank.
    fun validationForAddingPost(){
        Log.v(TAG, "post_it_button")
        if (ImageUri == null) {
            Log.v(TAG, "post_it_image")
            val snackbar = Snackbar
                .make(relative_layout, "Please select an image", Snackbar.LENGTH_LONG)
                .setAction("OK", object : View.OnClickListener {
                    override fun onClick(view: View) {}
                })
            snackbar.setActionTextColor(Color.WHITE)
            snackbar.show()
        } else if (TextUtils.isEmpty((user_post_text).text.toString())) {
            Log.v(TAG, "post_it_text")
            val snackbar = Snackbar
                .make(relative_layout, "Please enter a text", Snackbar.LENGTH_LONG)
                .setAction("OK", object : View.OnClickListener {
                    override fun onClick(view: View) {}
                })
            snackbar.setActionTextColor(Color.WHITE)
            snackbar.show()
        } else {
            Log.v(TAG, "post_it_new")
            storeUserPostsToFirebase()
        }
    }

    // Save the post's image in Firebase storage. USe current date & time to store it.
    @SuppressLint("SimpleDateFormat")
    private fun storeUserPostsToFirebase() {
        val date = Calendar.getInstance()
        val current_date = SimpleDateFormat("MMM-dd-yy")
        save_post_date = current_date.format(date.getTime())

        val current_time = SimpleDateFormat("HH:mm")
        save_post_time = current_time.format(date.getTime())

        post_date_time = save_post_date + save_post_time


        val filePath = mUserStorageRefs.child("User Posts")
            .child(ImageUri?.getLastPathSegment() + post_date_time + ".jpg")

        filePath.putFile(ImageUri!!).addOnSuccessListener {
            Log.d(TAG,"Successfully uploaded image: ${it.metadata?.path}")
            filePath.downloadUrl.addOnSuccessListener {
                downloadUrl = it.toString()
                saveUserPostDataToFirebase()
            }

        }
            .addOnFailureListener {
                Log.v(TAG, it.printStackTrace().toString())
            }
    }


    // Save the User Post information in Firebase Database.
    private fun saveUserPostDataToFirebase() {

        mUserDBRef.child(mAuth.currentUser!!.uid).addValueEventListener(object : ValueEventListener{
            override fun onCancelled(p0: DatabaseError) {
                Log.v(TAG,"saveUserPostDataToFirebase" +p0.message)
            }

            override fun onDataChange(p0: DataSnapshot) {
                if(p0.exists()){
                    val firstName = p0.child("firstName").value.toString()
                    val lastName =  p0.child("lastName").value.toString()
                    val user_full_name = firstName + " " + lastName
                    val user_Profile_Image = p0.child("ProfileImage").value.toString()

                   val user_posts_hashMap = HashMap<String,Any>()
                    user_posts_hashMap.put("UID", currentUserID)
                    user_posts_hashMap.put("fullname", user_full_name)
                    user_posts_hashMap.put("Date", save_post_date)
                    user_posts_hashMap.put("Time", save_post_time)
                    user_posts_hashMap.put("description", user_post_text.text.toString())
                    user_posts_hashMap.put("postimage", downloadUrl)
                    user_posts_hashMap.put("profileimage", user_Profile_Image)

                 mUserPostRef.child(currentUserID + post_date_time)
                     .updateChildren(user_posts_hashMap)
                     .addOnSuccessListener {
                        startActivity(HomeActivity.getLaunchIntent(this@user_post_activity))
                     }
                     .addOnFailureListener{
                         Log.v(TAG, it.printStackTrace().toString())
                     }
                }
            }

        })

    }

    fun requestRead() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(
                    Manifest.permission.READ_EXTERNAL_STORAGE,
                    Manifest.permission.WRITE_EXTERNAL_STORAGE),
                MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode : Int, permissions : Array<String>, grantResults :IntArray) {

    if (requestCode == MY_PERMISSIONS_REQUEST_READ_EXTERNAL_STORAGE) {
        if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            Log.v(TAG,"Permission granted")
        } else {
            Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
        }
        return
    }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }



    // Get the imageUri for Camera image.
   fun  getImageUri( inContext : Context, inImage : Bitmap) : Uri {
        val bytes = ByteArrayOutputStream()
        inImage.compress(Bitmap.CompressFormat.JPEG, 100, bytes)
        val path = MediaStore.Images.Media.insertImage(inContext.getContentResolver(), inImage, "Title", null)
        return Uri.parse(path)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_CANCELED) {
            when (requestCode) {
                0 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    val tempUri= data.data
                    Log.v(TAG,"camera image: " +tempUri)
                    val selectedImage = data.extras!!.get("data") as Bitmap?
                    ImageUri = getImageUri(this,selectedImage!!)
                    Log.v(TAG,"camera image-1: " +ImageUri)
                    user_post_image.setImageBitmap(selectedImage)
                }
                1 -> if (resultCode == Activity.RESULT_OK && data != null) {
                    ImageUri = data.data
                    Picasso.get()
                        .load(ImageUri)
                        .placeholder(R.drawable.ic_profile)
                        .error(R.drawable.ic_profile_name)
                        .fit()
                        .into(user_post_image)

                }
            }
        }
    }
    companion object {
        fun getLaunchIntent(from: Context) = Intent(from, user_post_activity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK)
        }
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater = menuInflater
        inflater.inflate(R.menu.add_bookpost_menu, menu)
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when(item.itemId){
            R.id.add_post -> validationForAddingPost()
            android.R.id.home -> startActivity(HomeActivity.getLaunchIntent(this))
        }
        return super.onOptionsItemSelected(item)
    }
}