package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R

class ChatThemeListAdapter(
    private val context: Context,
    var colorList: ArrayList<String>,
    var selectedColor: String,
    val setColor: (String) -> Unit
) : RecyclerView.Adapter<ChatThemeListAdapter.Viewholder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ChatThemeListAdapter.Viewholder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_color, parent, false)
        return Viewholder(itemView)
    }

    override fun getItemCount(): Int {
        return colorList.size
    }

    override fun onBindViewHolder(holder: ChatThemeListAdapter.Viewholder, position: Int) {

        holder.chatThemeColor.setCardBackgroundColor(Color.parseColor(colorList[position]))
        if (selectedColor.equals("", ignoreCase = true) && position == 0) {
            holder.selectedItem.visibility = View.VISIBLE
        } else if (selectedColor.equals(colorList[position], ignoreCase = true)) {
            holder.selectedItem.visibility = View.VISIBLE
        } else {
            holder.selectedItem.visibility = View.INVISIBLE
        }

        holder.chatThemeColor.setOnClickListener {
            selectedColor = colorList[position]
            setColor(selectedColor)
            notifyDataSetChanged()
        }

    }

    inner class Viewholder(view: View) : RecyclerView.ViewHolder(view) {
        var chatThemeColor: CardView
        var selectedItem: View

        init {
            // Define click listener for the ViewHolder's View.
            chatThemeColor = view.findViewById(R.id.chatColor)
            selectedItem = view.findViewById(R.id.selectedItem)
        }
    }
}

