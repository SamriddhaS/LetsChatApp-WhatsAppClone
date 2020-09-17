package com.samriddha.letschartapp.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.*
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.others.Constants
import com.samriddha.letschartapp.others.Constants.ALL_CONTACTS_KEY_CONTACTS
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_KEY_REQ_TYPE
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_ABOUT
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_DP
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_NAME
import com.samriddha.letschartapp.others.Constants.CONTACTS_VALUE_SAVED
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_CONTACTS
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_REQUESTS
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_FRIENDS
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_NEW
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_RECEIVED
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_SENT
import kotlinx.android.synthetic.main.activity_user_profile.*

class UserProfileActivity : AppCompatActivity() {

    private var receiverUserId = ""
    private var senderUserId = ""
    private var firebaseDbRef: DatabaseReference? = null
    private var firebaseAuth: FirebaseAuth? = null
    private var msgCurrentState = SEND_REQUEST_VALUE_NEW

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_profile)

        firebaseAuth = FirebaseAuth.getInstance()

        //Getting the current user id form firebase Auth : Request Sender
        senderUserId = firebaseAuth?.currentUser?.uid.toString()

        //getting the userId of the profile clicked by user : Request Receiver
        receiverUserId = intent.extras?.getString(Constants.KEY_FIND_FRIENDS_TO_USER_PROFILE_ACTIVITY).toString()

        //getting database reference to current user id. FirebaseDb->All_User->currentUserID
        firebaseDbRef = FirebaseDatabase
            .getInstance()
            .reference

        getUserInfo()

    }

    private fun getUserInfo() {

        //Getting the receivers profile info from firebaseDatabase: Name,About,Dp etc
        firebaseDbRef
            ?.child(DATABASE_PATH_NAME_ALL_USERS)
            ?.child(receiverUserId)
            ?.addValueEventListener(object : ValueEventListener {

                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists() && snapshot.hasChild(ALL_USER_KEY_USER_DP)) {
                        //if user profile dp exists
                        val userImage = snapshot.child(ALL_USER_KEY_USER_DP).value.toString()
                        val userName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()
                        val userAbout = snapshot.child(ALL_USER_KEY_USER_ABOUT).value.toString()

                        tvUserProfileName.text = userName
                        tvUserProfileAbout.text = userAbout

                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        Glide
                            .with(this@UserProfileActivity)
                            .load(userImage)
                            .apply(glideCustomization)
                            .into(ivUserProfileImage)

                        manageChatRequest()

                    } else if (snapshot.exists()) {
                        //If the user hasn't set an profile image/dp
                        val userName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()
                        val userAbout = snapshot.child(ALL_USER_KEY_USER_ABOUT).value.toString()

                        tvUserProfileName.text = userName
                        tvUserProfileAbout.text = userAbout

                        manageChatRequest()
                    }


                }

            })

    }

    private fun manageChatRequest() {

        /*For managing chat request status both on request sender and request receiver end.
        * Sending A request to a new user.
        * Canceling a request that has been sent previously
        * */

        firebaseDbRef
            ?.child(DATABASE_PATH_ALL_REQUESTS)
            ?.child(senderUserId)
            ?.addValueEventListener(object : ValueEventListener{
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {

                    //This part checks if the current user has already send an request to this receiver profile.
                    if (snapshot.exists() && snapshot.hasChild(receiverUserId)){

                        //If Yes then msgCurrentState = SEND_MESSAGE_STAT_SENT / "msg_sent"
                        msgCurrentState = snapshot.child(receiverUserId).child(SEND_REQUEST_KEY_REQ_TYPE).value.toString()


                        if (msgCurrentState == SEND_REQUEST_VALUE_SENT){
                            //If this statement is fulfilled that means the user already sent an request to this profile so the button text is changed.
                            btnUserProfileSendReq.text = "Cancel Request"
                        }

                        if (msgCurrentState == SEND_REQUEST_VALUE_RECEIVED){
                            btnUserProfileSendReq.text = "Accept Request"
                            btnUserProfileRejectReq.visibility = View.VISIBLE
                            //If the user wants to cancel a chat request that is send by another user.
                            btnUserProfileRejectReq.setOnClickListener { cancelChatRequest() }
                        }
                    }else{

                        /*If the reviverUserID is not on DATABASE_PATH_ALL_REQUESTS node we check if he/she is on DATABASE_PATH_ALL_CONTACTS
                        * node,that means both sender and receiver are already friends with each other.*/
                        val firebaseDbAllContactsRef = firebaseDbRef?.child(DATABASE_PATH_ALL_CONTACTS)
                        firebaseDbAllContactsRef
                            ?.child(senderUserId)
                            ?.addValueEventListener(object :ValueEventListener{

                                override fun onCancelled(error: DatabaseError) {}

                                override fun onDataChange(snapshot: DataSnapshot) {

                                    if (snapshot.hasChild(receiverUserId)){

                                        msgCurrentState = SEND_REQUEST_VALUE_FRIENDS
                                        btnUserProfileSendReq.text = "Remove Contact"//Give User Remove Contact Option if they are friends already.
                                    }
                                }
                            })

                    }


                }

            })

        //This if statement makes sure that "Send Request" button is not visible if the user opens his own profile.
        if (receiverUserId != senderUserId) {

            btnUserProfileSendReq.setOnClickListener {

                if (msgCurrentState == SEND_REQUEST_VALUE_NEW){
                    /*If msgCurrentState is SEND_MESSAGE_STAT_NEW/"new" then user wants to send a message request to this profile*/
                    btnUserProfileSendReq.isEnabled = false
                    sendChatRequest()
                }
                if(msgCurrentState == SEND_REQUEST_VALUE_SENT){
                    /*If msgCurrentState is SEND_MESSAGE_STAT_SENT/"msg_sent" then user has already send an request to this profile and
                    * now wants to cancel the request.*/
                    btnUserProfileSendReq.isEnabled = false
                    cancelChatRequest()
                }
                if(msgCurrentState == SEND_REQUEST_VALUE_RECEIVED){
                    /*If msgCurrentState is SEND_REQUEST_VALUE_RECEIVED then user wants to accept the request.*/
                    btnUserProfileSendReq.isEnabled = false
                    acceptChatRequest()
                }
                if(msgCurrentState == SEND_REQUEST_VALUE_FRIENDS){

                    /*If msgCurrentState is SEND_REQUEST_VALUE_RECEIVED then user wants to remove this particular profile from
                    * his/her contact list. */
                    btnUserProfileSendReq.isEnabled = false
                    removeFromContactList()
                }

            }
        } else {
            btnUserProfileSendReq.visibility = View.INVISIBLE
        }

    }

    private fun removeFromContactList() {

        /*Removing both sender and receiver are from each other's contact list.*/
        val firebaseDbAllContactsRef = firebaseDbRef?.child(DATABASE_PATH_ALL_CONTACTS)
        firebaseDbAllContactsRef
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.removeValue()
            ?.addOnSuccessListener {

                /*When the senders node is successfully updated the this usersId/senderUserId is removed from that
                * receiverUserId node.*/
                firebaseDbAllContactsRef
                    .child(receiverUserId)
                    .child(senderUserId)
                    .removeValue()
                    .addOnSuccessListener {

                        btnUserProfileSendReq.isEnabled = true
                        msgCurrentState = SEND_REQUEST_VALUE_NEW
                        btnUserProfileSendReq.text = "Send Request"

                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }
            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

    private fun acceptChatRequest() {

        val firebaseDbAllContactsRef = firebaseDbRef?.child(DATABASE_PATH_ALL_CONTACTS)

        /*When user accepts some request then user and the request sender are friends with each other now.
        * So add both of them(request sender and receiver) to ALL_CONTACTS_KEY_CONTACTS/"All_Contacts"
        * Both sender and receivers id are added to each other id's inside All_Contacts node. */
        firebaseDbAllContactsRef
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.child(ALL_CONTACTS_KEY_CONTACTS)
            ?.setValue(CONTACTS_VALUE_SAVED)
            ?.addOnSuccessListener {

                firebaseDbAllContactsRef
                    .child(receiverUserId)
                    .child(senderUserId)
                    .child(ALL_CONTACTS_KEY_CONTACTS)
                    .setValue(CONTACTS_VALUE_SAVED)
                    .addOnSuccessListener {

                        /*After accepting a request both sender and receiver are friends so we remove them
                        * from DATABASE_PATH_ALL_REQUESTS/"All_Requests" node. */
                        cancelChatRequest(true)
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }

            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

    private fun cancelChatRequest(requestAccepted:Boolean=false) {

        val firebaseDbChatRequestRef =  firebaseDbRef?.child(DATABASE_PATH_ALL_REQUESTS)

        /*The user wants to cancel an request that was previously sent.
        * First the receiversUserId is being removed from this user's/senders node.*/
        firebaseDbChatRequestRef
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.removeValue()
            ?.addOnSuccessListener {

                /*When the senders node is successfully updated the this usersId/senderUserId is removed from that
                * receiverUserId node.*/
                firebaseDbChatRequestRef
                    .child(receiverUserId)
                    .child(senderUserId)
                    .removeValue()
                    .addOnSuccessListener {

                        if (requestAccepted){

                            btnUserProfileSendReq.isEnabled = true
                            msgCurrentState = SEND_REQUEST_VALUE_FRIENDS
                            btnUserProfileSendReq.text = "Remove Contact"
                            btnUserProfileRejectReq.visibility = View.INVISIBLE

                        }else{
                            btnUserProfileSendReq.isEnabled = true
                            msgCurrentState = SEND_REQUEST_VALUE_NEW
                            btnUserProfileSendReq.text = "Send Request"
                            btnUserProfileRejectReq.visibility = View.INVISIBLE
                        }
                        
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }
            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

    private fun sendChatRequest() {

        //All the message request goes under this key: DATABASE_PATH_CHAT_REQUESTS/"ChatRequests"
        val firebaseDbChatRequestRef =  firebaseDbRef?.child(DATABASE_PATH_ALL_REQUESTS)

        /*We first save the receiversId under sendersUserID for saving all the profile where this user has sent an request.
        * We create a child called DATABASE_KEY_MESSAGE_REQ_TYPE/"request_type" for identifying who send that request and who received
        * that request.
        * This will store all the request that are send by this particular user inside his userId.
        * */
        firebaseDbChatRequestRef
            ?.child(senderUserId)
            ?.child(receiverUserId)
            ?.child(SEND_REQUEST_KEY_REQ_TYPE)
            ?.setValue(SEND_REQUEST_VALUE_SENT)
            ?.addOnSuccessListener {

                /*When the senders node is updated successfully,we update the receivers node for saving the sendersId and request type
                * inside receiversUserId.*/
                firebaseDbChatRequestRef
                    .child(receiverUserId)
                    .child(senderUserId)
                    .child(SEND_REQUEST_KEY_REQ_TYPE)
                    .setValue(SEND_REQUEST_VALUE_RECEIVED)
                    .addOnSuccessListener {

                        btnUserProfileSendReq.isEnabled = true
                        msgCurrentState = SEND_REQUEST_VALUE_SENT
                        btnUserProfileSendReq.text = "Cancel Request"
                    }
                    .addOnFailureListener {
                        Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }
            }
            ?.addOnFailureListener {
                Toast.makeText(this,"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }

    }
}