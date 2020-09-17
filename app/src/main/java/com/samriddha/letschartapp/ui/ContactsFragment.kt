package com.samriddha.letschartapp.ui

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.FirebaseRecContactsAdapter
import com.samriddha.letschartapp.adapters.FirebaseRecFindFriendsAdapter
import com.samriddha.letschartapp.models.Contacts
import com.samriddha.letschartapp.others.Constants
import kotlinx.android.synthetic.main.fragment_contacts.*

class ContactsFragment : Fragment(),FirebaseRecContactsAdapter.FirebaseRecAdapterInterface{

    private var firebaseDbRefAllContacts: DatabaseReference? = null
    private var firebaseAuth:FirebaseAuth? = null
    private var currentUserId = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_contacts, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseAuth = FirebaseAuth.getInstance()
        firebaseDbRefAllContacts = FirebaseDatabase.getInstance().reference.child(Constants.DATABASE_PATH_ALL_CONTACTS)
        currentUserId = firebaseAuth?.currentUser?.uid.toString()

        initRecyclerView()

    }


    private fun initRecyclerView() {

        //setting Recycler View
        contactsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        contactsRecyclerView.setHasFixedSize(true)

        /*This will have reference to firebaseDb and model class that will be mapped with data from firebaseDb*/
        val options = firebaseDbRefAllContacts
            ?.child(currentUserId)
            ?.let {
                FirebaseRecyclerOptions.Builder<Contacts>()
                    .setQuery(it, Contacts::class.java)
                    .setLifecycleOwner(viewLifecycleOwner)
                    .build()
            }

        /*Firebase Recycler View Adapter will take all the user data from firebase db and map it to Contacts object.*/
        val firebaseRecAdapter = FirebaseRecContactsAdapter(options!!,requireContext())
        contactsRecyclerView.adapter = firebaseRecAdapter

    }

    override fun onUserItemClickListener(userId: String) {

    }


}