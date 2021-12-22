package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.openNewTaskActivity
import com.pyra.krpytapplication.utils.show
import com.pyra.krpytapplication.view.adapter.ForwardAdapter
import com.pyra.krpytapplication.viewmodel.ForwardViewModel
import kotlinx.android.synthetic.main.activity_forward.*

class ForwardActivity : BaseActivity() {

    var viewModel: ForwardViewModel? = null
    var adapter: ForwardAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_forward)
        viewModel = ViewModelProvider(this).get(ForwardViewModel::class.java)
        initObservers()
        initView()
        getMessageList()
        initListeners()
    }

    private fun initListeners() {

        backButton.setOnClickListener { onBackPressed() }

        forward.setOnClickListener {
            viewModel?.forwardmessage()
        }
    }

    private fun getMessageList() {
        intent.extras?.let {
            it.getStringArrayList("list")?.let { list ->
                viewModel?.getRawMessage(list)
            }
        }
    }

    private fun initView() {

        viewModel?.getChatList()

        adapter = ForwardAdapter(this, viewModel)
        contactList.layoutManager = LinearLayoutManager(this)
        contactList.adapter = adapter
    }

    private fun initObservers() {

        viewModel?.notifyData?.observe(this, Observer {
            adapter?.notifyDataChanged()
        })

        viewModel?.messageForwarded?.observe(this, Observer {
           openNewTaskActivity(MainActivity::class.java)
        })

        viewModel?.isUsersSelected?.observe(this, Observer {
            if (it) {
                forward.show()
            } else {
                forward.hide()
            }
        })


    }

}