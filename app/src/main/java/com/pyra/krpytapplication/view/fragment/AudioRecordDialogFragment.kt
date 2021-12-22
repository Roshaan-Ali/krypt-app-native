package com.pyra.krpytapplication.view.fragment

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.media.MediaRecorder
import android.os.Bundle
import android.os.SystemClock
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.fragment.app.DialogFragment
import com.pyra.krpytapplication.R
import kotlinx.android.synthetic.main.record_dialog.*
import java.io.File
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.*


class AudioRecordDialogFragment:DialogFragment(),View.OnClickListener {

    private lateinit var mRecordedfileListener:RecordedFileListener
    private  val AUDIO_EXTENSION = ".3gp"
    private lateinit var myAudioRecorder: MediaRecorder

    private lateinit var file: File

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? =  inflater.inflate(R.layout.record_dialog, container, false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        myAudioRecorder = MediaRecorder()
        myAudioRecorder.setAudioSource(MediaRecorder.AudioSource.MIC)
        myAudioRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP)
        myAudioRecorder.setAudioEncoder(MediaRecorder.OutputFormat.AMR_NB)
        file =   createFile(
            requireActivity().filesDir,
            AUDIO_EXTENSION
        )
        myAudioRecorder.setOutputFile(
          file.absolutePath
        )
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog: Dialog = super.onCreateDialog(savedInstanceState)
        dialog.window?.requestFeature(Window.FEATURE_NO_TITLE)
        return dialog
    }

    override fun onStart() {
        super.onStart()
        val dialog = dialog
        if (dialog != null) {
            dialog.window!!.setLayout(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
            dialog.window!!.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        chronometerTimer.format = "%s"
        recordCheck.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked){
                chronometerTimer.base = SystemClock.elapsedRealtime()
                chronometerTimer.start()
                try {
                    myAudioRecorder.prepare()
                    myAudioRecorder.start()
                } catch (ise: IllegalStateException) {
                } catch (ioe: IOException) {
                }
            }else{
                chronometerTimer.base = SystemClock.elapsedRealtime()
                chronometerTimer.stop()
                myAudioRecorder.stop()
                myAudioRecorder.release()
            }
        }

        cancelButton.setOnClickListener(this)
        yesButton.setOnClickListener(this)
        deleteBtn.setOnClickListener(this)
    }

    override fun onClick(v: View?) {
        when(v){
            cancelButton -> {
                if (!recordCheck.isChecked) {
                    dismiss()
                }
            }
            yesButton -> {
                if(!recordCheck.isChecked){
                    if (file.exists()){
                        mRecordedfileListener.onFinish(file)
                        dismiss()
                    }
                }
            }

            deleteBtn -> {
                if(!recordCheck.isChecked){
                    if (file.exists()){
                        file.delete()
                        dismiss()
                    }
                }
            }
        }
    }

    companion object{
        fun newInstance(): AudioRecordDialogFragment {
            return AudioRecordDialogFragment()
        }
    }

    fun onFileRecordFinished(recordedfileListener: RecordedFileListener) {
        mRecordedfileListener = recordedfileListener
    }

    interface RecordedFileListener{

        fun onFinish(file: File)
    }

    private fun createFile(baseFolder: File, extension: String):File =
        File(
            baseFolder, SimpleDateFormat("yyyy-MM-dd-HH-mm-ss-SSS", Locale.US)
                .format(System.currentTimeMillis()) + extension
        )
}