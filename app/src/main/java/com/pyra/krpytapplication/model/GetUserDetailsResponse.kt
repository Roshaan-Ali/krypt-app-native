package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName

data class GetUserDetailsResponse(

    @SerializedName("data")
    val data: List<Data>,
    @SerializedName("error")
    val error: String,
    @SerializedName("message")
    val message: String
)