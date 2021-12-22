package com.pyra.krpytapplication.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.RelativeLayout
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.gson.Gson
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.dismissLoader
import com.pyra.krpytapplication.utils.showLoader
import com.pyra.krpytapplication.model.SearchUserResult
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.viewmodel.SearchViewModel
import com.pyra.network.Api
import com.pyra.network.UrlHelper
import getApiParams
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.json.JSONObject
import showToast

class AddContactDialog : BottomSheetDialogFragment() {

    private val searchViewModel by viewModels<SearchViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setStyle(STYLE_NORMAL, R.style.DialogStyle)
    }

    private val loader by lazy {
        showLoader(requireContext())
    }

    private lateinit var contactNameEditText: EditText
    private lateinit var kryptCodeEditText: EditText
    private lateinit var contactSubmit: Button
    private lateinit var contactDialogClose: RelativeLayout

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val viewDialog = inflater.inflate(R.layout.fragment_add_contact_dialog, container, false)
        contactNameEditText = viewDialog.findViewById(R.id.contactName)
        contactSubmit = viewDialog.findViewById(R.id.submitButton)
        contactDialogClose = viewDialog.findViewById(R.id.backIcon)
        kryptCodeEditText = viewDialog.findViewById(R.id.enterKrypt)
        return viewDialog
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        contactDialogClose.setOnClickListener {
            dismiss()
        }

        contactSubmit.clickWithDebounce {
            LogUtil.e("usernam", "true")
            if (kryptCodeEditText.text.toString()
                    .isNotEmpty() && contactNameEditText.text.toString().isNotEmpty()
            ) {
                if (!loader.isShowing) {
                    loader.show()
                }

                val jsonObject = JSONObject()
                jsonObject.put(Constants.ApiKeys.NAME, kryptCodeEditText.text.toString())

                Api.postMethod(
                    getApiParams(requireContext(), jsonObject, UrlHelper.SEARCHUSER),
                    object : ApiResponseCallback {

                        override fun setResponseSuccess(jsonObject: JSONObject) {
                            val gson = Gson()
                            val response: SearchUserResult =
                                gson.fromJson(jsonObject.toString(), SearchUserResult::class.java)
                            dismissLoader(loader)
                            if (response.error) {
                                dialog?.window?.decorView?.let { view ->
                                    context?.showToast(getString(R.string.enter_valid_contact_name))
                                }
                            } else {
                                searchViewModel.insertAddContacts(
                                    data = response.data!!,
                                    name = contactNameEditText.text.toString()
                                )

                                CoroutineScope(Dispatchers.Main).launch {
                                    delay(500)
                                    dismiss()
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
                dialog?.window?.decorView?.let { view ->
                    requireContext().showToast(getString(R.string.enter_valid_contact_name))

                }
            }
        }

    }
}