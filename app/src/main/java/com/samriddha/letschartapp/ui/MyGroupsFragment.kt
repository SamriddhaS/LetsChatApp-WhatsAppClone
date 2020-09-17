package com.samriddha.letschartapp.ui

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.database.*
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.adapters.MyGroupsAdapter
import com.samriddha.letschartapp.others.Constants.DATABASE_PATH_GROUPS
import com.samriddha.letschartapp.others.Constants.KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY
import kotlinx.android.synthetic.main.fragment_my_groups.*

class MyGroupsFragment : Fragment(),MyGroupsAdapter.MyGroupsAdapterItemClickInterface {

    private lateinit var adapter:MyGroupsAdapter
    private var firebaseDbRef: DatabaseReference? =null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_my_groups, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        firebaseDbRef = FirebaseDatabase.getInstance().reference.child(DATABASE_PATH_GROUPS)

        initRecyclerView()

        fetchGroupsData()
    }

    private fun initRecyclerView() {

        adapter = MyGroupsAdapter(this)
        myGroupsRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        myGroupsRecyclerView.setHasFixedSize(true)
        myGroupsRecyclerView.adapter = adapter
    }

    private fun fetchGroupsData() {

        firebaseDbRef?.addValueEventListener(object : ValueEventListener{

            override fun onCancelled(error: DatabaseError) {

            }

            override fun onDataChange(snapshot: DataSnapshot) {

                val set = HashSet<String>()
                val iterator = snapshot.children.iterator()

                while (iterator.hasNext()){
                    set.add(iterator.next().key!!)
                }

                val listGroups = ArrayList<String>()
                listGroups.addAll(set)

                adapter.submitGroupList(listGroups)
            }

        })



    }

    override fun onRecyclerItemClick(groupName: String) {
        startActivity(Intent(activity,GroupChatActivity::class.java).putExtra(KEY_MY_GROUPS_TO_GROUP_CHAT_ACTIVITY,groupName))
    }

}