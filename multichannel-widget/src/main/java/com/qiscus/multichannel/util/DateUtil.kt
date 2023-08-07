package com.qiscus.multichannel.util

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

            } else if (compareToYesterday(todayCalendar, localCalendar)) {
                "Yesterday"
            } else {
                getDateStringFromDate(utcDate)
            }
        } else {
            return null
        }
    }

    private fun compareToYesterday(todayCalendar: Calendar, localCalendar: Calendar): Boolean {
        return todayCalendar.time.after(localCalendar.time)
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
        val month = SimpleDateFormat("MM", Locale.US)
        val years = SimpleDateFormat("yyyy", Locale.US)

        val dayText = day.format(date)
        val monthText =  when (month.format(date)) {
            "01" -> "Januari"
            "02" -> "Februari"
            "03" -> "Maret"
            "04" -> "April"
            "05" -> "Mei"
            "06" -> "Juni"
            "07" -> "Juli"
            "08" -> "Agustus"
            "09" -> "September"
            "10" -> "Oktober"
            "11" -> "November"
            else -> "Desember"
        }
        val yearsText = years.format(date)

        return "$dayText $monthText $yearsText"
    }

}