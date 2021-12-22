package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class LoginResponseModel : Serializable {

    @SerializedName("error")
    var error: Boolean? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("data")
    var data: LoginData? = null

}

class LoginData : Serializable {

    @SerializedName("username")
    var username: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("password")
    var password: String? = null

    @SerializedName("properties")
    var properties: ArrayList<Properties>? = null

    @SerializedName("passwordAttempt")
    var passwordAttempt: Int? = null

    @SerializedName("token")
    var token: String? = null

}
