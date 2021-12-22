package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GroupDetailsResponse {


    @SerializedName("error")
    var error: String = ""

    @SerializedName("message")
    var message: String = ""

    @SerializedName("data")
    var data: GroupDetailData = GroupDetailData()


}

class GroupDetailData : Serializable {

    @SerializedName("groupsData")
    var data: ArrayList<GroupDetails> = ArrayList()

    @SerializedName("blockedUsers")
    var blockedUsers: ArrayList<BlockedUsers> = ArrayList()

    @SerializedName("chatUsers")
    var chatUsers: ArrayList<ChatUsers> = ArrayList()

}


class ChatUsers : Serializable {

    @SerializedName("fromUserName")
    var fromUserName = ""

    @SerializedName("count")
    var count = 0


//    "chatUsers": [{
//        "id": 3,
//        "": "8ctqhle7bo",
//        "toUserName": "kjb9e1k550",
//        "timestamp": "1613050420970",
//        "": 16,
//        "createdAt": "2021-02-11T13:33:40.000Z",
//        "updatedAt": "2021-02-11T13:25:16.000Z"
//    }]
}

class BlockedUsers : Serializable {

    @SerializedName("fromUser")
    var fromUser = ""

    @SerializedName("toUser")
    var toUser = ""

}


class GroupDetails : Serializable {

    @SerializedName("groupName")
    var groupName: String = ""

    @SerializedName("type")
    var type: String = ""

    @SerializedName("groupTitleName")
    var groupTitleName: String = ""

    @SerializedName("image")
    var image: String? = ""

    @SerializedName("participants")
    var participants: ArrayList<GroupParticipants>? = ArrayList()


}

class GroupParticipants : Serializable {

    @SerializedName("id")
    var id: String = ""

    @SerializedName("groupId")
    var groupId: String = ""

    @SerializedName("userName")
    var userName: String = ""

    @SerializedName("role")
    var role: String = ""

    @SerializedName("createdAt")
    var createdAt: String = ""

}


