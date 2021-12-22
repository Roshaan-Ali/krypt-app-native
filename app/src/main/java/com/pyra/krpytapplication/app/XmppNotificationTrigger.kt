package com.pyra.krpytapplication.app

import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.widget.Toast
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.utils.Constants
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.network.UrlHelper
import org.json.JSONObject

class XmppNotificationTrigger : Service() {

    override fun onBind(p0: Intent?): IBinder? {
        return null
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
//        Toast.makeText(this,"OnServiceStarted",Toast.LENGTH_SHORT).show()
        return START_STICKY
    }

    override fun onTaskRemoved(rootIntent: Intent?) {

//        Toast.makeText(this,"OnTaskRemoved",Toast.LENGTH_SHORT).show()
        LogUtil.d("OnTaskRemoved ","OnTaskRemoved")
        super.onTaskRemoved(rootIntent)

        if (SharedHelper(this).kryptKey != "") {
            val jsonObject = JSONObject()
            jsonObject.put(Constants.ApiKeys.USERNAME, SharedHelper(this).kryptKey)

            val input = ApiInput()
            input.context = this
            input.url = UrlHelper.TRIGGERNOTIFICATION
            input.jsonObject = jsonObject

            com.pyra.network.Api.postMethod(input, object : ApiResponseCallback {

                override  fun setResponseSuccess(jsonObject: JSONObject) {}
                override fun setErrorResponse(error: String) {}
            })
        }

    }

    override fun onDestroy() {
        super.onDestroy()
        Toast.makeText(this,"OnDestroyed",Toast.LENGTH_SHORT).show()
    }
}