package com.samriddha.letschartapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.database.*
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_KEY_REQ_TYPE
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_RECEIVED
import com.samriddha.letschartapp.others.Constants.SEND_REQUEST_VALUE_SENT
import kotlinx.android.synthetic.main.show_requests_item.view.*

class FirebaseRecRequestsAdapter(
    options: FirebaseRecyclerOptions<Contacts>
    , val context: Context
    , val mListener: FirebaseRecRequestsAdapter.FirebaseRecAdapterInterface
) :
    FirebaseRecyclerAdapter<Contacts, FirebaseRecRequestsAdapter.FirebaseRecViewHolder>(options) {

    private val firebaseDbAllUsersRef =
        FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_PATH_NAME_ALL_USERS)

    inner class FirebaseRecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {

            itemView.btnShowReqAccept.setOnClickListener {
                mListener.onRequestAcceptClickListener(getRef(adapterPosition).key.toString())
            }
            itemView.btnShowReqReject.setOnClickListener {
                mListener.onRequestRejectClickListener(getRef(adapterPosition).key.toString())
            }
            
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseRecViewHolder {
        return FirebaseRecViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.show_requests_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FirebaseRecViewHolder, position: Int, model: Contacts) {

        val userId =
            getRef(position).key //getRef has the reference to the node where we have set our query parameter for FirebaseRecyclerOptions<Contacts>
        val firebaseDbRefRequestType = getRef(position).child(SEND_REQUEST_KEY_REQ_TYPE).ref

        firebaseDbRefRequestType.addValueEventListener(object : ValueEventListener {
            override fun onCancelled(error: DatabaseError) {}

            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists()) {
                    /*If this if statement satisfies that means this userId has send an request to this currentUser.*/
                    if (snapshot.value.toString() == SEND_REQUEST_VALUE_RECEIVED) {
                        showThisUsersProfile(userId, holder,true)
                    }
                    if(snapshot.value.toString() == SEND_REQUEST_VALUE_SENT){
                        showThisUsersProfile(userId,holder,false)
                    }

                }
            }

        })

    }

    private fun showThisUsersProfile(userId: String?, holder: FirebaseRecViewHolder,requestReceived:Boolean) {

        firebaseDbAllUsersRef
            .child(userId!!)
            .addValueEventListener(object : ValueEventListener {

                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {

                    if (snapshot.exists() && snapshot.hasChild(Constants.ALL_USER_KEY_USER_DP)) {

                        //if user profile dp exists
                        val userImage=snapshot.child(Constants.ALL_USER_KEY_USER_DP).value.toString()
                        val userName=snapshot.child(Constants.ALL_USER_KEY_USER_NAME).value.toString()
                        //val userAbout =snapshot.child(Constants.ALL_USER_KEY_USER_ABOUT).value.toString()

                        holder.itemView.tvShowReqItemName.text = userName

                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        Glide
                            .with(context)
                            .load(userImage)
                            .apply(glideCustomization)
                            .into(holder.itemView.ivShowReqItemDp)

                    } else if (snapshot.exists()) {

                        //If the user hasn't set an profile image/dp
                        val userName = snapshot.child(Constants.ALL_USER_KEY_USER_NAME).value.toString()
                        //val userAbout=snapshot.child(Constants.ALL_USER_KEY_USER_ABOUT).value.toString()

                        holder.itemView.tvShowReqItemName.text = userName
                    }

                    if (requestReceived){

                        holder.itemView.btnShowReqAccept.visibility = View.VISIBLE
                        holder.itemView.btnShowReqReject.text = "Reject"
                        holder.itemView.tvShowReqItemAbout.text = "Received An Request"

                    }else{

                        //If user has sent an request to someone then we don't show the accept button
                        holder.itemView.btnShowReqAccept.visibility = View.INVISIBLE
                        holder.itemView.btnShowReqReject.text = "Cancel"
                        holder.itemView.tvShowReqItemAbout.text = "Request Sent"
                    }


                }

            })
    }

    interface FirebaseRecAdapterInterface {
        fun onRequestAcceptClickListener(userId: String)
        fun onRequestRejectClickListener(userId: String)
    }
}