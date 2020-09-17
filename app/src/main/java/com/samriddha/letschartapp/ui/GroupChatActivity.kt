package com.samriddha.letschartapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ScrollView
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.others.Constants.DATABASE_KEY_MESSAGE
import com.samriddha.letschartapp.others.Constants.DATABASE_KEY_MSG_DATE
import com.samriddha.letschartapp.others.Constants.DATABASE_KEY_MSG_TIME
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_NAME
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_GROUPS
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.others.Constants.KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.DateTimeProvider
import kotlinx.android.synthetic.main.activity_group_chat.*
import kotlin.collections.HashMap

class GroupChatActivity : AppCompatActivity() {

    private var currentUserId:String? = null
    private var currentUserName:String? = null
    private var currentGroupName:String? = null
    private var firebaseAuth:FirebaseAuth? = null
    private var firebaseDbRef:DatabaseReference? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_chat)

        setSupportActionBar(groupChatToolbar)


        //Getting Group Name From MyGroupsFragment
        currentGroupName = intent.extras?.getString(KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY)
        groupChatToolbar.title = currentGroupName


        firebaseAuth = FirebaseAuth.getInstance()
        currentUserId = firebaseAuth?.currentUser?.uid //Getting Firebase user id from firebase auth
        firebaseDbRef = FirebaseDatabase.getInstance().reference


        showOldMessages()

        getUserInfo()

        btnSendGroupChat.setOnClickListener {
            saveMessageToDb()
            groupChatEt.setText("")
            groupChatScrollView.fullScroll(ScrollView.FOCUS_DOWN) //Setting the scroll view position on newest item.
        }



    }

    private fun showOldMessages() {

        //Getting reference to current group name
        val firebaseDbGroupNameRef = firebaseDbRef?.child(DATABASE_PATH_GROUPS)?.child(currentGroupName!!)

        firebaseDbGroupNameRef
            ?.addChildEventListener(object :ChildEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {

                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                    if(snapshot.exists()){
                        displayMessages(snapshot)
                    }
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    if(snapshot.exists()){
                        displayMessages(snapshot)
                    }
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {

                }

            })


    }

    private fun displayMessages(snapshot: DataSnapshot) {

        //Iterator has all the child in <key,values> pair that is available inside snapshot. Database->All_Groups->GroupName-> snapshot
        val iterator = snapshot.children.iterator()

        while (iterator.hasNext()){

            val chatDate = iterator.next().value.toString()
            val chatMessage = iterator.next().value.toString()
            val chatName = iterator.next().value.toString()
            val chatTime = iterator.next().value.toString()

            groupChatTextDisplay.append(" $chatName: \n $chatMessage \n $chatTime \n $chatDate \n\n\n\n")

            groupChatScrollView.fullScroll(ScrollView.FOCUS_DOWN)
        }




    }

    private fun saveMessageToDb() {

        val message = groupChatEt.text.toString()

        if (message.isEmpty())
        {
            //If the edit text is empty we don't do anything.
            Toast.makeText(this,"Enter Your Message First",Toast.LENGTH_SHORT).show()
            return
        }

        //getting reference to current group from database. Database -> All_Groups -> Group_Name
        val firebaseDbGroupNameRef = firebaseDbRef?.child(DATABASE_PATH_GROUPS)?.child(currentGroupName!!)

        //generating an unique key for each message that will be stored to the db.
        val messageDbKey = firebaseDbGroupNameRef?.push()?.key


        //getting current date and time.
        val currentDate = DateTimeProvider.getCurrentDate()
        val currentTime = DateTimeProvider.getCurrentTime()

        //Don't Know why.
        val groupMessageKey = HashMap<String,Any>()
        firebaseDbGroupNameRef?.updateChildren(groupMessageKey)

        //creating a child with a unique key("messageDbKey") under which message and its details(senders name,time,date) will be saved.
        val firebaseDbGroupsMessageKeyRef = firebaseDbGroupNameRef?.child(messageDbKey!!)

        val groupMessageInfoMap = HashMap<String,Any>()
        groupMessageInfoMap[ALL_USER_KEY_USER_NAME] = currentUserName!!
        groupMessageInfoMap[DATABASE_KEY_MESSAGE] = message
        groupMessageInfoMap[DATABASE_KEY_MSG_DATE] = currentDate
        groupMessageInfoMap[DATABASE_KEY_MSG_TIME] = currentTime

        firebaseDbGroupsMessageKeyRef?.updateChildren(groupMessageInfoMap) //Inserting all the data into firebase database.

    }

    private fun getUserInfo() {

        //getting user name form database using its user id.
        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(currentUserId!!)
            ?.addValueEventListener(object : ValueEventListener{

                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    //User name is saved under "DATABASE_KEY_USER_NAME" key.So we use this key for getting user name.
                    if (snapshot.exists()){
                        currentUserName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()
                    }
                }

            })



    }
}