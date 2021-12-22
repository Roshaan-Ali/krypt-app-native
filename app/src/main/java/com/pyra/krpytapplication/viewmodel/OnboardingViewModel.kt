package com.pyra.krpytapplication.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pyra.krpytapplication.model.SignupResponseModel

class OnboardingViewModel : ViewModel() {
    fun register(): LiveData<SignupResponseModel> {
        return MutableLiveData<SignupResponseModel>()
    }
}