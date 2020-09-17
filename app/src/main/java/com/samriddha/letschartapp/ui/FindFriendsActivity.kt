package com.samriddha.letschartapp.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.FirebaseRecFindFriendsAdapter
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.others.Constants.KEY_FIND_FRIENDS_TO_USER_PROFILE_ACTIVITY
import kotlinx.android.synthetic.main.activity_find_friends.*

class FindFriendsActivity : AppCompatActivity(),FirebaseRecFindFriendsAdapter.FirebaseRecAdapterInterface{

    private var firebaseDbRefAllUsers:DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_find_friends)

        //Firebase Database Reference To All Users.
        firebaseDbRefAllUsers = FirebaseDatabase.getInstance().reference.child(DATABASE_PATH_NAME_ALL_USERS)
        initRecyclerView()




    }

    private fun initRecyclerView() {

        //setting toolbar and back button inside toolbar
        setSupportActionBar(findFriendsToolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        findFriendsToolbar?.title = "Find Friends"

        //setting Recycler View
        findFriendsRecView.layoutManager = LinearLayoutManager(this)
        findFriendsRecView.setHasFixedSize(true)


        /*This will have reference to firebaseDb and model class that will be mapped with data from firebaseDb*/
        val options = firebaseDbRefAllUsers?.let {
            FirebaseRecyclerOptions.Builder<Contacts>()
                .setQuery(it, Contacts::class.java)
                .setLifecycleOwner(this) //for start listening and stop listening automatically.
                .build()
        }

        /*Firebase Recycler View Adapter will take all the user data from firebase db and map it to Contacts object.*/
        val firebaseRecAdapter = FirebaseRecFindFriendsAdapter(options!!,this)
        findFriendsRecView.adapter = firebaseRecAdapter
//        firebaseRecAdapter.startListening()

    }

    override fun onUserItemClickListener(userId: String) {

        startActivity(Intent(this,UserProfileActivity::class.java).putExtra(KEY_FIND_FRIENDS_TO_USER_PROFILE_ACTIVITY,userId))
    }
}