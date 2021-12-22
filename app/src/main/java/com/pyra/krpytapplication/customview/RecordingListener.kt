package com.pyra.krpytapplication.customview

interface RecordingListener {

    fun onRecordingStarted()

    fun onRecordingLocked()

    fun onRecordingCompleted()

    fun onRecordingCanceled()

    fun requestRecordPermission()
}