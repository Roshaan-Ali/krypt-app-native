package com.nicoqueijo.android.currencyconverter.kotlin.viewmodel

import android.annotation.SuppressLint
import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.nicoqueijo.android.currencyconverter.R
import com.nicoqueijo.android.currencyconverter.kotlin.app.CurrencyApplication
import com.nicoqueijo.android.currencyconverter.kotlin.data.Repository
import java.text.SimpleDateFormat
import java.util.*
import javax.inject.Inject

class MainActivityViewModel(application: Application) : AndroidViewModel(application) {

    @Inject
    lateinit var repository: Repository

    init {
        (application.applicationContext as CurrencyApplication).getAppComponent().inject(this)
    }

    val activeFragment = MutableLiveData(R.id.splashFragment)

    @SuppressLint("SimpleDateFormat")
    fun getFormattedLastUpdate(): String {
        val timestamp = repository.timestamp
        val date = Date(timestamp)
        val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm")
        simpleDateFormat.timeZone = TimeZone.getDefault()
        return simpleDateFormat.format(date)
    }

}