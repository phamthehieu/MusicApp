package com.example.musicapp

import android.app.Application
import android.app.DatePickerDialog
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import java.util.Calendar

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()

        createChannelNotification(this)
    }

    companion object {
        val CHANNEL_ID = "channel_id"

        private fun createChannelNotification(context: Context) {
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
                val channel = NotificationChannel(
                    CHANNEL_ID,
                    "Channel Name",
                    NotificationManager.IMPORTANCE_DEFAULT
                )
                channel.setSound(null, null)
                channel.vibrationPattern = null

                val manager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
                manager.createNotificationChannel(channel)
            }
        }

        fun calculateAge(birthDate: Calendar): Int {
            val currentDate = Calendar.getInstance()
            var age = currentDate.get(Calendar.YEAR) - birthDate.get(Calendar.YEAR)
            if (currentDate.get(Calendar.DAY_OF_YEAR) < birthDate.get(Calendar.DAY_OF_YEAR)) {
                age--
            }
            return age
        }

        private lateinit var calendar: Calendar
        private lateinit var selectedDate: String

        fun showCalendar(context: Context, callback: (String, Any) -> Unit, additionalParameter: Any) {
            calendar = Calendar.getInstance()

            val birthDateParts = if (additionalParameter is String && additionalParameter.isNotBlank()) {
                additionalParameter.split("/")
            } else {
                listOf(calendar.get(Calendar.DAY_OF_MONTH).toString(), (calendar.get(Calendar.MONTH) + 1).toString(), calendar.get(Calendar.YEAR).toString())
            }

            val birthDay = birthDateParts[0].toInt()
            val birthMonth = birthDateParts[1].toInt() - 1
            val birthYear = birthDateParts[2].toInt()

            val datePickerDialog = DatePickerDialog(
                context,
                { _, year, monthOfYear, dayOfMonth ->
                    val selectedDateCalendar = Calendar.getInstance()
                    selectedDateCalendar.set(year, monthOfYear, dayOfMonth)
                    val age = calculateAge(selectedDateCalendar)
                    selectedDate = if (age >= 18) {
                        "$dayOfMonth/${monthOfYear + 1}/$year"
                    } else {
                        ""
                    }
                    callback(selectedDate, additionalParameter)
                },
                birthYear,
                birthMonth,
                birthDay
            )

            datePickerDialog.show()
        }

        fun getChannelId(): Any {
            return CHANNEL_ID
        }

    }
}