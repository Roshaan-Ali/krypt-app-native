package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName

data class Data(
    val IMEI: String,
    @SerializedName("createdAt")
    val createdAt: String?,
    val deviceToken: String,
    val id: Int,
    val isEnable: Int,
    val os: String,
    val password: Any,
    val passwordAttempt: Int,
    @SerializedName("subsEnddate")
    val subsEnddate: String?,
    @SerializedName("subsStartDate")
    val subsStartDate: String?,
    @SerializedName("lastLoginTime")
    val lastLoginTime: String?,
    @SerializedName("isOnline")
    val isOnline: Int,
    val updatedAt: String,
    val userName: String,
    val voipToken: Any
)