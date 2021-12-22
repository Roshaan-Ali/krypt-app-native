package com.pyra.network

import com.android.volley.*
import com.android.volley.toolbox.JsonObjectRequest
import com.app.hakeemUser.network.ApiInput
import com.pyra.krpytapplication.app.MyApp
import com.pyra.krpytapplication.R
import com.pyra.krpytapplication.utils.LogUtil
import com.pyra.krpytapplication.utils.isNetworkConnected
import com.pyra.krpytapplication.repositories.interfaces.ApiResponseCallback
import com.pyra.krpytapplication.utils.SharedHelper
import com.pyra.krpytapplication.utils.moveToKryptCodeActivity

object Api {

    private var MY_SOCKET_TIMEOUT_MS = 50000

    fun postMethod(input: ApiInput, apiResponseCallback: ApiResponseCallback) {
        if (input.context!!.isNetworkConnected()) {

            LogUtil.d("ApiRequest", input.url + "  " + input.jsonObject.toString())

            val headerParam = HashMap<String, String>()
            headerParam["Authorization"] = SharedHelper(input.context!!).token
            input.headers = headerParam

            val jsonObjectRequest =
                object : JsonObjectRequest(Method.POST, input.url, input.jsonObject, {
                    apiResponseCallback.setResponseSuccess(it)
                    LogUtil.d("ApiResponse", input.url + "  " + it.toString())
                }, {
                    LogUtil.d("ApiResponse", input.url + "  " + it.toString())
                    if (it is TimeoutError || it is NoConnectionError) {
                        input.context?.getString(R.string.no_internet_connection)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is AuthFailureError) {
                        input.context?.moveToKryptCodeActivity()
                        input.context?.getString(R.string.session_expired)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is ServerError) {
                        input.context?.getString(R.string.server_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is NetworkError) {
                        input.context?.getString(R.string.network_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is ParseError) {
                        input.context?.getString(R.string.parsing_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else {
                        input.context?.getString(R.string.network_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        LogUtil.d("ApiHeader", input.url + "  " + input.headers)
                        return if (input.headers != null) {
                            val params: HashMap<String, String> = HashMap()

                            for ((key, value) in input.headers!!) {
                                params[key] = value
                            }
                            params
                        } else {
                            super.getHeaders()
                        }
                    }
                }
            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            MyApp.getInstance().addrequestToQueue(jsonObjectRequest)
        } else {
            apiResponseCallback.setErrorResponse("No Internet Connection")
        }
    }

    fun getMethod(input: ApiInput, apiResponseCallback: ApiResponseCallback) {
        if (input.context!!.isNetworkConnected()) {

            val jsonObjectRequest =
                object : JsonObjectRequest(Request.Method.GET, input.url, input.jsonObject, {
                    apiResponseCallback.setResponseSuccess(it)
                }, {

                    if (it is TimeoutError || it is NoConnectionError) {
                        input.context?.getString(R.string.no_internet_connection)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is AuthFailureError) {
//                        moveToLoginActivity(input.context)
                        input.context?.getString(R.string.session_expired)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is ServerError) {
                        input.context?.getString(R.string.server_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is NetworkError) {
                        input.context?.getString(R.string.network_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else if (it is ParseError) {
                        input.context?.getString(R.string.parsing_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    } else {
                        input.context?.getString(R.string.network_error)
                            ?.let { it1 -> apiResponseCallback.setErrorResponse(it1) }
                    }
                }) {
                    override fun getHeaders(): MutableMap<String, String> {
                        return if (input.headers != null) {
                            val params: HashMap<String, String> = HashMap<String, String>()

                            for ((key, value) in input.headers!!) {
                                params[key] = value
                            }
                            params
                        } else {
                            super.getHeaders()
                        }
                    }
                }

            jsonObjectRequest.retryPolicy = DefaultRetryPolicy(
                MY_SOCKET_TIMEOUT_MS,
                DefaultRetryPolicy.DEFAULT_MAX_RETRIES,
                DefaultRetryPolicy.DEFAULT_BACKOFF_MULT
            )

            MyApp.getInstance().addrequestToQueue(jsonObjectRequest)
        } else {
            apiResponseCallback.setErrorResponse("No Internet Connection")
        }
    }
}