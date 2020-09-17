package com.samriddha.letschartapp.others

import java.text.SimpleDateFormat
import java.util.*

object DateTimeProvider {

    fun getCurrentDate():String{
        val calender =  Calendar.getInstance()
        val dateFormat = SimpleDateFormat("MMM dd,yyyy")
        return dateFormat.format(calender.time)
    }

    fun getCurrentTime():String{
        val calender1 =  Calendar.getInstance()
        val timeFormat = SimpleDateFormat("hh:mm a")
        return timeFormat.format(calender1.time)
    }

}