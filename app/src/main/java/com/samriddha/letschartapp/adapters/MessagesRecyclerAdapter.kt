package com.samriddha.letschartapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Messages
import com.samriddha.letschartapp.others.Constants.MSG_TYPE_VALUE_TEXT_MESSAGE
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

            if (messageType == MSG_TYPE_VALUE_TEXT_MESSAGE){

                tvMsgItemReceived.visibility = View.INVISIBLE
                tvMsgItemSent.visibility = View.INVISIBLE

                if (messageIsFrom == currentUserID){

                    //If the message is from currentUserId that means current user has send a message to someone
                    tvMsgItemSent.visibility = View.VISIBLE
                    tvMsgItemSent.text = message.message

                }else{

                    //If the message is from some other Id that means current user has received a message from someone.
                    tvMsgItemReceived.visibility = View.VISIBLE
                    tvMsgItemReceived.text = message.message
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