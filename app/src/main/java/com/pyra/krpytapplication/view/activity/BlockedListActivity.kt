package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.view.adapter.BlockedListAdapter
import com.pyra.krpytapplication.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.activity_list_blocked_users.*

class BlockedListActivity : BaseActivity() {

    lateinit var viewModel: ProfileViewModel
    var adapter: BlockedListAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_list_blocked_users)

        viewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
        initAdapter()
        getBlockedList()
        initObservers()
        initListener()

    }

    private fun initListener() {

        backButton.setOnClickListener {
            onBackPressed()
        }
    }

    private fun initAdapter() {
        adapter = BlockedListAdapter(this, viewModel)
        blockedList.layoutManager = LinearLayoutManager(this)
        blockedList.adapter = adapter

    }

    private fun initObservers() {

        viewModel.updateBlockedUser?.observe(this, Observer {
            adapter?.notifyDataSetChanged()
        })
    }

    private fun getBlockedList() {
        viewModel.getBlockedUsers()
    }
}