package com.samriddha.letschartapp.ui

import android.content.Intent
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
import com.samriddha.letschartapp.adapters.FirebaseRecChatsAdapter
import com.samriddha.letschartapp.adapters.FirebaseRecContactsAdapter
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_ALL_CONTACTS
import com.samriddha.letschartapp.others.Constants.KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import com.samriddha.letschartapp.others.Constants.KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY
import kotlinx.android.synthetic.main.fragment_chats.*
import kotlinx.android.synthetic.main.fragment_contacts.*

class ChatsFragment : Fragment(),FirebaseRecChatsAdapter.FirebaseRecAdapterInterface {

    private var firebaseDbRefChats: DatabaseReference? = null
    private var currentUserId = ""


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_chats, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        currentUserId = FirebaseAuth.getInstance().currentUser?.uid.toString()
        firebaseDbRefChats = FirebaseDatabase.getInstance().reference.child(DATABASE_PATH_ALL_CONTACTS).child(currentUserId)

        chatsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        chatsRecyclerView.setHasFixedSize(true)

        val options = firebaseDbRefChats
            ?.let {
                FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(it, Contacts::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()
            }

        val firebaseRecAdapter = FirebaseRecChatsAdapter(options!!,requireContext(),this)
        chatsRecyclerView.adapter = firebaseRecAdapter

    }

    override fun onChatItemClickListener(userId: String, userName: String, userImage: String) {
        startActivity(Intent(requireContext(),PrivateChatActivity::class.java)
            .putExtra(KEY_USER_ID_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY,userId)
            .putExtra(KEY_USER_NAME_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY,userName)
            .putExtra(KEY_USER_IMAGE_CHAT_FRAGMENT_TO_PRIVATE_CHAT_ACTIVITY,userImage))
    }


}