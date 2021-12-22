package com.pyra.krpytapplication.domain

import com.pyra.krpytapplication.model.AwsUploadResponse

sealed class UploadStatus {

    object Loading : UploadStatus()

    data class Success(val awsUploadCompleted: AwsUploadResponse) : UploadStatus()

    data class Error(val error: String) : UploadStatus()

    object Empty : UploadStatus()


}
