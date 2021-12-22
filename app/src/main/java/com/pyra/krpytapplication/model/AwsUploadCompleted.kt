package com.pyra.krpytapplication.model

import java.io.File

data class AwsUploadCompleted (
    var file:File?,
    var fileUrl:String?,
    var thumbUrl:String?
)