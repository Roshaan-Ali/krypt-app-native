package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class CommonResponseModel : Serializable {

    @SerializedName("error")
    var error: String = ""

    @SerializedName("message")
    var message: String = ""
}