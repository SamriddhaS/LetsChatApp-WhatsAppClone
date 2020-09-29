package com.samriddha.letschartapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Messages
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_IMAGE_MESSAGE
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_TEXT_MESSAGE
import kotlinx.android.synthetic.main.show_chats_item.view.*
import kotlinx.android.synthetic.main.text_messages_item.view.*

class MessagesRecyclerAdapter
    : RecyclerView.Adapter<MessagesRecyclerAdapter.MessagesViewHolder>() {

    private var messagesList = ArrayList<Messages>()
    private var firebaseAuth = FirebaseAuth.getInstance()


    inner class MessagesViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Setting Click listener for the recycler items
        init {
            itemView.setOnClickListener {

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val groupName = messagesList[pos]
                    //mListener.onRecyclerItemClick(groupName)
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessagesViewHolder {

        return MessagesViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.text_messages_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (messagesList.size > 0) messagesList.size else 0
    }

    override fun onBindViewHolder(holder: MessagesViewHolder, position: Int) {

        val currentUserID = firebaseAuth.currentUser?.uid

        val message = messagesList[position]

        val messageIsFrom = message.from
        val messageType = message.type

        holder.itemView.apply {

            messagesItemReceivedLayoutId.visibility = View.GONE
            messagesItemSendLayoutId.visibility = View.GONE

            if (messageType == MSG_TYPE_VALUE_TEXT_MESSAGE){

                if (messageIsFrom == currentUserID){

                    //If the message is from currentUserId that means current user has send a message to someone
                    messagesItemSendLayoutId.visibility = View.VISIBLE
                    ivMessageItemReceived.visibility = View.GONE
                    tvMsgItemSent.visibility = View.VISIBLE
                    tvMsgItemSendDateTime.visibility = View.VISIBLE

                    tvMsgItemSent.text = message.message
                    tvMsgItemSendDateTime.text = "${message.message_date},${message.message_time}"

                }else{

                    //If the message is from some other Id that means current user has received a message from someone.
                    messagesItemReceivedLayoutId.visibility = View.VISIBLE
                    tvMsgItemReceived.visibility = View.VISIBLE
                    ivMessageItemReceived.visibility = View.GONE
                    tvMsgItemReceivedDateTime.visibility = View.VISIBLE

                    tvMsgItemReceived.text = message.message
                    tvMsgItemReceivedDateTime.text = "${message.message_date},${message.message_time}"
                }

            }

            if(messageType == MSG_TYPE_VALUE_IMAGE_MESSAGE){

                if (messageIsFrom == currentUserID){

                    //If the message is from currentUserId that means current user has send a message to someone
                    messagesItemSendLayoutId.visibility = View.VISIBLE
                    tvMsgItemSent.visibility = View.GONE
                    ivMessageItemSent.visibility = View.VISIBLE
                    tvMsgItemSendDateTime.visibility = View.VISIBLE

                    val glideCustomization = RequestOptions()
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                    Glide
                        .with(context)
                        .load(message.message)
                        .apply(glideCustomization)
                        .into(ivMessageItemSent)
                    tvMsgItemSendDateTime.text = "${message.message_date},${message.message_time}"
                }else{

                    //If the message is from currentUserId that means current user has send a message to someone
                    messagesItemReceivedLayoutId.visibility = View.VISIBLE
                    tvMsgItemReceived.visibility = View.GONE
                    ivMessageItemReceived.visibility = View.VISIBLE
                    tvMsgItemReceivedDateTime.visibility = View.VISIBLE

                    val glideCustomization = RequestOptions()
                        .placeholder(R.drawable.default_profile_image)
                        .error(R.drawable.default_profile_image)
                    Glide
                        .with(context)
                        .load(message.message)
                        .apply(glideCustomization)
                        .into(ivMessageItemReceived)
                    tvMsgItemReceivedDateTime.text = "${message.message_date},${message.message_time}"
                }

            }

        }

    }

    fun submitMessagesList(mMessagesList: ArrayList<Messages>) {

        messagesList = mMessagesList
        notifyDataSetChanged()

    }

    interface MyGroupsAdapterItemClickInterface {
        fun onRecyclerItemClick(groupName: String)
    }

}