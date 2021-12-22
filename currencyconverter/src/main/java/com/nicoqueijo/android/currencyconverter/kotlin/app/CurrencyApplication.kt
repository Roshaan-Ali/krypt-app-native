package com.nicoqueijo.android.currencyconverter.kotlin.app

import android.app.Application
import android.util.Log
import com.nicoqueijo.android.currencyconverter.kotlin.dagger.ApplicationComponent
import com.nicoqueijo.android.currencyconverter.kotlin.dagger.ApplicationScope
import com.nicoqueijo.android.currencyconverter.kotlin.dagger.ContextModule
import com.nicoqueijo.android.currencyconverter.kotlin.dagger.DaggerApplicationComponent

@ApplicationScope
open class CurrencyApplication : Application() {

    private lateinit var appComponent: ApplicationComponent

    override fun onCreate() {
        super.onCreate()
        Log.d("CurrencyApplication", "OnCreate called")
        appComponent = DaggerApplicationComponent
            .builder()
            .contextModule(ContextModule(applicationContext))
            .build()
    }

    fun getAppComponent(): ApplicationComponent {
        return appComponent
    }
}