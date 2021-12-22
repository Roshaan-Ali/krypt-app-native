package com.pyra.krpytapplication.model

import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.Serializable

data class AwsUploadResponse(


    var error: Boolean? = null,

    var message: String? = null,

    var completed: Boolean = false,

    var file: File? = null,

    var fileUrl: String? = null,

    var thumbUrl: String? = null
)