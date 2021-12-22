package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.Serializable

class GetUserProfile : Serializable {

    @SerializedName("error")
    var error: Boolean = false
    @SerializedName("message")
    var message: String = ""

    @SerializedName("data")
    var data: ArrayList<SearchUserData>? = null


}
