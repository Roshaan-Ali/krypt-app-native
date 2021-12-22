package com.pyra.krpytapplication.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ChildBlockListBinding
import com.pyra.krpytapplication.viewmodel.ProfileViewModel

class BlockedListAdapter(
    private val context: Activity,
    var viewModel: ProfileViewModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.child_block_list,
            parent,
            false
        )
        return MyViewHolder(binding as ChildBlockListBinding)
    }

    override fun getItemCount(): Int {
        return viewModel?.blockedUsers?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder

        viewHolder.binding.userName.text = viewModel?.getBlockedUserName(position)?.toLowerCase()
        viewHolder.binding.userImage.loadImage(viewModel?.getBlockedUserImage(position))

        viewHolder.binding.unblock.setOnClickListener {
            viewModel?.unblockUser(position)
        }
    }

    inner class MyViewHolder(itemView: ChildBlockListBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ChildBlockListBinding = itemView

    }

}

