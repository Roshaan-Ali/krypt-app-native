package com.pyra.krpytapplication.utils

import com.pyra.krpytapplication.roomDb.entity.ChatListSchema

class SelectedContactSingleton private constructor() {

    companion object {
        private var instance: SelectedContactSingleton? = null

        fun getInstance(): SelectedContactSingleton? {
            if (instance == null) {
                instance = SelectedContactSingleton()
            }
            return instance
        }
    }

    var listofUsers: ArrayList<ChatListSchema> = ArrayList()
    var groupType = "PRIVATE"
}