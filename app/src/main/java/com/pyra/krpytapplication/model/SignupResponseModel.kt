package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class SignupResponseModel : Serializable {

    @SerializedName("error")
    var error: Boolean? = null

    @SerializedName("message")
    var message: String? = null

    @SerializedName("token")
    var token: String? = null
}