package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.openNewTaskActivity
import com.pyra.krpytapplication.view.adapter.AddContactListAdapter
import com.pyra.krpytapplication.view.adapter.AddParticipantKryptCodeAdapter
import com.pyra.krpytapplication.view.adapter.AddParticipantSelectedContactAdapter
import com.pyra.krpytapplication.viewmodel.AddMemberViewModel
import kotlinx.android.synthetic.main.activity_group_member_selection.*
import kotlinx.android.synthetic.main.contact_lists.*
import showToast

class AddGroupMemberActivity : BaseActivity() {

    private lateinit var chatListAdapter: AddContactListAdapter
    lateinit var kryptCodeAdapter: AddParticipantKryptCodeAdapter
    lateinit var selectedContactAdapter: AddParticipantSelectedContactAdapter
    var viewModel: AddMemberViewModel? = null
    var roomId = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_member_selection)
        viewModel = ViewModelProvider(this).get(AddMemberViewModel::class.java)

        getIntentValues()
        header.text = getString(R.string.add_member)
        initAdapter()
        initListeners()

    }

    private fun getIntentValues() {
        intent.extras?.let {
            roomId = it.getString(Constants.IntentKeys.ROOMID, "")
            viewModel?.setRoomId(roomId)
        }
    }

    private fun initListeners() {
        nextButton.setOnClickListener {
            if (viewModel?.selectedList?.size != 0) {
                viewModel?.addMembersToList(roomId)
            }

            viewModel?.groupCreated?.observe(this, Observer {
                if (it) {
                    openNewTaskActivity(MainActivity::class.java)
                }
            })

            viewModel?.errorMessage?.observe(this, Observer {
                showToast(it)

            })
        }
    }


    private fun initAdapter() {

        viewModel?.getNamedUser(roomId)
        viewModel?.updateNameList?.observe(this, Observer {
            if (viewModel?.namedContacts?.size == 0) {
                my_Contacts.visibility = View.GONE
            }
            chatListAdapter = AddContactListAdapter(this, viewModel) { position ->
                viewModel?.onItemAddContact(viewModel?.namedContacts?.get(position))
            }
            contactList.adapter = chatListAdapter
        })


        viewModel?.getUnnamedUser(roomId)
        viewModel?.updateUnnameList?.observe(this, Observer {

            if (viewModel?.unNamedContacts?.size == 0) {
                krypt_code_title.visibility = View.GONE
            }
            kryptCodeAdapter = AddParticipantKryptCodeAdapter(this, viewModel) { position ->
                viewModel?.onItemAddContact(viewModel?.unNamedContacts?.get(position))
            }
            kryptLists.adapter = kryptCodeAdapter

        })


        val selectedContactLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)


        selectedContactAdapter = AddParticipantSelectedContactAdapter(this, viewModel) { position ->
            viewModel?.removeSelectedUser(position)
        }
        selectedContactLists.layoutManager = selectedContactLayoutManager
        selectedContactLists.adapter = selectedContactAdapter


        viewModel?.selectedListUpdate?.observe(this, Observer {
            selectedContactAdapter.notifyChanges()
        })
    }


}
