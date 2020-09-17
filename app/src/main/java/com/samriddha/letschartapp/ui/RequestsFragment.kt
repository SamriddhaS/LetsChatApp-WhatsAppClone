package com.samriddha.letschartapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.FirebaseRecRequestsAdapter
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants
import com.samriddha.letschartapp.others.Constants.ALL_CONTACTS_KEY_CONTACTS
import com.samriddha.letschartapp.others.Constants.CONTACTS_VALUE_SAVED
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_CONTACTS
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_REQUESTS
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.fragment_requests.*


class RequestsFragment : Fragment(),FirebaseRecRequestsAdapter.FirebaseRecAdapterInterface {

    private var currentUserId = ""
    private var firebaseDbRef:DatabaseReference?=null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_requests, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseDbRef = FirebaseDatabase.getInstance().reference
        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString() //Getting current userId from firebase Auth.

        initRecyclerView()

    }

    private fun initRecyclerView() {
        requestsRecView.layoutManager = LinearLayoutManager(requireContext())
        requestsRecView.setHasFixedSize(true)

        /*This will have reference to firebaseDb and model class that will be mapped with data from child of firebaseDatabase's
        * current reference is at. firebaseDbRequestsRef->currentUserId -> Data for recycler view*/
        val options = firebaseDbRef
            ?.child(DATABASE_PATH_ALL_REQUESTS)
            ?.child(currentUserId)
            ?.let {
                FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(it, Contacts::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()
            }

        /*Firebase Recycler View Adapter will take all the user data from firebase db and map it to Contacts object.*/
        val firebaseRecAdapter = FirebaseRecRequestsAdapter(options!!,requireContext(),this)
        requestsRecView.adapter = firebaseRecAdapter
    }

    override fun onRequestAcceptClickListener(userId: String) {
        acceptChatRequest(userId)
    }

    override fun onRequestRejectClickListener(userId: String) {
        removeChatRequest(userId,false)
    }

    private fun acceptChatRequest(senderUserId:String) {

        val firebaseDbAllContactsRef = firebaseDbRef?.child(DATABASE_PATH_ALL_CONTACTS)

        /*When user accepts some request then user and the request sender are friends with each other now.
        * So add both of them(request sender and receiver) to ALL_CONTACTS_KEY_CONTACTS/"All_Contacts"
        * Both sender and receivers id are added to each other id's inside All_Contacts node. */
        firebaseDbAllContactsRef
            ?.child(senderUserId)
            ?.child(currentUserId)
            ?.child(ALL_CONTACTS_KEY_CONTACTS)
            ?.setValue(CONTACTS_VALUE_SAVED)
            ?.addOnSuccessListener {

                firebaseDbAllContactsRef
                    .child(currentUserId)
                    .child(senderUserId)
                    .child(ALL_CONTACTS_KEY_CONTACTS)
                    .setValue(CONTACTS_VALUE_SAVED)
                    .addOnSuccessListener {

                        removeChatRequest(senderUserId,true)

                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }

            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(),"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

    private fun removeChatRequest(senderUserId:String,requestAccepted:Boolean) {

        val firebaseDbChatRequestRef =  firebaseDbRef?.child(DATABASE_PATH_ALL_REQUESTS)

        /*The user wants to cancel an request that was previously sent.
        * First the receiversUserId is being removed from this user's/senders node.*/
        firebaseDbChatRequestRef
            ?.child(senderUserId)
            ?.child(currentUserId)
            ?.removeValue()
            ?.addOnSuccessListener {

                /*When the senders node is successfully updated the this usersId/senderUserId is removed from that
                * receiverUserId node.*/
                firebaseDbChatRequestRef
                    .child(currentUserId)
                    .child(senderUserId)
                    .removeValue()
                    .addOnSuccessListener {

                        if (requestAccepted){
                            Toast.makeText(requireContext(),"Profile Added To Your Contacts",Toast.LENGTH_LONG).show()
                        }else{
                            Toast.makeText(requireContext(),"Request Removed",Toast.LENGTH_LONG).show()
                        }

                    }
                    .addOnFailureListener {
                        Toast.makeText(requireContext(),"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
                    }
            }
            ?.addOnFailureListener {
                Toast.makeText(requireContext(),"Error:${it.message.toString()}",Toast.LENGTH_LONG).show()
            }
    }

}