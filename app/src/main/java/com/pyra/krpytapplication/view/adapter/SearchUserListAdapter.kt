package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.databinding.ItemContactBinding
import com.pyra.krpytapplication.repositories.interfaces.OnItemClickListener
import com.pyra.krpytapplication.view.activity.ChatActivity
import com.pyra.krpytapplication.viewmodel.SearchViewModel
import getRoomId

class SearchUserListAdapter(
    private val context: Context,
    private var searchList: SearchViewModel?,
    val onCLickListener: OnItemClickListener
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
        return searchList?.searchedData?.size ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder

        viewHolder.itemView.setOnClickListener {


            searchList?.getSearchUserName(position)?.let { kryptId ->
                onCLickListener.onClick(position)
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(Constants.IntentKeys.KRYPTKEY, kryptId)
                intent.putExtra(
                    Constants.IntentKeys.DISPLAY_NAME,
                    searchList?.getDisplayName(position)
                )
                intent.putExtra(Constants.IntentKeys.ROOMID, getRoomId(context, kryptId))
                intent.putExtra(Constants.IntentKeys.ISGROUP, false)
                intent.putExtra(
                    Constants.IntentKeys.IS_ADDED_TO_CONTACTS,
                    searchList?.getIsAddContactName(position)
                )
                context.startActivity(intent)
            }

        }

        viewHolder.binding.userName.text = searchList?.getDisplayName(position)
        holder.binding.userImage.loadImage(searchList?.getImage(position))

    }

    inner class MyViewHolder(itemView: ItemContactBinding) :
        RecyclerView.ViewHolder(itemView.root) {

        var binding: ItemContactBinding = itemView

    }

}

