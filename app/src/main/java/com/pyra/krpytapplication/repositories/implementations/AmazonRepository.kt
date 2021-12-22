package com.pyra.krpytapplication.repositories.implementations

import android.content.Context
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.amazonaws.SDKGlobalConfiguration
import com.amazonaws.auth.CognitoCachingCredentialsProvider
import com.amazonaws.mobileconnectors.s3.transferutility.*
import com.amazonaws.regions.Region
import com.amazonaws.services.s3.AmazonS3Client
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.MediaType
import com.pyra.krpytapplication.utils.getFileType
import com.pyra.krpytapplication.model.AwsDownloadResponse
import com.pyra.krpytapplication.model.AwsUploadResponse
import getNewFileName
import org.json.JSONObject
import java.io.File


class AmazonRepository private constructor() {

    companion object {

        private var repository: AmazonRepository? = null
        private var s3Client: AmazonS3Client? = null
        private var transferUtility: TransferUtility? = null
        private var transferObserver: TransferObserver? = null
        var credentialsProvider: CognitoCachingCredentialsProvider? = null

        fun getInstance(): AmazonRepository {
            if (repository == null) {
                repository = AmazonRepository()
            }
            return repository as AmazonRepository
        }
    }

    fun uploadToAWS(
        context: Context,
        file: File
    ): LiveData<AwsUploadResponse> {

        val apiUploadResponse: MutableLiveData<AwsUploadResponse> = MutableLiveData()

        val fileName = file.name

        s3Client = getClient(context.applicationContext)

        TransferNetworkLossHandler.getInstance(context)
        val tuOptions = TransferUtilityOptions()
        tuOptions.transferThreadPoolSize = 10

        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .transferUtilityOptions(tuOptions)
            .build()

        transferObserver =
            transferUtility!!.upload(Constants.AWS.BUCKET_NAME, fileName, file)
        val finalFileName: String = fileName

        transferObserver!!.setTransferListener(object : TransferListener {
            override fun onStateChanged(
                id: Int,
                state: TransferState
            ) {

                if (state.toString().equals("COMPLETED", ignoreCase = true)) {

                    val url: String = Constants.AWS.BASE_S3_URL + finalFileName
                    val jsonObject = JSONObject()
                    jsonObject.put(Constants.ApiKeys.IMAGEURL, url)

                    println("Image Upload completed $url")

                    val response = AwsUploadResponse()
                    response.error = false
                    response.message = url
                    response.completed = true
                    apiUploadResponse.postValue(response)
                }
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
                try {
                    val percentage = (bytesCurrent / bytesTotal * 100).toInt()
                    val response = AwsUploadResponse()
                    response.error = false
                    response.message = "$percentage"
                    println("Image Upload Percentage = $percentage")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(id: Int, ex: Exception) {
                val response = AwsUploadResponse()
                response.error = true
                response.message = ex.message
                apiUploadResponse.postValue(response)
            }
        })

        return apiUploadResponse
    }

    // get auth details for aws
    private fun getClient(context: Context): AmazonS3Client {
        System.setProperty(
            SDKGlobalConfiguration.ENFORCE_S3_SIGV4_SYSTEM_PROPERTY,
            "true"
        )

        credentialsProvider =
            CognitoCachingCredentialsProvider(
                context.applicationContext, Constants.AWS.POOL_ID, Constants.AWS.REGION
            )
        return AmazonS3Client(
            credentialsProvider, Region.getRegion(Constants.AWS.REGION)
        )
    }

    fun stopUpload() {

        transferObserver?.let {
            transferUtility?.cancel(it.id)
        }

    }

    fun downloadFile(context: Context, url: String): LiveData<AwsDownloadResponse>? {
        val apiUploadResponse: MutableLiveData<AwsDownloadResponse> = MutableLiveData()

        val file = File(url)

        var newFileName = getNewFileName(MediaType.IMAGE.value, "")

        if (file.getFileType() == MediaType.IMAGE.value) {
            newFileName =
                getNewFileName(MediaType.IMAGE.value, "")
        } else if (file.getFileType() == MediaType.VIDEO.value) {
            newFileName =
                getNewFileName(MediaType.VIDEO.value, "")
        }

        var destiniFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/download")

        if (!destiniFile.exists()) {
            destiniFile.mkdirs()
        }

        destiniFile =
            File(context.getExternalFilesDir(null)!!.absolutePath, "/download/$newFileName")
        if (!destiniFile.exists()) {
            destiniFile.createNewFile()
        }

        s3Client = getClient(context.applicationContext)

        TransferNetworkLossHandler.getInstance(context)
        val tuOptions = TransferUtilityOptions()
        tuOptions.transferThreadPoolSize = 10

        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .transferUtilityOptions(tuOptions)
            .build()

        transferObserver =
            transferUtility!!.download(Constants.AWS.BUCKET_NAME, file.name, destiniFile)

        transferObserver!!.setTransferListener(object : TransferListener {
            override fun onStateChanged(
                id: Int,
                state: TransferState
            ) {

                if (state.toString().equals("COMPLETED", ignoreCase = true)) {

                    println("Image Download completed $url")

                    val response = AwsDownloadResponse()
                    response.error = false
                    response.message = ""
                    response.file = destiniFile
                    apiUploadResponse.postValue(response)
                }
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
                try {
                    val percentage = (bytesCurrent / bytesTotal * 100).toInt()
                    println("Image Download Percentage = $percentage")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(id: Int, ex: Exception) {
                val response = AwsDownloadResponse()
                response.error = true
                response.message = ex.message
                apiUploadResponse.postValue(response)
            }
        })

        return apiUploadResponse

    }

    fun downloadDocumentFile(
        context: Context,
        url: String,
        fileName: String
    ): LiveData<AwsDownloadResponse>? {
        val apiUploadResponse: MutableLiveData<AwsDownloadResponse> = MutableLiveData()

        val file = File(url)

        var destiniFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/download")

        if (!destiniFile.exists()) {
            destiniFile.mkdirs()
        }

        destiniFile = File(
            context.getExternalFilesDir(null)!!.absolutePath,
            "/download/${getNewFileName(MediaType.DOCUMENT.value, fileName)}"
        )
        if (!destiniFile.exists()) {
            destiniFile.createNewFile()
        }

        s3Client = getClient(context.applicationContext)

        TransferNetworkLossHandler.getInstance(context)
        val tuOptions = TransferUtilityOptions()
        tuOptions.transferThreadPoolSize = 10

        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .transferUtilityOptions(tuOptions)
            .build()

        transferObserver =
            transferUtility!!.download(Constants.AWS.BUCKET_NAME, file.name, destiniFile)

        transferObserver!!.setTransferListener(object : TransferListener {
            override fun onStateChanged(
                id: Int,
                state: TransferState
            ) {

                if (state.toString().equals("COMPLETED", ignoreCase = true)) {

                    println("Image Download completed $url")

                    val response = AwsDownloadResponse()
                    response.error = false
                    response.message = ""
                    response.file = destiniFile
                    apiUploadResponse.postValue(response)
                }
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
                try {
                    val percentage = (bytesCurrent / bytesTotal * 100).toInt()
                    println("Image Download Percentage = $percentage")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(id: Int, ex: Exception) {
                val response = AwsDownloadResponse()
                response.error = true
                response.message = ex.message
                apiUploadResponse.postValue(response)
            }
        })

        return apiUploadResponse
    }

    fun downloadAudioFile(
        context: Context,
        url: String,
        fileName: String
    ): LiveData<AwsDownloadResponse>? {
        val apiUploadResponse: MutableLiveData<AwsDownloadResponse> = MutableLiveData()

        val file = File(url)

        var destiniFile = File(context.getExternalFilesDir(null)!!.absolutePath, "/download")

        if (!destiniFile.exists()) {
            destiniFile.mkdirs()
        }

        destiniFile = File(
            context.getExternalFilesDir(null)!!.absolutePath,
            "/download/${getNewFileName(MediaType.AUDIO.value, fileName)}"
        )
        if (!destiniFile.exists()) {
            destiniFile.createNewFile()
        }

        s3Client = getClient(context.applicationContext)

        TransferNetworkLossHandler.getInstance(context)
        val tuOptions = TransferUtilityOptions()
        tuOptions.transferThreadPoolSize = 10

        transferUtility = TransferUtility.builder()
            .s3Client(s3Client)
            .context(context.applicationContext)
            .transferUtilityOptions(tuOptions)
            .build()

        transferObserver =
            transferUtility!!.download(Constants.AWS.BUCKET_NAME, file.name, destiniFile)

        transferObserver!!.setTransferListener(object : TransferListener {
            override fun onStateChanged(
                id: Int,
                state: TransferState
            ) {

                if (state.toString().equals("COMPLETED", ignoreCase = true)) {

                    println("Image Download completed $url")

                    val response = AwsDownloadResponse()
                    response.error = false
                    response.message = ""
                    response.file = destiniFile
                    apiUploadResponse.postValue(response)
                }
            }

            override fun onProgressChanged(
                id: Int,
                bytesCurrent: Long,
                bytesTotal: Long
            ) {
                try {
                    val percentage = (bytesCurrent / bytesTotal * 100).toInt()
                    println("Image Download Percentage = $percentage")
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun onError(id: Int, ex: Exception) {
                val response = AwsDownloadResponse()
                response.error = true
                response.message = ex.message
                apiUploadResponse.postValue(response)
            }
        })

        return apiUploadResponse
    }

    fun cancelDownload() {

        transferObserver?.let {
            transferUtility?.cancel(it.id)
        }

    }

}