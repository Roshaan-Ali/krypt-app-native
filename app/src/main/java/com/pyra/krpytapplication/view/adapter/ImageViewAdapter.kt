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
import com.pyra.krpytapplication.view.activity.EditImageActivity
import com.pyra.krpytapplication.view.activity.ImageAndVideoViewer
import com.pyra.krpytapplication.viewmodel.VaultFragViewModel

class ImageViewAdapter(
    private val context: Context,
    private val isVideoValue: Boolean,
    val viewModel: VaultFragViewModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var isVideo: Boolean = isVideoValue

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
        return viewModel?.getImageCount(isVideoValue) ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder
        if (isVideo) {
            viewHolder.setForVideo()
        } else {
            viewHolder.setForImage()
        }

        holder.itemView.setOnClickListener {

            val intent = Intent(context, ImageAndVideoViewer::class.java)
            intent.putExtra(
                Constants.IntentKeys.CONTENT,
                viewModel?.getThumpImage(position, isVideoValue)
            )

            if (isVideo) {
                viewModel?.getIsMultipleVideoSelectionEnabled()?.let {
                    if (it) {
                        viewModel.selectMultiVideo(position)
                    } else {
                        intent.putExtra(Constants.IntentKeys.ISVIDEO, true)
                        context.startActivity(intent)
                    }
                }
            } else {
                viewModel?.getIsMultipleImageSelectionEnabled()?.let {
                    if (it) {
                        viewModel.selectMultiImage(position)
                    } else {
                        intent.putExtra(Constants.IntentKeys.ISVIDEO, false)
                        context.startActivity(intent)
                    }
                }
            }

        }

        viewModel?.getIsSelected(position, isVideo)?.let {
            if (it) {
                holder.binding.selectedView.show()
                holder.binding.selectedImage.show()
                holder.binding.editImage.hide()
            } else {
                holder.binding.selectedView.hide()
                holder.binding.selectedImage.hide()
                if (isVideo) {
                    holder.binding.editImage.hide()
                } else {
                    holder.binding.editImage.show()
                }

            }
        }

        holder.binding.editImage.setOnClickListener {

            context.startActivity(
                Intent(context, EditImageActivity::class.java)
                    .putExtra("imageUrl", viewModel?.getLocalPath(position, false))
            )
        }

        holder.itemView.setOnLongClickListener {
            if (isVideo)
                viewModel?.selectMultiVideo(position)
            else
                viewModel?.selectMultiImage(position)
            true
        }


        holder.binding.thumpImage.loadImage(viewModel?.getThumpImage(position, isVideoValue))
    }

    inner class MyViewHolder(itemView: ItemVaultBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemVaultBinding = itemView
    }
}

private fun ImageViewAdapter.MyViewHolder.setForVideo() {
    this.binding.isVideo.show()
}

private fun ImageViewAdapter.MyViewHolder.setForImage() {
    this.binding.isVideo.hide()
}
