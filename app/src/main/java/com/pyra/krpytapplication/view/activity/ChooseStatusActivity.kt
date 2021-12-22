package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.view.adapter.StatusAdapter
import com.pyra.krpytapplication.viewmodel.ChooseStatusViewModel
import kotlinx.android.synthetic.main.activity_choose_status.*

class ChooseStatusActivity : BaseActivity() {
    private lateinit var chooseStatusViewModel: ChooseStatusViewModel
    lateinit var statusAdapter: StatusAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_choose_status)
        chooseStatusViewModel = ViewModelProvider(this).get(ChooseStatusViewModel::class.java)
        status.text = chooseStatusViewModel.getCurrentStatusName()
        initStatusAdapter()
    }


    fun onBackButtonPressed(view: View) {
        finish()
    }

    fun onSaveButtonPressed(view: View) {
        chooseStatusViewModel.changeStatus()
        chooseStatusViewModel.success.observe(this, Observer {
            if (it) {
                finish()
            }
        })
    }

    private fun initStatusAdapter() {
        val linearLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        statusAdapter = StatusAdapter(this, chooseStatusViewModel)
        statusRecyclerView.layoutManager = linearLayoutManager
        statusRecyclerView.adapter = statusAdapter
    }

}
