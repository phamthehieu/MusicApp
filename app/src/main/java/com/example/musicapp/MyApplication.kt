package com.example.musicapp

import android.app.Application
import android.app.DatePickerDialog
import android.content.Context
import android.util.Log
import android.widget.Toast
import java.util.Calendar

class MyApplication : Application() {

    override fun onCreate() {
        super.onCreate()
    }

    companion object {

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

    }
}