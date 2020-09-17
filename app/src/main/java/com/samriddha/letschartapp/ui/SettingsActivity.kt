package com.samriddha.letschartapp.ui

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_ABOUT
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_DP
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_NAME
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_UID
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.others.Constants.PICK_IMAGE_FROM_GALLERY
import com.samriddha.letschartapp.others.Constants.USER_DP_STORAGE_REF_PATH
import com.theartofdev.edmodo.cropper.CropImage
import com.theartofdev.edmodo.cropper.CropImageView
import kotlinx.android.synthetic.main.activity_settings.*


class SettingsActivity : AppCompatActivity() {

    private var firebaseAuth:FirebaseAuth? = null
    private var currentUserId:String? = null
    private var firebaseDbRef:DatabaseReference? = null
    private var firebaseStorageProfileDpRef:StorageReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        setSupportActionBar(settingsToolbar)
        supportActionBar?.title = "Account Settings"

        firebaseAuth = FirebaseAuth.getInstance()
        currentUserId = firebaseAuth?.currentUser?.uid
        firebaseDbRef = FirebaseDatabase.getInstance().reference
        firebaseStorageProfileDpRef = FirebaseStorage.getInstance().reference.child(USER_DP_STORAGE_REF_PATH)

        getUserCurrentInfo()

        btnSettingsUpdate.setOnClickListener {
            updateUserData()
        }

        btnChangeDp.setOnClickListener {

            //starting gallery intent for picking image
            val galleryIntent = Intent()
            galleryIntent.action = Intent.ACTION_GET_CONTENT
            galleryIntent.type = "image/*"
            startActivityForResult(galleryIntent, PICK_IMAGE_FROM_GALLERY)

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        // When user has selected an image from gallery this part will be executed
        if(requestCode == PICK_IMAGE_FROM_GALLERY
            && resultCode == Activity.RESULT_OK
            && data != null)
        {
            val imageUri = data.data

            //Starting the crop image activity for cropping the selected image using theartofdev image cropper library
            CropImage
                .activity(imageUri)
                .setGuidelines(CropImageView.Guidelines.ON)
                .setAspectRatio(1,1)
                .start(this)
        }


        //When crop button is pressed in crop mode,this part will be executed
        if (requestCode == CropImage.CROP_IMAGE_ACTIVITY_REQUEST_CODE)
        {
            val result = CropImage.getActivityResult(data)

            if (resultCode == Activity.RESULT_OK){

                val imageUri = result.uri //This has the cropped image inside

                saveImageInFbStorageAndDatabase(imageUri)
            }
        }


    }

    private fun saveImageInFbStorageAndDatabase(imageUri: Uri) {

        //showing progress bar while image is uploading...
        settingsProgressBar.visibility = View.VISIBLE
        window.setFlags(
            WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

        //Saving the cropped image inside firebase storage with userId.jpg. Firebase Storage-> USER_DP_STORAGE_REF_PATH-> userId.jpg
        val filePath = firebaseStorageProfileDpRef?.child("$currentUserId.jpg")

        filePath
            ?.putFile(imageUri)
            ?.addOnSuccessListener {

                /*Image is added to firebaseStorage successfully,we get the downloadUrl of that image
                * and save it inside fbDatabase under current user profile*/
                filePath
                    .downloadUrl
                    .addOnSuccessListener {

                        //getting download link of the uploaded image from firebase storage.
                        val downloadUrl = it.toString()

                        //saving image url link inside firebase database inside current user.
                        //FirebaseDatabase->All_User->CurrentUserId->profile_pic
                        firebaseDbRef
                            ?.child(DATABASE_PATH_NAME_ALL_USERS)
                            ?.child(currentUserId!!)
                            ?.child(ALL_USER_KEY_USER_DP)
                            ?.setValue(downloadUrl)
                            ?.addOnSuccessListener {
                                Toast.makeText(this,"Profile Image Updated",Toast.LENGTH_SHORT).show()
                                settingsProgressBar.visibility = View.INVISIBLE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            }
                            ?.addOnFailureListener {

                                //If adding url to database is not successful
                                Toast.makeText(this,"Database Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                                settingsProgressBar.visibility = View.INVISIBLE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            }
                    }
                    .addOnFailureListener {

                        //If downloading url from firebaseStorage is not successful.
                        Toast.makeText(this,"Image Url Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                        settingsProgressBar.visibility = View.INVISIBLE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                    }

            }
            ?.addOnFailureListener {

                //If saving image inside firebaseStorage is not successful
                Toast.makeText(this,"Storage Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                settingsProgressBar.visibility = View.INVISIBLE
                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
            }

    }

    private fun getUserCurrentInfo() {

        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(currentUserId!!)
            ?.addValueEventListener(object : ValueEventListener {

                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists()
                        && snapshot.hasChild(ALL_USER_KEY_USER_NAME)
                        && snapshot.hasChild(ALL_USER_KEY_USER_DP)
                    ) {

                        val oldUserName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()
                        val oldUserAbout = snapshot.child(ALL_USER_KEY_USER_ABOUT).value.toString()
                        val oldUserDp = snapshot.child(ALL_USER_KEY_USER_DP).value.toString()

                        settingsUserName.setText(oldUserName)
                        settingsAbout.setText(oldUserAbout)

                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        /*We are using application context because if the user hits crop and presses back button,then we don't have
                        * the activity context to load image with.And this will lead to an illegal argument exception.*/
                        Glide
                            .with(applicationContext)
                            .load(oldUserDp)
                            .apply(glideCustomization)
                            .into(profile_image)

                    }else if(snapshot.exists()
                        && !snapshot.hasChild(ALL_USER_KEY_USER_NAME)
                        && snapshot.hasChild(ALL_USER_KEY_USER_DP)){

                        val oldUserDp = snapshot.child(ALL_USER_KEY_USER_DP).value.toString()
                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        Glide
                            .with(applicationContext)
                            .load(oldUserDp)
                            .apply(glideCustomization)
                            .into(profile_image)

                    }
                    else if (snapshot.exists()
                        && snapshot.hasChild(ALL_USER_KEY_USER_NAME)
                    ) {
                        val oldUserName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()
                        val oldUserAbout = snapshot.child(ALL_USER_KEY_USER_ABOUT).value.toString()

                        settingsUserName.setText(oldUserName)
                        settingsAbout.setText(oldUserAbout)

                    }else{
                        Toast.makeText(baseContext,"Please Update Your Profile",Toast.LENGTH_SHORT).show()
                    }


                }

            })


    }

    private fun updateUserData() {

        val userName = settingsUserName.text.toString()
        var userAbout = settingsAbout.text.toString()

        if (userName.isEmpty()) {
            Toast.makeText(this, "Please Enter A User Name", Toast.LENGTH_SHORT).show()
            return
        }

        //If About section is left empty the we save the default about message in the database.
        if (userAbout.isEmpty())
            userAbout = "Hey I'm Using LetsChat App!!"

        val profileMap = HashMap<String, Any>()
        profileMap[ALL_USER_KEY_USER_UID] = currentUserId!!
        profileMap[ALL_USER_KEY_USER_NAME] = userName
        profileMap[ALL_USER_KEY_USER_ABOUT] = userAbout

        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(currentUserId!!)
            ?.updateChildren(profileMap)
            ?.addOnSuccessListener {
                Toast.makeText(this, "Profile Data Updated", Toast.LENGTH_SHORT).show()
                gotoMainActivity()
            }
            ?.addOnFailureListener {

                Toast.makeText(this, "Error: ${it.message}", Toast.LENGTH_LONG).show()
            }


    }

    private fun gotoMainActivity() {

        //Clearing back stack because we don't want the user to come back to login activity after pressing back button from main activity
        startActivity(
            Intent(this, MainActivity::class.java)
                .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                .addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK)
        )
        finish()

    }
}

