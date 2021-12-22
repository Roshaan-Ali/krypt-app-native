package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.Constants.IntentKeys.DISPLAY_NAME
import com.pyra.krpytapplication.utils.Constants.IntentKeys.ISGROUP
import com.pyra.krpytapplication.utils.Constants.IntentKeys.KRYPTKEY
import com.pyra.krpytapplication.utils.Constants.IntentKeys.ROOMID
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.utils.show
import com.pyra.krpytapplication.databinding.ItemChatsBinding
import com.pyra.krpytapplication.view.activity.ChatActivity
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import fetchThemeColor

class ChatListAdapter(
    private val context: Context,
    var viewModel: ChatListViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_chats,
            parent,
            false
        )
        return MyViewHolder(binding as ItemChatsBinding)
    }

    override fun getItemCount(): Int {
        return viewModel.getNumberOfChatList()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder

        if (viewModel.getUnreadMessagesCount(position) == "") {
            viewHolder.binding.lastMsgTime.setTextColor(
                fetchThemeColor(
                    R.attr.sub_title_color,
                    context
                )
            )
            viewHolder.binding.unreadCountText.hide()
        } else {
            viewHolder.binding.lastMsgTime.setTextColor(
                fetchThemeColor(
                    R.attr.unread_green_color,
                    context
                )
            )
            viewHolder.binding.unreadCountText.show()
        }
        viewHolder.binding.lastMessage.text = viewModel.getLastMessage(position)
        viewHolder.binding.lastMsgTime.text = viewModel.getLastMessageTime(position)
        viewHolder.binding.removeViewCheck.hide()
        viewHolder.binding.unreadCountText.text = viewModel.getUnreadMessagesCount(position)
        viewHolder.binding.userName.text = viewModel.getChatListDisplayName(position)?.capitalize()
        viewHolder.binding.userImage.loadImage(viewModel.getChatListImage(position))

        viewHolder.itemView.setOnClickListener {
            if (viewModel.isMultiSelectionEnabled) {
                viewModel.makeSelection(position)
            } else {
                val intent = Intent(context, ChatActivity::class.java)
                intent.putExtra(KRYPTKEY, viewModel.getChatListKryptId(position))
                intent.putExtra(ROOMID, viewModel.getChatListRoomId(position))
                intent.putExtra(ISGROUP, viewModel.isGroupChat(position))
                intent.putExtra(DISPLAY_NAME, viewModel.getChatListDisplayName(position))
                intent.putExtra(
                    Constants.IntentKeys.IS_ADDED_TO_CONTACTS,
                    viewModel.isAlreadyAddedToContacts(position)
                )
                context.startActivity(intent)
            }
        }

        viewHolder.itemView.setOnLongClickListener {
            viewModel.makeSelection(position)
            true
        }

        if (viewModel.getIsSelected(position)) {
            holder.binding.parentView.setBackgroundColor(
                ContextCompat.getColor(
                    context,
                    R.color.selectedChatColor
                )
            )
            holder.binding.chatSelected.show()
        } else {
            holder.binding.parentView.setBackgroundColor(
                fetchThemeColor(R.attr.page_default_bg, context)
            )
            holder.binding.chatSelected.hide()
        }

    }

    inner class MyViewHolder(itemView: ItemChatsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemChatsBinding = itemView
    }

}