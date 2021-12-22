package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.MessageBurnType
import com.pyra.krpytapplication.databinding.ChildCountBinding

class CountAdapter(var context: Context, var type: String) :
    RecyclerView.Adapter<CountAdapter.CountViewHolder>() {

    class CountViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var childCountBinding: ChildCountBinding = ChildCountBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CountViewHolder {
        return CountViewHolder(
            LayoutInflater.from(context).inflate(R.layout.child_count, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return when (type) {
            MessageBurnType.MINUTES.type -> 60
            MessageBurnType.HOURS.type -> 60
            MessageBurnType.DAYS.type -> 30
            MessageBurnType.SECONDS.type -> 60
            else -> 0
        }
    }

    override fun onBindViewHolder(holder: CountViewHolder, position: Int) {

        holder.childCountBinding.count.text = (position + 1).toString()

    }
}