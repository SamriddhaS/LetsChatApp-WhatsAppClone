package com.samriddha.letschartapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.samriddha.letschartapp.R
import kotlinx.android.synthetic.main.my_groups_item.view.*
import java.util.zip.Inflater

class MyGroupsAdapter(private val mListener:MyGroupsAdapterItemClickInterface)
    : RecyclerView.Adapter<MyGroupsAdapter.MyGroupsViewHolder>() {

    var groupList = ArrayList<String>()

    inner class MyGroupsViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        //Setting Click listener for the recycler items
        init {
            itemView.setOnClickListener {

                val pos = adapterPosition
                if (pos != RecyclerView.NO_POSITION) {
                    val groupName = groupList[pos]
                    mListener.onRecyclerItemClick(groupName)
                }

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MyGroupsViewHolder {
        return MyGroupsViewHolder(
            LayoutInflater.from(parent.context).inflate(
                R.layout.my_groups_item,
                parent,
                false
            )
        )
    }

    override fun getItemCount(): Int {
        return if (groupList.size > 0) groupList.size else 0
    }

    override fun onBindViewHolder(holder: MyGroupsViewHolder, position: Int) {

        holder.itemView.apply {

            myGroupsItemName.text = groupList[position]

        }

    }

    fun submitGroupList(mGroupList: ArrayList<String>) {
        groupList.clear()
        groupList = mGroupList
        notifyDataSetChanged()

    }

    interface MyGroupsAdapterItemClickInterface {
        fun onRecyclerItemClick(groupName: String)
    }

}