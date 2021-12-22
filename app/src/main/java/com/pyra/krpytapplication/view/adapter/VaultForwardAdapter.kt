package com.pyra.krpytapplication.view.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ItemContactBinding
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema

class VaultForwardAdapter : ListAdapter<ChatListSchema,VaultForwardAdapter.ChatListViewHolder>(DiffUtil) {



    class ChatListViewHolder(itemView: ItemContactBinding):RecyclerView.ViewHolder(itemView.root){
        var binding: ItemContactBinding = itemView
    }


    companion object{

      private  val DiffUtil = object :DiffUtil.ItemCallback<ChatListSchema>(){
          override fun areItemsTheSame(oldItem: ChatListSchema, newItem: ChatListSchema): Boolean {
              return oldItem.kryptId ==  newItem.kryptId
          }

          override fun areContentsTheSame(
              oldItem: ChatListSchema,
              newItem: ChatListSchema
          ): Boolean {
              return oldItem == newItem
          }


      }


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatListViewHolder {

        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_contact,
            parent,
            false
        )
        return ChatListViewHolder(binding as ItemContactBinding)

    }

    override fun onBindViewHolder(holder: ChatListViewHolder, position: Int) {

        val data = getItem(position)

        if(data.roomName != null && data.roomName != "" && data.roomName != "null"){
            holder.binding.userName.text = data.roomName
        }else{
            holder.binding.userName.text = data.kryptId
        }
        holder.binding.userImage.loadImage(getItem(position).roomImage)

        holder.binding.copyCode.hide()
    }
}