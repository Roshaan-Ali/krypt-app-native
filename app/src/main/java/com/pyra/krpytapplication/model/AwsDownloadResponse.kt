package com.pyra.krpytapplication.model

import java.io.File
import java.io.Serializable

class AwsDownloadResponse : Serializable {

    var error: Boolean? = null

    var message: String? = null

    var file: File? = null


}