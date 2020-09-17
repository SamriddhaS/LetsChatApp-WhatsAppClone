package com.samriddha.letschartapp.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.firebase.ui.database.FirebaseRecyclerAdapter
import com.firebase.ui.database.FirebaseRecyclerOptions
import com.samriddha.letschartapp.R
import com.samriddha.letschartapp.models.Contacts
import kotlinx.android.synthetic.main.show_users_item.view.*

class FirebaseRecFindFriendsAdapter(options:FirebaseRecyclerOptions<Contacts>
                                    , val mListener:FirebaseRecFindFriendsAdapter.FirebaseRecAdapterInterface):
    FirebaseRecyclerAdapter<Contacts, FirebaseRecFindFriendsAdapter.FirebaseRecViewHolder>(options) {

    inner class FirebaseRecViewHolder(itemView:View):RecyclerView.ViewHolder(itemView){
        init {
            itemView.setOnClickListener {

                mListener.onUserItemClickListener(getRef(adapterPosition).key.toString())
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FirebaseRecViewHolder {
        return FirebaseRecViewHolder(LayoutInflater
            .from(parent.context)
            .inflate(R.layout.show_users_item,parent,false))
    }

    override fun onBindViewHolder(holder: FirebaseRecViewHolder, position: Int, model: Contacts) {

        holder.itemView.apply {

            tvShowUserItemName.text = model.name
            tvShowUserItemAbout.text = model.about

            val glideCustomization = RequestOptions()
                .placeholder(R.drawable.default_profile_image)
                .error(R.drawable.default_profile_image)

            Glide
                .with(context)
                .load(model.profile_pic)
                .apply(glideCustomization)
                .into(ivShowUserItemDp)

        }

    }

    interface FirebaseRecAdapterInterface{
        fun onUserItemClickListener(userId:String)
    }
}