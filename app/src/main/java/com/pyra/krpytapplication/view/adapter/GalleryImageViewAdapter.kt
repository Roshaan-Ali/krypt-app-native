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
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.loadImage
import com.pyra.krpytapplication.utils.show
import com.pyra.krpytapplication.databinding.ItemVaultBinding
import com.pyra.krpytapplication.view.activity.ImageAndVideoViewer
import com.pyra.krpytapplication.viewmodel.GalleryViewModel

class GalleryImageViewAdapter(
    private val context: Context,
    private val isVideoValue: Boolean,
    private val viewModel: GalleryViewModel,
    private val onClick : (Int) -> Unit
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_vault,
            parent,
            false
        )
        return MyViewHolder(binding as ItemVaultBinding)

    }

    override fun getItemCount(): Int {
        return viewModel.getImageCount(isVideoValue) ?: 0
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder
        if (isVideoValue) {
            viewHolder.setForVideo()
        } else {
            viewHolder.setForImage()
        }

        holder.itemView.setOnClickListener {
            val intent = Intent(context, ImageAndVideoViewer::class.java)
            intent.putExtra(
                Constants.IntentKeys.CONTENT,
                viewModel.getThumpImage(position, isVideoValue)
            )
            if (isVideoValue) {
                intent.putExtra(Constants.IntentKeys.ISVIDEO, true)
            } else {
                intent.putExtra(Constants.IntentKeys.ISVIDEO, false)
            }
            context.startActivity(intent)
        }

        holder.binding.selectedView.hide()
        holder.binding.selectedImage.hide()


        holder.itemView.setOnClickListener {
            onClick(position)
        }



        holder.binding.thumpImage.loadImage(viewModel.getThumpImage(position,isVideoValue))

    }

    inner class MyViewHolder(itemView: ItemVaultBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemVaultBinding = itemView

    }


    private fun GalleryImageViewAdapter.MyViewHolder.setForVideo() {
        this.binding.isVideo.show()
    }

    private fun GalleryImageViewAdapter.MyViewHolder.setForImage() {
        this.binding.isVideo.hide()
    }

    fun notifyDataChanged() {
        notifyDataSetChanged()
    }
}