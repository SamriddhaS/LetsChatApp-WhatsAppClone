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
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants
import com.samriddha.letschartapp.others.Constants.ALL_MESSAGES_KEY_LAST_MESSAGE
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_DP
import com.samriddha.letschartapp.others.Constants.ALL_USER_KEY_USER_NAME
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_MESSAGES
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_NAME_ALL_USERS
import kotlinx.android.synthetic.main.show_chats_item.view.*

class FirebaseRecChatsAdapter(
    options: FirebaseRecyclerOptions<Contacts>
    , val context: Context
    , val mListener: FirebaseRecChatsAdapter.FirebaseRecAdapterInterface
) :
    FirebaseRecyclerAdapter<Contacts, FirebaseRecChatsAdapter.FirebaseRecViewHolder>(options) {

    private val firebaseDbRef = FirebaseDatabase.getInstance().reference
    private val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

    private val userNameMap = HashMap<String,String>()
    private val userImageMap = HashMap<String,String>()

    inner class FirebaseRecViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        init {
            itemView.setOnClickListener {

                val userId = getRef(adapterPosition).key.toString()
                mListener.onChatItemClickListener(userId,userNameMap[userId]!!,userImageMap[userId]!!)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseRecViewHolder {
        return FirebaseRecViewHolder(
            LayoutInflater
                .from(parent.context)
                .inflate(R.layout.show_chats_item, parent, false)
        )
    }

    override fun onBindViewHolder(holder: FirebaseRecViewHolder, position: Int, model: Contacts) {

        val userId = getRef(position).key

        val firebaseDbAllUserRef = firebaseDbRef.child(DATABASE_PATH_NAME_ALL_USERS)
        val firebaseDbLastMessagesRef = firebaseDbRef
            .child(DATABASE_PATH_ALL_MESSAGES)
            .child(currentUserId!!)
            .child(userId!!)

        firebaseDbAllUserRef
            .child(userId)
            .addValueEventListener(object : ValueEventListener {
                override fun onCancelled(error: DatabaseError) {}

                override fun onDataChange(snapshot: DataSnapshot) {

                    var userImage = ""
                    var userName = ""

                    if (snapshot.exists() && snapshot.hasChild(ALL_USER_KEY_USER_DP)) {
                        //if user profile dp exists
                         userImage = snapshot.child(ALL_USER_KEY_USER_DP).value.toString()
                         userName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()

                        holder.itemView.tvChatsItemName.text = userName

                        val glideCustomization = RequestOptions()
                            .placeholder(R.drawable.default_profile_image)
                            .error(R.drawable.default_profile_image)
                        Glide
                            .with(context)
                            .load(userImage)
                            .apply(glideCustomization)
                            .into(holder.itemView.ivChatsItemDp)


                    } else if (snapshot.exists()) {
                        //If the user hasn't set an profile image/dp
                        userName = snapshot.child(ALL_USER_KEY_USER_NAME).value.toString()

                        holder.itemView.tvChatsItemName.text = userName
                    }

                    userNameMap[userId] = userName
                    userImageMap[userId] = userImage
                }

            })

        firebaseDbLastMessagesRef.addValueEventListener(object : ValueEventListener{
            override fun onCancelled(error: DatabaseError){}
            override fun onDataChange(snapshot: DataSnapshot) {

                if (snapshot.exists() && snapshot.child(ALL_MESSAGES_KEY_LAST_MESSAGE).exists() ){
                    holder.itemView.tvChatsItemLast.text = snapshot.child(ALL_MESSAGES_KEY_LAST_MESSAGE).value.toString()
                }else{
                    holder.itemView.tvChatsItemLast.text = "Last Message"
                }
            }
        })



    }

    interface FirebaseRecAdapterInterface {
        fun onChatItemClickListener(userId: String,userName:String,userImage:String)
    }
}