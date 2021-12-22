package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SearchUserResult : Serializable {

    @SerializedName("error")
    var error: Boolean = false
    @SerializedName("message")
    var message: String = ""

    @SerializedName("data")
    var data: SearchUserData? = null


}

class SearchUserData : Serializable {


    @SerializedName("username")
    var username: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("email")
    var email: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("properties")
    var properties: ArrayList<Properties>? = null


}

class Properties : Serializable {

    @SerializedName("key")
    var key: String? = null

    @SerializedName("value")
    var value: String? = null
}