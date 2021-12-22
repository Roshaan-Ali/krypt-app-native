package com.pyra.krpytapplication.utils

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

object Coroutine {

     fun iOWorker( work: suspend (() -> Unit)) {
        CoroutineScope(Dispatchers.IO).launch {
            work()
        }
    }

    fun mainWorker(work: suspend (() -> Unit)) {
        CoroutineScope(Dispatchers.Main).launch {
            work()
        }
    }


}