package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CreateCallResponseModel : Serializable {

    @SerializedName("error")
    var error: String = ""
    @SerializedName("message")
    var message: String = ""
    @SerializedName("data")
    var data: CreateCallData? = null

}

class CreateCallData : Serializable {

    @SerializedName("id")
    var id: String = ""

}