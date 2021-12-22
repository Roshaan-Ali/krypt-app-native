package com.pyra.krpytapplication.view.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.databinding.ChildBurnMsgUnitBinding
import java.util.*

class BurnMsgUnitAdapter(var context: Context) :
    RecyclerView.Adapter<BurnMsgUnitAdapter.BurnMsgUnitViewHolder>() {

    var unitList = ArrayList(listOf("Seconds", "Minutes", "Hours", "Days"))

    class BurnMsgUnitViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        var childBurnMsgUnitBinding: ChildBurnMsgUnitBinding = ChildBurnMsgUnitBinding.bind(view)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BurnMsgUnitViewHolder {
        return BurnMsgUnitViewHolder(
            LayoutInflater.from(context).inflate(R.layout.child_burn_msg_unit, parent, false)
        )
    }

    override fun getItemCount(): Int {
        return unitList.size
    }

    override fun onBindViewHolder(holder: BurnMsgUnitViewHolder, position: Int) {

        holder.childBurnMsgUnitBinding.burnMsgCountUnit.text = unitList[position]

    }
}