package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ItemSelectedContactBinding
import com.pyra.krpytapplication.viewmodel.CreateRoomViewModel

class SelectedUsersAdapter(
    private val context: Context,
    var viewModel: CreateRoomViewModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_selected_contact,
            parent,
            false
        )
        return MyViewHolder(binding as ItemSelectedContactBinding)
    }

    override fun getItemCount(): Int {
        return viewModel?.selectedList?.size ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as MyViewHolder

        holder.binding.contactName.text = viewModel?.getSelectedName(position)
        holder.binding.profileImage.loadImage(viewModel?.getSelectedImage(position))

        holder.binding.remove.setOnClickListener { 
            viewModel?.removeUser(position)
        }
       
    }

    fun notifyChanges() {
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: ItemSelectedContactBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemSelectedContactBinding = itemView

    }

}

