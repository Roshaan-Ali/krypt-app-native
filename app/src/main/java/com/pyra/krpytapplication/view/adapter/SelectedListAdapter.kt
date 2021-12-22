package com.pyra.krpytapplication.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ItemContactBinding
import com.pyra.krpytapplication.viewmodel.ChatListViewModel

class SelectedListAdapter(
    private val context: Activity,
    var viewModel: ChatListViewModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_contact,
            parent,
            false
        )
        return MyViewHolder(binding as ItemContactBinding)
    }

    override fun getItemCount(): Int {
        return viewModel?.selectedList?.size ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder

        viewHolder.binding.userName.text = viewModel?.getSelectedName(position)
        viewHolder.binding.userImage.loadImage(viewModel?.getSelectedImage(position))

    }

    inner class MyViewHolder(itemView: ItemContactBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemContactBinding = itemView

    }

}

