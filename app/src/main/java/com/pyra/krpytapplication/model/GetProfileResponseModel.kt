package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GetProfileResponseModel {

    @SerializedName("error")
    var error: Boolean? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("data")
    var data: ProfileData? = null

}

class ProfileData : Serializable {

    @SerializedName("username")
    var username: String? = null

    @SerializedName("name")
    var name: String? = null

    @SerializedName("properties")
    var properties: ArrayList<Properties>? = null

}