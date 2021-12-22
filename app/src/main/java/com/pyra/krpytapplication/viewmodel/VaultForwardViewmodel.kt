package com.pyra.krpytapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.asLiveData
import com.pyra.krpytapplication.roomDb.dao.ChatListDao
import com.pyra.krpytapplication.roomDb.entity.ChatListSchema

class VaultForwardViewmodel : ViewModel() {

    fun getUser(chatListDao: ChatListDao): LiveData<MutableList<ChatListSchema>> =
        chatListDao.getUserList().asLiveData()
}