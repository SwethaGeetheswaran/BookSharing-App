package com.example.booksharingapp

import org.junit.Test

import org.junit.Assert.*
import org.mockito.Mockito

class changePasswordTest {

    @Test
    fun checkIncorrectPassword() {
       val changclass = Mockito.mock(changePassword::class.java)
        val result = changclass.isValidPassword("abcd")
        assertEquals( result.toString(),"false")
    }


    @Test
    fun checkValidPassword() {
        val c1class = Mockito.mock(changePassword::class.java)
        val result = c1class.isValidPassword("Test@123")
        assertEquals( "true",result.toString())
    }


    @Test
    fun mockDistancealculator() {
        val c1class = Mockito.mock(searchBook_adapter::class.java)
        val result = c1class.distance(0.0,0.0,0.0,0.0)
        assertEquals( 0,result.toInt())
    }

    @Test
    fun calculateDistance() {
        val c1class = Mockito.mock(searchBook_adapter::class.java)
        val result = c1class.distance(41.308274,-72.9278835,41.270548399999996,-72.9469711)
        assertEquals( 2,result.toInt())
    }
}