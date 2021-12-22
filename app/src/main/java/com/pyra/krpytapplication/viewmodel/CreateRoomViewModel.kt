package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.pyra.krpytapplication.utils.Coroutine
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.repositories.implementations.ProfileRepository
import com.pyra.krpytapplication.roomDb.entity.GroupParticipationSchema

class CreateRoomViewModel(application: Application) : AndroidViewModel(application) {

    var myapp = application

    var selectedIds = ArrayList<String>()

    var participationList = ArrayList<GroupParticipationSchema>()
    var selectedList = ArrayList<GroupParticipationSchema>()

    var notifyAdapter:MutableLiveData<Void>? = MutableLiveData<Void>()

    var profileRepository = ProfileRepository.getInstance((application as MyApp).getAppDatabase())

    fun getGroupMembers(roomId: String) {

        Coroutine.iOWorker {

            val details = profileRepository.getRoomProfile(roomId)

            Coroutine.mainWorker {
                if (details?.groupType == "PRIVATE") {
                    profileRepository.getParticipation(roomId)?.observeForever {
                        participationList = it as ArrayList
                        var i = 0
                        while (i < participationList.size) {
                            if (participationList[i].kryptId.toUpperCase() == SharedHelper(myapp).kryptKey.toUpperCase()) {
                                participationList.removeAt(i)
                            } else {
                                i++
                            }
                        }
                        notifyAdapter?.value = null
                    }
                } else {
                    profileRepository.getParticipationList(roomId)?.observeForever {
                        participationList = it as ArrayList
                        var i = 0
                        while (i < participationList.size) {
                            if (participationList[i].kryptId.toUpperCase() == SharedHelper(myapp).kryptKey.toUpperCase()) {
                                participationList.removeAt(i)
                            } else {
                                i++
                            }
                        }
                        notifyAdapter?.value = null

                    }
                }
            }
        }

    }

    fun getSelectedName(position: Int): String? {
        return if (selectedList[position].userName == null || selectedList[position].userName == "" || selectedList[position].userName == "null") {
            selectedList[position].kryptId
        } else {
            selectedList[position].userName
        }

    }

    fun getSelectedImage(position: Int): String? {
        return selectedList[position].userName
    }

    fun getName(position: Int): CharSequence? {
        return if (participationList[position].userName == null || participationList[position].userName == "" || participationList[position].userName == "null") {
            participationList[position].kryptId
        } else {
            participationList[position].userName
        }

    }

    fun getImage(position: Int): String? {
        return participationList[position].userName
    }

    fun addUser(position: Int) {

        if ((!selectedIds.contains(participationList[position].kryptId.toUpperCase())) && selectedIds.size <= 5) {
            selectedIds.add(participationList[position].kryptId.toUpperCase())
            selectedList.add(participationList[position])
            notifyAdapter?.value = null
        }

    }

    fun removeUser(position: Int) {
        if (selectedIds.contains(selectedList[position].kryptId.toUpperCase())) {
            selectedIds.remove(selectedList[position].kryptId.toUpperCase())
            selectedList.removeAt(position)
            notifyAdapter?.value = null
        }
    }


}