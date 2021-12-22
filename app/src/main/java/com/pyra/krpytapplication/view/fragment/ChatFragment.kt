package com.pyra.krpytapplication.view.fragment

import android.annotation.SuppressLint
import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.view.*
import android.widget.*
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.gson.Gson
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.*
import com.pyra.krpytapplication.domain.OnClickButtonListener
import com.pyra.krpytapplication.model.SearchUserResult
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.view.activity.KryptCodeActivity
import com.pyra.krpytapplication.view.adapter.ChatListAdapter
import com.pyra.krpytapplication.viewmodel.ChatListViewModel
import com.pyra.krpytapplication.viewmodel.ProfileViewModel
import com.pyra.krpytapplication.viewmodel.SearchViewModel
import com.pyra.network.Api
import com.pyra.network.UrlHelper
import getApiParams
import jp.wasabeef.blurry.Blurry
import kotlinx.android.synthetic.main.activity_krypt_code.*
import kotlinx.android.synthetic.main.fragment_chat.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import showToast

class ChatFragment : Fragment(R.layout.fragment_chat) {

    lateinit var chatListAdapter: ChatListAdapter
    var onClickButtonListener: OnClickButtonListener? = null
    lateinit var chatListViewModel: ChatListViewModel
    lateinit var searchViewModel: SearchViewModel
    var loader: Dialog? = null
    val profileViewModel: ProfileViewModel by viewModels()

    private val loadBar by lazy {
        showLoader(requireContext())
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chatListViewModel = ViewModelProvider(this).get(ChatListViewModel::class.java)
        searchViewModel = ViewModelProvider(this).get(SearchViewModel::class.java)
        initAdapter()
        initListener()
        setMenuInflater()
    }

    private fun showSearchLayout() {
        selectionPanel.show()
        topLayout.hide()
    }

    private fun showNormalLayout() {
        selectionPanel.hide()
        topLayout.show()
    }

    private fun initListener() {

        searchText.addTextChangedListener {
            if (it.toString().trim() == "") {
                chatListViewModel.getChatListAsync()
            } else {
                chatListViewModel.getSearchedData(it.toString())
            }
        }

        backPressed.setOnClickListener {
            chatListViewModel.removeSelection()
        }

        deleteChat.setOnClickListener {
            showRemoveDialog()
        }
    }

    @SuppressLint("SetTextI18n")
    private fun showRemoveDialog() {
        val dialog = getChatDeleteDialog(requireContext())

        val title = dialog.findViewById<TextView>(R.id.title)
        val checkBox = dialog.findViewById<CheckBox>(R.id.checkBox)
        val cancel = dialog.findViewById<TextView>(R.id.cancel)
        val delete = dialog.findViewById<TextView>(R.id.delete)

        if (chatListViewModel.getSelectedCount() == 1) {
            title.text =
                getString(R.string.delete_chat_with) + " \"" + chatListViewModel.selectedRoomName + "\""
        } else {
            title.text = "Delete " + chatListViewModel.getSelectedCount() + " selected chats"
        }

        cancel.setOnClickListener {
            dialog.dismiss()
        }

        delete.setOnClickListener {
            chatListViewModel.deleteSelectedChats(checkBox.isChecked)
            dialog.dismiss()
        }
    }

    override fun onResume() {
        super.onResume()
        chatListViewModel.checkForBurnMessage()
        println("LXMPP On Resume Called")

        checkSubscription()
    }

    private fun checkSubscription() {

        profileViewModel.getUserDeatilsResponse(
            SharedHelper(requireContext()).kryptKey,
            UrlHelper.GETUSERDETAILS
        ).observe(viewLifecycleOwner) {

            if (it.error == "false") {

                it.data[0].subsEnddate?.let {

                    val endDatedate = getFormatedDate(
                        it,
                        "yyyy-MM-dd'T'HH:mm:ss",
                        "yyyy-MM-dd"
                    )

                    endDatedate?.let {
                        if (endDatedate != "Not Updated") {
                            val isSubscriptionEnded =
                                isSubScriptionEnded(endDatedate, "yyyy-MM-dd")

                            if (isSubscriptionEnded) {
                                clearAllData()
                            }
                        }
                    }

                }

            }

        }

    }

    private fun clearAllData() {
        val bundle = Bundle()
        bundle.putBoolean("isSubEnded", true)
        requireActivity().openNewTaskActivity(KryptCodeActivity::class.java, bundle)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        onClickButtonListener = (activity as OnClickButtonListener?)
    }

    private fun initAdapter() {
        val linearLayoutManager =
            LinearLayoutManager(activity, LinearLayoutManager.VERTICAL, false)
        chatListAdapter = ChatListAdapter(requireActivity(), chatListViewModel)
        chatLists.layoutManager = linearLayoutManager
        chatLists.adapter = chatListAdapter
        listener()

        chatListViewModel.update.observe(viewLifecycleOwner) {
            chatListAdapter.notifyDataSetChanged()
            if (chatListViewModel.isMultiSelectionEnabled) {
                showSearchLayout()
            } else {
                showNormalLayout()
            }
            selectedCount.text = chatListViewModel.getSelectedCount().toString()
        }

        chatListViewModel.chatListCount.observe(viewLifecycleOwner, Observer {
            messageCount.text = "( $it messages )"
        })

        chatListViewModel.getChatList()
    }

    private fun listener() {
        contactButton.setOnClickListener {
            onClickButtonListener?.onClickListener()
        }
        newUser.setOnClickListener {
            showAddContactDialog()
            //findNavController().navigate(ChatFragmentDirections.actionChatToAddContactDialog())
        }
    }

    private fun showAddContactDialog() {
        val dialogView = View.inflate(requireContext(), R.layout.fragment_add_contact_dialog, null)
        val dialog = Dialog(requireContext())
        val kryptCodeEditText = dialogView.findViewById<EditText>(R.id.enterKrypt)
        val contactNameEditText = dialogView.findViewById<EditText>(R.id.contactName)
        val submitButton = dialogView.findViewById<Button>(R.id.submitButton)
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(dialogView)
        val window: Window = dialog.window!!
        window.setGravity(Gravity.CENTER)
        window.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        window.setDimAmount(0.0f)
        Blurry.with(requireContext()).radius(10).sampling(2).onto(activity?.rootLayout)

        dialog.setOnDismissListener {
            Blurry.delete(activity?.rootLayout)
        }

        //window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialog.show()

        val backButton = dialog.findViewById<RelativeLayout>(R.id.backIcon)
        backButton.setOnClickListener { dialog.dismiss() }

        submitButton.clickWithDebounce {
            LogUtil.e("usernam", "true")
            if (kryptCodeEditText.text.toString()
                    .isNotEmpty() && contactNameEditText.text.toString().isNotEmpty()
            ) {
                if (!loadBar.isShowing) {
                    loadBar.show()
                }

                val jsonObject = JSONObject()
                jsonObject.put(Constants.ApiKeys.NAME, kryptCodeEditText.text.toString())

                Api.postMethod(
                    getApiParams(requireContext(), jsonObject, UrlHelper.SEARCHUSER),
                    object : ApiResponseCallback {

                        override fun setResponseSuccess(jsonObject: JSONObject) {
                            val gson = Gson()
                            val response: SearchUserResult =
                                gson.fromJson(
                                    jsonObject.toString(),
                                    SearchUserResult::class.java
                                )
                            dismissLoader(loadBar)
                            if (response.error) {
                                dialog.window?.decorView?.let { view ->
                                    requireActivity().showToast(getString(R.string.enter_valid_contact_name))
                                }
                            } else {
                                searchViewModel.insertAddContacts(
                                    data = response.data!!,
                                    name = contactNameEditText.text.toString()
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    dialog.dismiss()
                                }

                            }
                        }

                        override fun setErrorResponse(error: String) {

                            requireContext().showToast(error)

                        }
                    })

                searchViewModel.getSearchResultAndAddUser(kryptCodeEditText.text.toString())
                    ?.observe(viewLifecycleOwner, Observer {

                    })

                LogUtil.e("usernam", contactNameEditText.text.toString())

            } else {
                dialog.window?.decorView?.let { view ->
                    requireContext().showToast(getString(R.string.enter_valid_contact_name))

                }
            }
        }

    }

    private fun setMenuInflater() {

        val popup = PopupMenu(requireContext(), selectionMenu)
        popup.menuInflater.inflate(R.menu.chat_message_menu, popup.menu)

        popup.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.clearChat -> {
                    chatListViewModel.clearAllChats()
                }
            }
            return@setOnMenuItemClickListener true
        }

        selectionMenu.setOnClickListener {
            popup.show()
        }

    }
}
