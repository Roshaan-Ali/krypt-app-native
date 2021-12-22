package com.pyra.krpytapplication.utils

import android.content.res.Resources
import android.text.format.DateUtils
import com.pyra.krpytapplication.R
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit


fun String.longDateToDisplayTimeString(showDateForOlderTime: Boolean = false): String {
    try {
        val date = Date(this.toLong())
        if (showDateForOlderTime) {
            if (date.isToday()) {
                val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
                return format.format(date)
            } else if (date.isYesterday()) {
                return Resources.getSystem().getString(R.string.yesterday)
            } else {
                val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                return format.format(date)
            }

        } else {
            val format = SimpleDateFormat("hh:mm a", Locale.getDefault())
            return format.format(date)
        }
    } catch (e: Exception) {
        return ""
    }
}

fun Date.isYesterday(): Boolean {
    return DateUtils.isToday(this.time + DateUtils.DAY_IN_MILLIS)
}

fun Date.isToday(): Boolean {
    return DateUtils.isToday(this.time)
}

fun String.getDisplayTime(): String {
    val date = Date(this.toLong())
    return when {
        date.isToday() -> "today"
        date.isYesterday() -> "yesterday"
        else -> {
            val format = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            format.format(date)
        }
    }
}


fun getFormatedDate(
    date: String?,
    inputFormat: String,
    outputFormat: String
): String? {
    var spf =
        SimpleDateFormat(inputFormat, Locale.ENGLISH)
    spf.timeZone = TimeZone.getTimeZone("UTC")
    var newDate: Date? = null
    try {
        newDate = spf.parse(date)
    } catch (e: java.text.ParseException) {
        e.printStackTrace()
        return "Not Updated"
    }
    newDate?.let {
        spf = SimpleDateFormat(outputFormat, Locale.ENGLISH)
        return spf.format(newDate)
    }
    return "Not Updated"

}


fun isSubScriptionEnded(end: String, format: String): Boolean {


    val sdf = SimpleDateFormat(format, Locale.ENGLISH)
    val endDate: Date? = sdf.parse(end)

    endDate?.let {




        val currentDate1 : Date = Calendar.getInstance().time
        val dateString : String = sdf.format(currentDate1)

        val currentDate : Date? = sdf.parse(dateString)

        currentDate?.let {

            val currentTimeStamp: Long = currentDate.time
            val endDateTimestamp = endDate.time

            val diff = currentTimeStamp - endDateTimestamp

            val diffInSec: Long = TimeUnit.MILLISECONDS.toSeconds(diff)

            return diffInSec >= 0
        }
    }

    return false
}


