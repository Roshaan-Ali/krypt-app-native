package com.pyra.krpytapplication.view.adapter

import android.app.Activity
import android.content.Intent
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.databinding.ItemDocumentsBinding
import com.pyra.krpytapplication.view.activity.DocumentActivity
import com.pyra.krpytapplication.view.activity.ImageAndVideoViewer
import com.pyra.krpytapplication.viewmodel.VaultFragViewModel
import getDocumentIcon
import getViewIntent
import java.io.File

class DocumentListAdapter(
    private val fragment: Fragment,
    private val context: Activity,
    val viewModel: VaultFragViewModel?
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_documents,
            parent,
            false
        )
        return MyViewHolder(binding as ItemDocumentsBinding)
    }

    override fun getItemCount(): Int {
        return viewModel?.getDocumentListSize() ?: 0
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {

        holder as MyViewHolder
        holder.binding.fileName.text = viewModel?.getDocFileName(position)
        holder.binding.fileDate.text = viewModel?.getDate(position)
        //holder.binding.fileType.text = viewModel?.getDocFileType(position)
        holder.binding.fileSize.text = viewModel?.getDocFileSize(position)

        holder.binding.fileIcon.setImageDrawable(
            ContextCompat.getDrawable(
                context,
                getDocumentIcon(viewModel?.getFile(position)?.getFileExtension())
            )
        )

        viewModel?.isDocSelected(position)?.let {
            if (it) {
                holder.binding.selectedView.show()
                holder.binding.selectedImage.show()
            } else {
                holder.binding.selectedView.hide()
                holder.binding.selectedImage.hide()
            }
        }

        holder.itemView.setOnClickListener {
            viewModel?.getIsMultipleDocumentSelectionEnabled()?.let {
                if (it) {
                    viewModel.selectMultiDocument(position)
                } else {
                    fragment.startActivityForResult(
                        Intent(context, DocumentActivity::class.java)
                            .putExtra("isNewDoc", false)
                            .putExtra("path", viewModel.getFilePath(position)),
                        Constants.RequestCode.DOC_INTENT
                    )
//                    openDocument(viewModel?.getFilePath(position), position)
                }
            }
        }

        holder.itemView.setOnLongClickListener {
            viewModel?.selectMultiDocument(position)
            true
        }
    }

    inner class MyViewHolder(itemView: ItemDocumentsBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemDocumentsBinding = itemView
    }

    fun openDocument(file: File?, position: Int) {

        file?.getDocumentType()?.let {
            when {
                it.contains("image", true) -> {
                    var intent = Intent(context, ImageAndVideoViewer::class.java)
                    intent.putExtra(
                        Constants.IntentKeys.CONTENT,
                        viewModel?.getMediaUrl(position)
                    )
                    intent.putExtra(Constants.IntentKeys.ISVIDEO, false)
                    context.startActivity(intent)
                }
                it.contains("video", true) -> {
                    var intent = Intent(context, ImageAndVideoViewer::class.java)
                    intent.putExtra(
                        Constants.IntentKeys.CONTENT,
                        viewModel?.getMediaUrl(position)
                    )
                    intent.putExtra(Constants.IntentKeys.ISVIDEO, true)
                    context.startActivity(intent)
                }
                else -> {


                    var intent = getViewIntent(
                        FileProvider.getUriForFile(
                            context,
                            context.applicationContext.packageName + ".provider",
                            file
                        )
                    )

                    val chooser =
                        Intent.createChooser(intent, "Choose an application to open with:")
                    context.startActivity(chooser)

//                    try {
//                        val intent = Intent()
//                        intent.action = Intent.ACTION_VIEW
//                        intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
//                        intent.setDataAndType(
//                            FileProvider.getUriForFile(
//                                context,
//                                context.applicationContext.packageName + ".provider",
//                                file
//                            ), it
//                        )
//                        val chooser =
//                            Intent.createChooser(intent, "Choose an application to open with:")
//                        context.startActivity(chooser)
//                    } catch (e: Exception) {
//                        println(e.toString())
//                    }


                }
            }
        }

    }
}

