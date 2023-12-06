/** "ConvertUtils.kt"
 *  Description: A Utility class of different frequently used conversions used
 *               in this application
 *  Last Modified: December 3, 2023
 * */
package com.example.cooksmart.utils

import java.text.SimpleDateFormat
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.temporal.ChronoUnit
import java.util.Locale

class ConvertUtils {

    companion object {
        /** "stringToArrayList"
         *  Description: Parses a string into an array list
         * */
        // Inspired by https://stackoverflow.com/questions/53512575/how-to-convert-a-string-sentence-to-arraylist-in-kotlin
        fun stringToArrayList(str: String): List<String> {
            // Discard the starting and ending square brackets
            val strNoArray = str.substring(1, str.length-1)
            // Only split if the letters following the comma and space is not a lowercase letter
            val list = strNoArray.trim().split(", (?![a-z])".toRegex()).filter { it.isNotEmpty() }
            // Trim leading and trailing whitespace
            val trimmedSpacesList = list.map {it.trim()}
            return ArrayList<String>(trimmedSpacesList)
        }

        /** "longToDateString"
         *  Description: Converts a long object into a date string
         * */
        // SimpleDateFormat from https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
        fun longToDateString(long: Long): String {
            val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            return dateFormat.format(long).uppercase(Locale.getDefault())
        }

        /** "convertLongToDate"
         *  Description: Converts a long into a local date object
         * */
        fun convertLongToDate(dateMilli : Long): LocalDate {
            val date = Instant.ofEpochMilli(dateMilli)
            return date.atZone(ZoneId.systemDefault()).toLocalDate()
        }

        /** "daysExpiry"
         *  Description: Determine the days until expiry between the current date and object date
         * */
        fun daysExpiry(expiryDateLong : Long, selectedDate : Long): Long {
            val expiryDate = convertLongToDate(expiryDateLong)
            val currentDate = convertLongToDate(selectedDate)
            return ChronoUnit.DAYS.between(currentDate, expiryDate)
        }
    }
}