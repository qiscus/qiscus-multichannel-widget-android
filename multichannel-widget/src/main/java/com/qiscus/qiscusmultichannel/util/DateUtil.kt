package com.qiscus.qiscusmultichannel.util

import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

/**
 * Created on : 19/08/19
 * Author     : Taufik Budi S
 * GitHub     : https://github.com/tfkbudi
 */
object DateUtil {
    private var fullDateFormat: DateFormat? = null

    init {
        fullDateFormat = SimpleDateFormat("EEEE, MMMM dd, yyyy")
    }

    fun toFullDate(date: Date): String {
        return fullDateFormat!!.format(date)
    }

    fun getLastMessageTimestamp(utcDate: Date?): String? {
        if (utcDate != null) {
            val todayCalendar = Calendar.getInstance()
            val localCalendar = Calendar.getInstance()
            localCalendar.time = utcDate

            return if (getDateStringFromDate(todayCalendar.time) == getDateStringFromDate(
                    localCalendar.time
                )
            ) {

                getTimeStringFromDate(utcDate)

            } else if (todayCalendar.get(Calendar.DATE) - localCalendar.get(Calendar.DATE) == 1) {
                "Yesterday"
            } else {
                getDateStringFromDate(utcDate)
            }
        } else {
            return null
        }
    }

    fun getTimeStringFromDate(date: Date): String {
        val dateFormat = SimpleDateFormat("HH:mm", Locale.US)
        return dateFormat.format(date)
    }

    fun getDateStringFromDate(date: Date): String {
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.US)
        return dateFormat.format(date)
    }

    fun getDateStringFromDateTimeline(date: Date): String {
        val day = SimpleDateFormat("dd", Locale.US)
        val month1 = SimpleDateFormat("MM", Locale.US)
        val years = SimpleDateFormat("yyyy", Locale.US)
        val dayText = day.format(date)
        val month = month1.format(date)
        var monthText = ""
        if (month == "01") {
            monthText = "Januari"
        } else if (month == "02") {
            monthText = "Febuari"
        } else if (month == "03") {
            monthText = "Maret"
        } else if (month == "04") {
            monthText = "April"
        } else if (month == "05") {
            monthText = "Mei"
        } else if (month == "06") {
            monthText = "Juni"
        } else if (month == "07") {
            monthText = "July"
        } else if (month == "08") {
            monthText = "Agustus"
        } else if (month == "09") {
            monthText = "September"
        } else if (month == "10") {
            monthText = "Oktober"
        } else if (month == "11") {
            monthText = "November"
        } else if (month == "12") {
            monthText = "Desember"
        }
        val yearsText = years.format(date)
        val time = getTimeStringFromDate(date)
        return "$dayText $monthText $yearsText"
    }

}