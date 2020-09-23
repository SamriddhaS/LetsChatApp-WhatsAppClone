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
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_DP
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_STATE
import com.samriddha.letschartapp.others.Constants.ALL_USER_USER_STATE_KEY_IS_ONLINE
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import kotlinx.android.synthetic.main.activity_private_chat.*
import kotlinx.android.synthetic.main.activity_user_profile.*
import kotlinx.android.synthetic.main.show_users_item.view.*

class FirebaseRecContactsAdapter(options: FirebaseRecyclerOptions<Contacts>, val context: Context) :
    FirebaseRecyclerAdapter<Contacts, FirebaseRecContactsAdapter.FirebaseRecViewHolder>(options) {

    private val firebaseDbAllUsersRef =
        FirebaseDatabase.getInstance().reference.child(DATABASE_PATH_NAME_ALL_USERS)

    inner class FirebaseRecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {

                //mListener.onUserItemClickListener(getRef(adapterPosition).key.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseRecViewHolder {
        return FirebaseRecViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.show_users_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FirebaseRecViewHolder, position: Int, model: Contacts) {

        val userId = getRef(position).key

        firebaseDbAllUsersRef
            .child(userId!!)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {

                }

                override fun onDataChange(snapshot: DataSnapshot) {
                    if (snapshot.exists() && snapshot.hasChild(ALL_USER_KEY_USER_DP)) {
                        //if user profile dp exists
                        val userImage = snapshot.child(ALL_USER_KEY_USER_DP).value.toString()
                        val userName =
                            snapshot.child(Constants.ALL_USER_KEY_USER_NAME).value.toString()
                        val userAbout =
                            snapshot.child(Constants.ALL_USER_KEY_USER_ABOUT).value.toString()

                        holder.itemView.tvShowUserItemName.text = userName
                        holder.itemView.tvShowUserItemAbout.text = userAbout

                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        Glide
                            .with(context)
                            .load(userImage)
                            .apply(glideCustomization)
                            .into(holder.itemView.ivShowUserItemDp)

                        displayUserOnline(snapshot,holder)

                    } else if (snapshot.exists()) {
                        //If the user hasn't set an profile image/dp
                        val userName =
                            snapshot.child(Constants.ALL_USER_KEY_USER_NAME).value.toString()
                        val userAbout =
                            snapshot.child(Constants.ALL_USER_KEY_USER_ABOUT).value.toString()

                        holder.itemView.tvShowUserItemName.text = userName
                        holder.itemView.tvShowUserItemAbout.text = userAbout

                        displayUserOnline(snapshot,holder)
                    }
                }

            })

    }

    private fun displayUserOnline(snapshot: DataSnapshot,holder: FirebaseRecViewHolder) {

        if (snapshot.hasChild(ALL_USER_KEY_USER_STATE)) {

            val isOnline = snapshot.child(ALL_USER_KEY_USER_STATE).child(ALL_USER_USER_STATE_KEY_IS_ONLINE).value.toString()

            if (isOnline == "true") {
                holder.itemView.ivShowUserItemOnline.visibility = View.VISIBLE
            } else {
                holder.itemView.ivShowUserItemOnline.visibility = View.INVISIBLE
            }


        } else {
            holder.itemView.ivShowUserItemOnline.visibility = View.INVISIBLE
        }
              
    }

    interface FirebaseRecAdapterInterface {
        fun onUserItemClickListener(userId: String)
    }
}