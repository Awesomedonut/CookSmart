package com.example.cooksmart.utils

import java.text.SimpleDateFormat
import java.util.Locale

class ConvertUtils {

    companion object {
        // Inspired by https://stackoverflow.com/questions/53512575/how-to-convert-a-string-sentence-to-arraylist-in-kotlin
        fun stringToArrayList(str: String): List<String> {
            // Discard the starting and ending square brackets
            val strNoArray = str.substring(1, str.length-1)
            val list = strNoArray.trim().splitToSequence(',').filter{it.isNotEmpty()}.toList()
            // Trim leading and trailing whitespace
            val trimmedSpacesList = list.map {it.trim()}
            return ArrayList<String>(trimmedSpacesList)
        }

        // SimpleDateFormat from https://developer.android.com/reference/kotlin/android/icu/text/SimpleDateFormat
        fun longToDateString(long: Long): String {
            val dateFormat = SimpleDateFormat("MMM dd yyyy", Locale.getDefault())
            return dateFormat.format(long).uppercase(Locale.getDefault())
        }
    }
}