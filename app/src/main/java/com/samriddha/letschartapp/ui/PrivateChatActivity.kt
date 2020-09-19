package com.samriddha.letschartapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.MessagesRecyclerAdapter
import com.samriddha.letschartapp.models.Messages
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_FROM
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_MSG_TYPE
import com.samriddha.letschartapp.others.Constants.DATABASE_KEY_MESSAGE
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_MESSAGES
import com.samriddha.letschartapp.others.Constants.KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_TEXT_MESSAGE
import kotlinx.android.synthetic.main.activity_private_chat.*
import java.util.*
import kotlin.collections.HashMap

class PrivateChatActivity : AppCompatActivity() {

    private var senderUserId = ""
    private var receiverUserId = ""
    private var firebaseDbRef:DatabaseReference? = null
    private var messagesAdapter:MessagesRecyclerAdapter? = null
    private var messagesList = ArrayList<Messages>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_private_chat)


        senderUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        firebaseDbRef = FirebaseDatabase.getInstance().reference

        setToolbarData()

        initRecyclerView()

        btnSendPrivateChat.setOnClickListener {
            sendMessage()
        }

        showOldMessages()

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
            ?.push()

        val messagePushId = userMsgKeyRef?.key


        val messageBody = HashMap<String,String>()
        messageBody[DATABASE_KEY_MESSAGE] = message
        messageBody[ALL_MESSAGES_KEY_MSG_TYPE] = MSG_TYPE_VALUE_TEXT_MESSAGE
        messageBody[ALL_MESSAGES_KEY_FROM] = senderUserId

        /*We want to update both sender and receiver same time so we make two references of database using below variables and put them
        * in a hash map.*/
        val messageSenderRef = "$DATABASE_PATH_ALL_MESSAGES/$senderUserId/$receiverUserId"
        val messageReceiverRef = "$DATABASE_PATH_ALL_MESSAGES/$receiverUserId/$senderUserId"

        //We want to inset "messageBody" inside both sender and receiver node.So we use an hashMap for updating both nodes at sane time.
        val messageDetails = HashMap<String,Any>()
        messageDetails["$messageSenderRef/$messagePushId"] = messageBody
        messageDetails["$messageReceiverRef/$messagePushId"] = messageBody

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
}