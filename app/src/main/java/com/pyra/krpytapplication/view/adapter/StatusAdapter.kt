package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.databinding.ItemStatusBinding
import com.pyra.krpytapplication.viewmodel.ChooseStatusViewModel


class StatusAdapter(
    private val context: Context,
    chooseStatusViewModel: ChooseStatusViewModel
) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var viewModel: ChooseStatusViewModel
    init {
        this.viewModel = chooseStatusViewModel
    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val layoutInflater = LayoutInflater.from(context)
        val binding = DataBindingUtil.inflate<ViewDataBinding>(
            layoutInflater,
            R.layout.item_status,
            parent,
            false
        )
        return MyViewHolder(binding as ItemStatusBinding)
    }

    override fun getItemCount(): Int {
        return this.viewModel.numberOfStatuses()
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val viewHolder: MyViewHolder = holder as MyViewHolder
        viewHolder.binding.status.text = viewModel.getStatusName(position)

        when (viewModel.isStatusSelected(position)) {
            true -> viewHolder.binding.selectedIcon.setImageResource(R.drawable.selected_icon)
            false -> viewHolder.binding.selectedIcon.setImageResource(R.drawable.unselected_icon)
        }
        viewHolder.itemView.setOnClickListener {
            viewModel.setSelected(position)
            notifyDataSetChanged()
        }

    }

    inner class MyViewHolder(itemView: ItemStatusBinding) :
        RecyclerView.ViewHolder(itemView.root) {
        var binding: ItemStatusBinding

        init {
            binding = itemView
        }
    }
}
