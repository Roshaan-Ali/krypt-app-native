package com.pyra.krpytapplication.view.fragment

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.MediaController
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.camera.view.video.ExperimentalVideo
import androidx.camera.view.video.OnVideoSavedCallback
import androidx.camera.view.video.OutputFileResults
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.getFileType
import com.pyra.krpytapplication.domain.UploadStatus
import com.pyra.krpytapplication.viewmodel.GalleryViewModel
import kotlinx.android.synthetic.main.fragment_gallery.*
import kotlinx.android.synthetic.main.fragment_video.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import java.io.File


class VideoFragment : Fragment(R.layout.fragment_video), View.OnClickListener {


    private val recordFiles by lazy {
        requireActivity().filesDir
    }
    private val args: VideoFragmentArgs by navArgs()

    private val videoRecordingFilePath by lazy {
        "${recordFiles.absoluteFile}/${System.currentTimeMillis()}_video.mp4"
    }

    private val file by lazy {
        File(videoRecordingFilePath)
    }

    private val countDown by lazy {
        object : CountDownTimer(31000, 1000) {
            @SuppressLint("SetTextI18n")
            override fun onTick(millisUntilFinished: Long) {
                timer.text = "00 : ${String.format("%02d", millisUntilFinished / 1000)}"
            }

            override fun onFinish() {
                video_Record_Btn.isChecked = false
            }
        }
    }

    private val galleryViewModel by viewModels<GalleryViewModel>()


    @SuppressLint("SetTextI18n")
    @ExperimentalVideo
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        startCameraSession()
        video_Record_Btn.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                videoActionsGroup.visibility = View.INVISIBLE
                countDown.start()
                recordVideo(videoRecordingFilePath)
            } else {
                timer.text = "00 : ${String.format("%02d", 0)}"
                countDown.cancel()
                camera_view.stopRecording()
            }
        }


        lifecycleScope.launch {
            galleryViewModel.awsUploadData.collect {
                when(it){
                    UploadStatus.Loading -> {
                        videoProgressBar.visibility = View.VISIBLE
                    }
                    is UploadStatus.Success -> {
                        videoProgressBar.visibility = View.GONE
                        it.awsUploadCompleted.fileUrl?.let {url->
                            Intent().apply {
                                putExtra(Constants.IntentKeys.FILE, it.awsUploadCompleted.file?.absolutePath)
                                putExtra(Constants.IntentKeys.MEDIAURL,url)
                                putExtra(Constants.IntentKeys.THUMBURL,it.awsUploadCompleted.thumbUrl)
                                putExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, true)
                                requireActivity().setResult(Activity.RESULT_OK, this)
                                requireActivity().finish()
                            }
                        }
                    }
                    is UploadStatus.Error -> {
                        videoProgressBar.visibility = View.GONE
                        Toast.makeText(requireActivity(), it.error, Toast.LENGTH_SHORT).show()
                    }
                    UploadStatus.Empty ->{
                        videoProgressBar.visibility = View.GONE
                    }
                }


            }
        }


        video_delete.setOnClickListener(this)
        video_delete.setOnClickListener(this)
        video_upload_button.setOnClickListener(this)
        videoPlayButton.setOnClickListener(this)
        video_upload_button.setOnClickListener(this)
    }


    private fun startCameraSession() {
        if (ActivityCompat.checkSelfPermission(
                requireActivity(),
                Manifest.permission.CAMERA
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }
        camera_view.bindToLifecycle(requireActivity())
    }

    override fun onClick(v: View?) {
        when (v) {
            video_delete -> {
                if (!video_Record_Btn.isChecked) {
                    if (file.exists()) {
                        deleteFile()
                    }
                }
            }
            video_upload_button -> {
                if (!video_Record_Btn.isChecked) {

                    if (args.IsVault){
                        galleryViewModel.uploadImageFullyCompressed(File(videoRecordingFilePath), File(videoRecordingFilePath).getFileType())
                    }else{
                        Intent().apply {
                            putExtra(Constants.IntentKeys.FILE, videoRecordingFilePath)
                            putExtra(Constants.IntentKeys.ISVIDEOAVAILABLE, true)
                            requireActivity().setResult(Activity.RESULT_OK, this)
                            requireActivity().finish()
                        }
                    }
                }
            }

            videoPlayButton -> {
                videoPlayButton.visibility = View.GONE
                videoPreviewImg.start()
            }
        }
    }


    private fun deleteFile() {
        AlertDialog.Builder(requireActivity(), android.R.style.Theme_Material_Dialog)
            .setTitle(getString(R.string.delete_title))
            .setMessage(getString(R.string.delete_video))
            .setIcon(android.R.drawable.ic_dialog_alert)
            .setPositiveButton(android.R.string.yes) { _, _ ->
                file.delete()
                videoPreviewActionGroup.visibility = View.VISIBLE
                videoActionsGroup.visibility = View.INVISIBLE
            }
            .setNegativeButton(android.R.string.no, null)
            .create().showImmersive()
    }


    @ExperimentalVideo
    private fun recordVideo(videoRecordingFilePath: String) {
        camera_view.startRecording(File(videoRecordingFilePath),ContextCompat.getMainExecutor(requireActivity()), object : OnVideoSavedCallback {

            override fun onVideoSaved(outputFileResults: OutputFileResults) {
                videoActionsGroup.visibility = View.VISIBLE
                videoPreviewActionGroup.visibility = View.INVISIBLE
                videoPreviewImg.setVideoPath(file.absolutePath)
                videoPreviewImg.setMediaController(MediaController(requireActivity()))
                Toast.makeText(requireContext(), "Recording Saved", Toast.LENGTH_SHORT).show()
            }



            override fun onError(videoCaptureError: Int, message: String, cause: Throwable?) {
                videoActionsGroup.visibility = View.INVISIBLE
                videoPreviewActionGroup.visibility = View.VISIBLE
                Toast.makeText(requireActivity(), "Recording Failed", Toast.LENGTH_SHORT).show()
            }
        })
    }
}