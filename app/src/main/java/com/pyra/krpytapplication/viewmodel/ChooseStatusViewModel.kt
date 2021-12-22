package com.pyra.krpytapplication.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.repositories.implementations.ProfileRepository
import com.pyra.network.UrlHelper
import getImei
import org.json.JSONArray
import org.json.JSONObject

enum class Status {
    AVAILABLE,
    AT_WORK,
    IN_A_MEETING,
    BUSY
}

class ChooseStatusViewModel(application: Application) : AndroidViewModel(application) {
    var currentStatus: Status
    var currentSelectedIndex = 0
    var profielRepository: ProfileRepository
    var app: Application
    var sharedHelper: SharedHelper? = null
    var success: MutableLiveData<Boolean> = MutableLiveData()

    init {
        this.app = application
        sharedHelper = SharedHelper(application.applicationContext)
        currentStatus = stringToStatus(sharedHelper?.status!!)
        profielRepository = ProfileRepository.getInstance((application as MyApp).getAppDatabase())
        this.currentSelectedIndex = getIndex(currentStatus)
    }

    fun numberOfStatuses(): Int {
        return Status.values().size
    }

    fun getStatusName(at: Int): String {
        return statusToString(Status.values()[at])
    }

    fun isStatusSelected(at: Int): Boolean {
        return currentSelectedIndex == at
    }

    fun setSelected(at: Int) {
        currentSelectedIndex = at
        currentStatus = getStatus(at)
    }

    fun getStatus(at: Int): Status {
        return Status.values()[at]
    }

    fun getCurrentStatusName(): String {
        return statusToString(currentStatus)
    }

    fun changeStatus() {

        val propertiesValue = JSONArray()

        var jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.KEY, Constants.ApiKeys.IMAGE)
        jsonObject.put(Constants.ApiKeys.VALUE, sharedHelper?.userImage)

        propertiesValue.put(jsonObject)

        jsonObject = JSONObject()
        jsonObject.put(Constants.ApiKeys.KEY, Constants.ApiKeys.STATUS)
        jsonObject.put(Constants.ApiKeys.VALUE, statusToString(currentStatus))

        propertiesValue.put(jsonObject)

        val requestPayload = JSONObject()
        requestPayload.put(Constants.ApiKeys.PROPERTIES, propertiesValue.toString())
        requestPayload.put(Constants.ApiKeys.IMEI, getImei(app.applicationContext))
        requestPayload.put(Constants.ApiKeys.USER_NAME, sharedHelper?.kryptKey)

        profielRepository.updateProperties(getApiParams(requestPayload, UrlHelper.UPDATEPROPERTIES))
            ?.observeForever {

                if (it.error == "false") {
                    SharedHelper(app.applicationContext).status = statusToString(currentStatus)
                    success.value = true
                }
            }
    }


    fun getIndex(of: Status): Int {
        Status.values().forEachIndexed { index, status ->
            if (status == of) {
                return index;
            }
        }
        return 0
    }

    fun stringToStatus(string: String): Status {
        when (string) {
            app.getString(R.string.available) -> return Status.AVAILABLE
            app.getString(R.string.at_work) -> return Status.AT_WORK
            app.getString(R.string.in_a_meeting) -> return Status.IN_A_MEETING
            app.getString(R.string.busy) -> return Status.BUSY
        }
        return Status.AVAILABLE
    }

    fun statusToString(status: Status): String {
        when (status) {
            Status.AVAILABLE -> return app.getString(R.string.available)
            Status.AT_WORK -> return app.getString(R.string.at_work)
            Status.IN_A_MEETING -> return app.getString(R.string.in_a_meeting)
            Status.BUSY -> return app.getString(R.string.busy)
        }
    }

    private fun getApiParams(jsonObject: JSONObject?, url: String): ApiInput {

        val header: MutableMap<String, String> = HashMap()
//        header[Constants.ApiKeys.ROLE] = Constants.ApiValues.USER
//        header[Constants.ApiKeys.AUTHORIZATION] = sharedHelper!!.token

        val apiInputs = ApiInput()
        apiInputs.context = app.applicationContext
        apiInputs.jsonObject = jsonObject
        apiInputs.url = url
        apiInputs.headers = header

        return apiInputs
    }
}

