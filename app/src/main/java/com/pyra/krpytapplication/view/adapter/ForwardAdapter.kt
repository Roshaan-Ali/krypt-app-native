package com.pyra.krpytapplication.view.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.utils.show
import com.pyra.krpytapplication.databinding.ItemContactBinding
import com.pyra.krpytapplication.viewmodel.ForwardViewModel

class ForwardAdapter(
    var context: Activity,
    var viewModel: ForwardViewModel?
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {


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
        return viewModel?.chatList?.size ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder


        viewHolder.binding.userName.text = viewModel?.getUserName(position)
        viewHolder.binding.userImage.loadImage(viewModel?.getUserImage(position))

        viewHolder.itemView.setOnClickListener {
            viewModel?.setSelected(position)
        }

        viewModel?.isKryptContactSelected(position)?.let {
            if (it) {
                viewHolder.binding.selectedView.show()
                viewHolder.binding.selectedImage.show()
            } else {
                viewHolder.binding.selectedView.hide()
                viewHolder.binding.selectedImage.hide()
            }
        }


        viewHolder.itemView.setOnLongClickListener {
            viewModel?.setSelected(position)
            true
        }

        viewHolder.binding.copyCode.hide()
//        viewHolder.binding.copyCode.setOnClickListener {
//
//            val clipboard: ClipboardManager =
//                context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
//            val clip = ClipData.newPlainText("Code Copied", viewModel?.getUnNamedKryptId(position))
//            clipboard.setPrimaryClip(clip)
//            Toast.makeText(context, "Code Copied", Toast.LENGTH_SHORT).show()
//        }

    }

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }


    inner class MyViewHolder(itemView: ItemContactBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemContactBinding = itemView

    }

}