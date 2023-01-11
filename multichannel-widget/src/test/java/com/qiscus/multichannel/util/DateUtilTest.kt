package com.qiscus.multichannel.util

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.text.DateFormat
import java.text.ParseException
import java.text.SimpleDateFormat
import java.util.*

internal class DateUtilTest {

    private val filterSdf: DateFormat =
        SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US).apply {
            timeZone = TimeZone.getTimeZone("UTC")
        }

    @Test
    fun toFullDateTest() {
        assertNotNull(
            DateUtil.toFullDate(Date())
        )
    }

    @Test
    fun getLastMessageTimestampTest() {
        assertNull(
            DateUtil.getLastMessageTimestamp(null)
        )
        assertNotNull(
            DateUtil.getLastMessageTimestamp(Date())
        )

        val cal = Calendar.getInstance().apply {
            add(Calendar.DATE, -1)
        }
        assertEquals(
            DateUtil.getLastMessageTimestamp(cal.time), "Yesterday"
        )

        val fullCal = Calendar.getInstance().apply {
            add(Calendar.DATE, -2)
        }
        assertNotNull(
            DateUtil.getLastMessageTimestamp(fullCal.time)
        )
    }

    @Test
    fun getTimeStringFromDateTest() {
        assertNotNull(
            DateUtil.getTimeStringFromDate(Date())
        )
    }

    @Test
    fun getDateStringFromDateTest() {
        assertNotNull(
            DateUtil.getDateStringFromDate(Date())
        )
    }

    @Test
    fun getDateStringFromDateTimelineTest() {
        setDate("01", "Januari")
        setDate("02", "Februari")
        setDate("03", "Maret")
        setDate("04", "April")
        setDate("05", "Mei")
        setDate("06", "Juni")
        setDate("07", "Juli")
        setDate("08", "Agustus")
        setDate("09", "September")
        setDate("10", "Oktober")
        setDate("11", "November")
        setDate("12", "Desember")
    }

    private fun setDate(number: String, name: String) {
        try {
            val dateTime = DateUtil.getDateStringFromDateTimeline(
                filterSdf.parse(
                    "2022-$number-01T22:22:22Z"
                ) as Date
            )

            assertEquals(
                "02 $name 2022", dateTime
            )
        } catch (e: ParseException) {
            e.printStackTrace()
        }

    }
}