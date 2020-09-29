package com.samriddha.letschartapp.ui

import android.app.Activity
import android.content.DialogInterface
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.google.firebase.storage.FirebaseStorage
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.MessagesRecyclerAdapter
import com.samriddha.letschartapp.models.Messages
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_ALL_CHATS
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_FROM
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_LAST_MESSAGE
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_MESSAGE_DATE
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_MESSAGE_ID
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_MESSAGE_TIME
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_MSG_TYPE
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_TO
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_STATE
import com.samriddha.letschartapp.others.Constants.ALL_USER_USER_STATE_KEY_DATE
import com.samriddha.letschartapp.others.Constants.ALL_USER_USER_STATE_KEY_IS_ONLINE
import com.samriddha.letschartapp.others.Constants.ALL_USER_USER_STATE_KEY_TIME
import com.samriddha.letschartapp.others.Constants.DATABASE_KEY_MESSAGE
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_MESSAGES
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.others.Constants.KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_IMAGE_MESSAGE
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_TEXT_MESSAGE
import com.samriddha.letschartapp.others.Constants.PICK_IMAGE_FROM_GALLERY
import com.samriddha.letschartapp.others.Constants.STORAGE_REF_PATH_IMAGE_FILES
import kotlinx.android.synthetic.main.activity_private_chat.*
import kotlinx.android.synthetic.main.activity_settings.*
import java.text.SimpleDateFormat
import java.util.*
import kotlin.collections.HashMap

class PrivateChatActivity : AppCompatActivity() {

    private var senderUserId = ""
    private var receiverUserId = ""
    private var firebaseDbRef:DatabaseReference? = null
    private var messagesAdapter:MessagesRecyclerAdapter? = null
    private var messagesList = ArrayList<Messages>()
    private var fileType = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)


        senderUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        firebaseDbRef = FirebaseDatabase.getInstance().reference

        setToolbarData()

        initRecyclerView()

        showOldMessages()

        btnSendPrivateChat.setOnClickListener {
            sendMessage()
        }

        btnPrivateChatImage.setOnClickListener {

            val options = arrayOf("Images","PDF","MS Docs")

            val alertDialog = AlertDialog.Builder(this)
            alertDialog.setTitle("Select File Format")
            alertDialog.setItems(options, DialogInterface.OnClickListener { dialog, which ->

                when(which){
                    0 -> {
                        fileType = "image"
                        val intent = Intent()
                        intent.action = Intent.ACTION_GET_CONTENT
                        intent.type = "image/*"
                        intent.putExtra(Intent.EXTRA_TEXT,"Please Select An Image")
                        startActivityForResult(intent, PICK_IMAGE_FROM_GALLERY)
                    }
                    1 -> {


                        fileType = "pdf"
                    }
                    2 -> {

                        fileType = "docx"
                    }
                }

            })
            alertDialog.show()

        }

    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_FROM_GALLERY
            && resultCode == Activity.RESULT_OK
            && data!=null
            && data.data != null){

            val fileUri = data.data
            if (fileType == "image"){

                privateChatProgressBar.visibility = View.VISIBLE
                window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
                    , WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                val firebaseStorageRef = FirebaseStorage.getInstance().reference.child(STORAGE_REF_PATH_IMAGE_FILES)

                val userMsgKeyRef = firebaseDbRef
                    ?.child(DATABASE_PATH_ALL_MESSAGES)
                    ?.child(senderUserId)
                    ?.child(receiverUserId)
                    ?.child(ALL_MESSAGES_KEY_ALL_CHATS)
                    ?.push()

                //unique key generated for each message
                val messagePushId = userMsgKeyRef?.key

                val filePath = firebaseStorageRef.child("$messagePushId.jpg")
                filePath
                    .putFile(fileUri!!)
                    .addOnSuccessListener {

                        /*Image is added to firebaseStorage successfully,we get the downloadUrl of that image
                        * and save it inside fbDatabase under current user profile*/
                        filePath
                            .downloadUrl
                            .addOnSuccessListener {

                                //getting download link of the uploaded image from firebase storage.
                                val downloadUrl = it.toString()

                                val calender = Calendar.getInstance()

                                val dateFormat = SimpleDateFormat("MMM dd")
                                val currentDate = dateFormat.format(calender.time)
                                val timeFormat = SimpleDateFormat("hh:mm a")
                                val currentTime = timeFormat.format(calender.time)

                                val msgImageBody = HashMap<String,String>()
                                msgImageBody[ALL_MESSAGES_KEY_MESSAGE_ID] = messagePushId.toString()
                                msgImageBody[ALL_MESSAGES_KEY_MSG_TYPE] = MSG_TYPE_VALUE_IMAGE_MESSAGE
                                msgImageBody[DATABASE_KEY_MESSAGE] = downloadUrl
                                msgImageBody["fileName"] = fileUri.lastPathSegment.toString()
                                msgImageBody[ALL_MESSAGES_KEY_TO] = receiverUserId
                                msgImageBody[ALL_MESSAGES_KEY_FROM] = senderUserId
                                msgImageBody[ALL_MESSAGES_KEY_MESSAGE_DATE] = currentDate
                                msgImageBody[ALL_MESSAGES_KEY_MESSAGE_TIME] = currentTime

                                /*We want to update both sender and receiver same time so we make two references of database using below variables and put them
                                * in a hash map.*/
                                val msgSenderRef = "$DATABASE_PATH_ALL_MESSAGES/$senderUserId/$receiverUserId/$ALL_MESSAGES_KEY_ALL_CHATS"
                                val msgReceiverRef = "$DATABASE_PATH_ALL_MESSAGES/$receiverUserId/$senderUserId/$ALL_MESSAGES_KEY_ALL_CHATS"

                                val messageDetails = HashMap<String,Any>()
                                messageDetails["$msgSenderRef/$messagePushId"] = msgImageBody
                                messageDetails["$msgReceiverRef/$messagePushId"] = msgImageBody

                                firebaseDbRef
                                    ?.updateChildren(messageDetails)
                                    ?.addOnSuccessListener {
                                        Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
                                        privateChatProgressBar.visibility = View.INVISIBLE
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    }
                                    ?.addOnFailureListener {
                                        Toast.makeText(this,"Error:${it.message}",Toast.LENGTH_SHORT).show()
                                        privateChatProgressBar.visibility = View.INVISIBLE
                                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                                    }


                            }
                            .addOnFailureListener {
                                //If downloading url from firebaseStorage is not successful.
                                Toast.makeText(this,"Image Url Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                                privateChatProgressBar.visibility = View.INVISIBLE
                                window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
                            }

                    }
                    .addOnFailureListener {

                        //If saving image inside firebaseStorage is not successful
                        Toast.makeText(this,"Storage Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                        privateChatProgressBar.visibility = View.INVISIBLE
                        window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)

                    }
            }

        }
    }

    private fun initRecyclerView() {

        messagesAdapter = MessagesRecyclerAdapter()
        privateChatRecView.layoutManager = LinearLayoutManager(this)
        privateChatRecView.setHasFixedSize(true)
        privateChatRecView.adapter = messagesAdapter

    }

    private fun showOldMessages() {

        firebaseDbRef
            ?.child(DATABASE_PATH_ALL_MESSAGES)
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.child(ALL_MESSAGES_KEY_ALL_CHATS)
            ?.addChildEventListener(object : ChildEventListener{
                override fun onCancelled(error: DatabaseError) {
                }

                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {

                    val message = snapshot.getValue(Messages::class.java)
                    message?.let {
                        messagesList.add(it)
                    }
                    messagesAdapter?.submitMessagesList(messagesList)
                    privateChatRecView.smoothScrollToPosition(messagesAdapter?.itemCount!!)
                }

                override fun onChildRemoved(snapshot: DataSnapshot) {
                }

            })

    }

    private fun setToolbarData() {
        receiverUserId = intent.extras?.getString(KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY).toString()
        val userName = intent.extras?.getString(KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY)
        val userImage = intent.extras?.getString(KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY)

        tvPrivateChatToolbarName.text = userName
        val glideCustomization = RequestOptions()
            .placeholder(R.drawable.default_profile_image)
            .error(R.drawable.default_profile_image)
        Glide
            .with(this)
            .load(userImage)
            .apply(glideCustomization)
            .into(ivPrivateChatToolbarDp)

        displayLastSeen()

    }

    private fun sendMessage() {

        val message = etPrivateChat.text.toString()

        if (message.isEmpty()){
            Toast.makeText(this,"Enter A Message First",Toast.LENGTH_SHORT).show()
            return
        }

        //Generating random key inside db -> DATABASE_PATH_ALL_MESSAGES -> senderId -> receiverId
        val userMsgKeyRef = firebaseDbRef
            ?.child(DATABASE_PATH_ALL_MESSAGES)
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.child(ALL_MESSAGES_KEY_ALL_CHATS)
            ?.push()

        val messagePushId = userMsgKeyRef?.key

        //getting date and time of the message send time
        val calender = Calendar.getInstance()

        val dateFormat = SimpleDateFormat("MMM dd")
        val currentDate = dateFormat.format(calender.time)
        val timeFormat = SimpleDateFormat("hh:mm a")
        val currentTime = timeFormat.format(calender.time)

        val messageBody = HashMap<String,String>()
        messageBody[ALL_MESSAGES_KEY_MESSAGE_ID] = messagePushId.toString()
        messageBody[ALL_MESSAGES_KEY_MSG_TYPE] = MSG_TYPE_VALUE_TEXT_MESSAGE
        messageBody[DATABASE_KEY_MESSAGE] = message
        messageBody[ALL_MESSAGES_KEY_TO] = receiverUserId
        messageBody[ALL_MESSAGES_KEY_FROM] = senderUserId
        messageBody[ALL_MESSAGES_KEY_MESSAGE_DATE] = currentDate
        messageBody[ALL_MESSAGES_KEY_MESSAGE_TIME] = currentTime

        /*We want to update both sender and receiver same time so we make two references of database using below variables and put them
        * in a hash map.*/
        val messageSenderRef = "$DATABASE_PATH_ALL_MESSAGES/$senderUserId/$receiverUserId/$ALL_MESSAGES_KEY_ALL_CHATS"
        val messageReceiverRef = "$DATABASE_PATH_ALL_MESSAGES/$receiverUserId/$senderUserId/$ALL_MESSAGES_KEY_ALL_CHATS"
        val lastMessageSenderRef = "$DATABASE_PATH_ALL_MESSAGES/$receiverUserId/$senderUserId/$ALL_MESSAGES_KEY_LAST_MESSAGE"
        val lastMessageReceiverRef = "$DATABASE_PATH_ALL_MESSAGES/$senderUserId/$receiverUserId/$ALL_MESSAGES_KEY_LAST_MESSAGE"

        //We want to inset "messageBody" inside both sender and receiver node.So we use an hashMap for updating both nodes at sane time.
        val messageDetails = HashMap<String,Any>()
        messageDetails["$messageSenderRef/$messagePushId"] = messageBody
        messageDetails["$messageReceiverRef/$messagePushId"] = messageBody
        messageDetails[lastMessageSenderRef] = message
        messageDetails[lastMessageReceiverRef] = message

        /* "messageDetails" has both database references of sender and receiver and messageBody that we want to insert into those nodes*/
        firebaseDbRef
            ?.updateChildren(messageDetails)
            ?.addOnSuccessListener {
                Toast.makeText(this,"Success",Toast.LENGTH_SHORT).show()
                etPrivateChat.setText("")
            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error:${it.message}",Toast.LENGTH_SHORT).show()
            }
    }

    private fun displayLastSeen() = firebaseDbRef
        ?.child(DATABASE_PATH_NAME_ALL_USERS)
        ?.child(receiverUserId)
        ?.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if(snapshot.hasChild(ALL_USER_KEY_USER_STATE)){

                    val isOnline = snapshot.child(ALL_USER_KEY_USER_STATE).child(ALL_USER_USER_STATE_KEY_IS_ONLINE).value.toString()
                    val lastSeenDate = snapshot.child(ALL_USER_KEY_USER_STATE).child(ALL_USER_USER_STATE_KEY_DATE).value.toString()
                    val lastSeenTime = snapshot.child(ALL_USER_KEY_USER_STATE).child(ALL_USER_USER_STATE_KEY_TIME).value.toString()

                    if(isOnline=="true"){
                        tvPrivateChatToolbarLastSeen.text = "Online"
                    }else{
                        tvPrivateChatToolbarLastSeen.text = "Last Seen:$lastSeenTime,$lastSeenDate"
                    }

                }else{
                    tvPrivateChatToolbarLastSeen.text = "Offline"
                }
            }

        })

}