package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ItemGroupMembersBinding
import com.pyra.krpytapplication.viewmodel.ProfileViewModel


class GroupMembersAdapter(
    private val context: Context,
    var profileViewModel: ProfileViewModel,
    var onLongClick: (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_group_members,
            parent,
            false
        )
        return MyViewHolder(binding as ItemGroupMembersBinding)

    }

    override fun getItemCount(): Int {
        return profileViewModel.participationList.size
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder

        viewHolder.binding.userImage.loadImage(profileViewModel.getParticipationImage(position))
        viewHolder.binding.userName.text = profileViewModel.getParticipationName(position)

        viewHolder.binding.root.setOnLongClickListener {

            if (profileViewModel.isMemberRemoveable(position)) {
                onLongClick(position)
                false
            } else {
                false
            }

        }

    }

    inner class MyViewHolder(itemView: ItemGroupMembersBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemGroupMembersBinding

        init {
            binding = itemView
        }
    }
}
