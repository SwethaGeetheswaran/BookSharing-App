package com.example.booksharingapp

import java.util.regex.Matcher
import java.util.regex.Pattern

// To validate password
fun isValidPassword(password: String): Boolean {
    val pattern: Pattern
    val matcher: Matcher
    val PASSWORD_PATTERN = "^(?=.*[0-9])(?=.*[A-Z])(?=.*[@#$%^&+=!])(?=\\S+$).{4,}$"
    pattern = Pattern.compile(PASSWORD_PATTERN)
    matcher = pattern.matcher(password)
    return matcher.matches()
}


fun distance(fromLat: Double, fromLon: Double, toLat: Double, toLon: Double): Double {
    val radius = 3958.756
    val curLat = Math.toRadians(fromLat)
    val curLong = Math.toRadians(fromLon)
    val deltaLat = Math.toRadians(toLat - fromLat)
    val deltaLon = Math.toRadians(toLon - fromLon)
    val a = Math.sin(deltaLat / 2) * Math.sin(deltaLat / 2) +
            Math.cos(curLat) * Math.cos(curLong) *
            Math.sin(deltaLon / 2) * Math.sin(deltaLon/ 2)
    val c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a))
    return radius * c
}
