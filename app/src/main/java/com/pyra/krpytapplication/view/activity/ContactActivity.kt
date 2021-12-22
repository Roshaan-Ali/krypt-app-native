package com.pyra.krpytapplication.view.activity

import android.app.Dialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.hide
import com.pyra.krpytapplication.utils.show
import com.pyra.krpytapplication.databinding.ActivityContactListBinding
import com.pyra.krpytapplication.repositories.interfaces.OnItemClickListener
import com.pyra.krpytapplication.view.adapter.ContactListAdapter
import com.pyra.krpytapplication.view.adapter.KryptCodeAdapter
import com.pyra.krpytapplication.view.adapter.SearchUserListAdapter
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import com.pyra.krpytapplication.viewmodel.SearchViewModel
import kotlinx.android.synthetic.main.activity_contact_list.*

class ContactActivity : BaseActivity() {

    lateinit var chatBinding: ActivityContactListBinding
    private lateinit var chatListAdapter: ContactListAdapter
    lateinit var kryptCodeAdapter: KryptCodeAdapter

    var searchViewModel: SearchViewModel? = null
    var chatListViewModel: ChatListViewModel? = null
    var searchAdapter: SearchUserListAdapter? = null
    var selectedType = "PRIVATE"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_contact_list)

        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)
        initAdapter()
        listener()
    }

    private fun initAdapter() {

        chatListViewModel?.getNamedUser()
        chatListViewModel?.updateNameList?.observe(this, Observer {
            checkForDeleteConstact()
            if (chatListViewModel?.namedContacts?.size == 0) {
                contactsTitle.visibility = View.GONE
            }


            chatListAdapter = ContactListAdapter(this, chatListViewModel, { position ->

                chatListViewModel?.isContactSelectionEnabled()?.let {
                    if (it) {
                        chatListViewModel?.setContactSelected(position)

                    } else {
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra(
                            Constants.IntentKeys.KRYPTKEY,
                            chatListViewModel?.getNamedKryptId(position)
                        )
                        intent.putExtra(
                            Constants.IntentKeys.ROOMID,
                            chatListViewModel?.getNamedRoomId(position)
                        )
                        intent.putExtra(
                            Constants.IntentKeys.ISGROUP,
                            chatListViewModel?.getNamedisGroupChat(position)!!
                        )

                        intent.putExtra(
                            Constants.IntentKeys.DISPLAY_NAME,
                            chatListViewModel?.getNamedDisplayName(position)
                        )
                        intent.putExtra(Constants.IntentKeys.IS_ADDED_TO_CONTACTS, true)
                        startActivity(intent)
                        finish()
                    }
                }

            }, { longClickposition ->
                chatListViewModel?.setContactSelected(longClickposition)
            })
            contactList.adapter = chatListAdapter
        })

        chatListViewModel?.getUnnamedUser()
        chatListViewModel?.updateUnnameList?.observe(this, Observer {
            checkForDeleteConstact()
            if (chatListViewModel?.unNamedContacts?.size == 0) {
                krypt_code_title.visibility = View.GONE
            }
            kryptCodeAdapter = KryptCodeAdapter(this, chatListViewModel, { position ->

                chatListViewModel?.isContactSelectionEnabled()?.let {

                    if (it) {
                        chatListViewModel?.setKryptContactSelected(position)
                    } else {
                        val intent = Intent(this, ChatActivity::class.java)
                        intent.putExtra(
                            Constants.IntentKeys.KRYPTKEY,
                            chatListViewModel?.getUnNamedKryptId(position)
                        )

                        intent.putExtra(
                            Constants.IntentKeys.ROOMID,
                            chatListViewModel?.getUnNamedRoomId(position)
                        )
                        intent.putExtra(
                            Constants.IntentKeys.ISGROUP,
                            chatListViewModel?.getUnNamedisGroupChat(position)!!
                        )

                        intent.putExtra(Constants.IntentKeys.IS_ADDED_TO_CONTACTS, false)
                        intent.putExtra(
                            Constants.IntentKeys.DISPLAY_NAME,
                            chatListViewModel?.getUnNamedKryptId(position)
                        )
                        startActivity(intent)
                        finish()
                    }
                }


            }, { longClickPosition ->
                chatListViewModel?.setKryptContactSelected(longClickPosition)
            })
            kryptLists.adapter = kryptCodeAdapter

        })
        val contactListLayoutManager =
            LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)
        val kryptListLayoutManager = LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false)

        contactList.layoutManager = contactListLayoutManager
        kryptLists.layoutManager = kryptListLayoutManager
    }

    private fun listener() {

        searchBox.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(p0: Editable?) {

                p0.toString().let {
                    if (it == "") {
                        searchViewModel?.emptyData()
                    } else {
                        searchUser(it)
                    }
                }


            }

            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}

        })

        backPressed.setOnClickListener {
            chatListViewModel?.unselectContact()
        }

        edit.setOnClickListener {

            showEditDialog()
        }

        delete.setOnClickListener {
            chatListViewModel?.deleteSelectedContact()
        }
    }

    private fun searchUser(userName: String) {

        searchViewModel?.getSearchResult(userName)

        searchViewModel?.notifyData?.observe(this, Observer {

            searchAdapter =
                SearchUserListAdapter(this, searchViewModel, object : OnItemClickListener {
                    override fun onClick(position: Int) {

                        searchViewModel?.addToView(position)

                        finish()

                    }
                })
            randomSearchList.adapter = searchAdapter

        })

    }

//    fun onSearchClicked(view: View) {
//
//        AnimationHelper.enterRevelAnimation(searchView)
//        AnimationHelper.enterRevelAnimation(randomSearchList)
//
//    }

//    override fun onBackPressed() {
//        if (searchView.visibility == View.VISIBLE) {
//            searchBox.setText("")
//            AnimationHelper.exitRevelAnimation(searchView)
//            AnimationHelper.exitRevelAnimation(randomSearchList)
//
//        } else {
//            super.onBackPressed()
//        }
//    }

    fun onNewGroupClicked(view: View) {
        showGroupTypeDialog()
    }

    private fun showGroupTypeDialog() {
        val dialogView = View.inflate(this, R.layout.dialog_group_type, null)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        //window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show()

        val privateText = dialog.findViewById<TextView>(R.id.privateText)
        val generalText = dialog.findViewById<TextView>(R.id.generalText)

        val firstIcon = dialog.findViewById<ImageView>(R.id.firstIcon)
        val secondIcon = dialog.findViewById<ImageView>(R.id.secondIcon)

        privateText.setOnClickListener {
            firstIcon.setImageDrawable(getDrawable(R.drawable.selected_icon))
            secondIcon.setImageDrawable(getDrawable(R.drawable.unselected_icon))
            selectedType = "PRIVATE"
        }

        firstIcon.setOnClickListener {
            firstIcon.setImageDrawable(getDrawable(R.drawable.selected_icon))
            secondIcon.setImageDrawable(getDrawable(R.drawable.unselected_icon))
            selectedType = "PRIVATE"
        }


        generalText.setOnClickListener {
            firstIcon.setImageDrawable(getDrawable(R.drawable.unselected_icon))
            secondIcon.setImageDrawable(getDrawable(R.drawable.selected_icon))
            selectedType = "GENERAL"
        }

        secondIcon.setOnClickListener {
            firstIcon.setImageDrawable(getDrawable(R.drawable.unselected_icon))
            secondIcon.setImageDrawable(getDrawable(R.drawable.selected_icon))
            selectedType = "GENERAL"
        }

        val submitText = dialog.findViewById<Button>(R.id.submitText)
        val cancelText = dialog.findViewById<TextView>(R.id.cancelText)
        submitText.setOnClickListener {
            dialog.dismiss()
            onSubmitTextClicked()
        }
        cancelText.setOnClickListener { dialog.dismiss() }
    }

    private fun onSubmitTextClicked() {
        val intent = Intent(this, GroupMemberSelectionActivity::class.java)
        intent.putExtra(Constants.IntentKeys.GROUPTYPE, selectedType)
        startActivity(intent)
    }

    private fun checkForDeleteConstact() {

        chatListViewModel?.getisAnyContactSelected()?.let {
            if (it) {
                searchPanel.show()
                selectedCount.text = chatListViewModel?.getcontactSelectedCount()
                if (chatListViewModel?.getcontactSelectedCount() == "1") {
                    edit.show()
                } else {
                    edit.hide()
                }
            } else {
                searchPanel.hide()
            }
        }
    }


    private fun showEditDialog() {

        val dialogView = View.inflate(this, R.layout.dialog_add_contact, null)
        val dialog = Dialog(this)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.BOTTOM)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        dialog.show()

        val backButton = dialog.findViewById<ImageView>(R.id.backIcon)

        val contactName = dialog.findViewById<EditText>(R.id.contactName)
        val code = dialog.findViewById<EditText>(R.id.enterKrypt)


        backButton.setOnClickListener { dialog.dismiss() }

        val submitButton = dialog.findViewById<TextView>(R.id.submitButton)

        submitButton.setOnClickListener {
            if (contactName.text.toString().trim().isNotEmpty()) {
                chatListViewModel?.updateValue(contactName.text.toString().trim())
                if (dialog.isShowing)
                    dialog.dismiss()
            }

        }

        chatListViewModel?.notifyContact?.observe(this, Observer {
            dialog?.let {
                if (dialog.isShowing)
                    contactName?.let { edittext ->
                        chatListViewModel?.selectedDetail?.roomName?.let {
                            edittext.setText(it)
                        }

                    }

                code?.let { codeText ->
                    chatListViewModel?.selectedDetail?.kryptId?.let {
                        codeText.setText(it)
                        code.isEnabled = false
                    }

                }

            }
        })

        chatListViewModel?.getContactName()

    }


}
