package com.pyra.krpytapplication.view.activity

import android.os.Bundle
import android.view.View
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.openActivity
import com.pyra.krpytapplication.view.adapter.ContactListAdapter
import com.pyra.krpytapplication.view.adapter.KryptCodeAdapter
import com.pyra.krpytapplication.view.adapter.SelectedContactAdapter
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import kotlinx.android.synthetic.main.activity_group_member_selection.*
import kotlinx.android.synthetic.main.contact_lists.*

class GroupMemberSelectionActivity : BaseActivity() {

    private lateinit var chatListAdapter: ContactListAdapter
    lateinit var kryptCodeAdapter: KryptCodeAdapter
    lateinit var selectedContactAdapter: SelectedContactAdapter
    var chatListViewModel: ChatListViewModel? = null
    var groupType = "PRIVATE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_group_member_selection)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)

        getIntentValues()
        header.text = getString(R.string.new_group)
        initAdapter()
        initListeners()
    }

    private fun getIntentValues() {
        intent.extras?.let {

            groupType = it.getString(Constants.IntentKeys.GROUPTYPE, "PRIVATE")
        }
    }

    private fun initListeners() {
        nextButton.setOnClickListener {
            if (chatListViewModel?.selectedList?.size != 0) {
                chatListViewModel?.saveToSingleton(groupType)
                openActivity(NewGroupDetailsActivity::class.java)
            }

        }
    }

    private fun initAdapter() {

        chatListViewModel?.getNamedUser()
        chatListViewModel?.updateNameList?.observe(this, Observer {
            if (chatListViewModel?.namedContacts?.size == 0) {
                my_Contacts.visibility = View.GONE
            }
            chatListAdapter = ContactListAdapter(this, chatListViewModel, { position ->
                chatListViewModel?.onItemAddContact(chatListViewModel?.namedContacts?.get(position))
            }, { longClickposition ->

            })
            contactList.adapter = chatListAdapter
        })

        chatListViewModel?.getUnnamedUser()
        chatListViewModel?.updateUnnameList?.observe(this, Observer {

            if (chatListViewModel?.unNamedContacts?.size == 0) {
                krypt_code_title.visibility = View.GONE
            }
            kryptCodeAdapter = KryptCodeAdapter(this, chatListViewModel, { position ->
                chatListViewModel?.onItemAddContact(chatListViewModel?.unNamedContacts?.get(position))
            }, { longClickPosition ->

            })
            kryptLists.adapter = kryptCodeAdapter
        })

        val selectedContactLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)

        selectedContactAdapter = SelectedContactAdapter(this, chatListViewModel) { position ->
            chatListViewModel?.removeSelectedUser(position)
        }
        selectedContactLists.layoutManager = selectedContactLayoutManager
        selectedContactLists.adapter = selectedContactAdapter

        chatListViewModel?.selectedListUpdate?.observe(this, Observer {
            selectedContactAdapter.notifyChanges()
        })
    }

}
