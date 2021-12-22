package com.pyra.krpytapplication.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.roomDb.AppDataBase
import com.pyra.krpytapplication.viewmodel.ProfileViewModel
import kotlinx.android.synthetic.main.dialog_add_contact.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class EditProfileBottomSheet : BottomSheetDialogFragment(), View.OnClickListener {

    private lateinit var profileViewModel: ProfileViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.dialog_add_contact, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.let {
            profileViewModel.getUser(it.getString(Constants.IntentKeys.ROOMID, ""))
                ?.observe(this, Observer { chat ->
                    enterKrypt.setText(chat.kryptId)
                    contactName.setText(chat.roomName)

                })
        }

        submitButton.setOnClickListener(this)
        backIcon.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        when (v) {
            submitButton -> {
                arguments?.let {
                    CoroutineScope(Dispatchers.IO).launch {
                        AppDataBase.getInstance(requireActivity())?.chatListDao()?.updateUser(
                            it.getString(Constants.IntentKeys.ROOMID, ""),
                            contactName.text.toString()
                        )
                    }
                }
                dismiss()
            }
            backIcon -> {
                dismiss()
            }
        }
    }

    companion object {

        fun newInstance(): EditProfileBottomSheet {
            return EditProfileBottomSheet()
        }

    }
}