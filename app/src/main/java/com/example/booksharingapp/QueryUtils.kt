package com.example.booksharingapp

import org.json.JSONException
import org.json.JSONObject
import java.io.BufferedReader
import java.io.IOException
import java.io.InputStream
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.MalformedURLException
import java.net.URL
import java.nio.charset.Charset
import java.util.*

object QueryUtils {
    private var json: String? = null
    const val TAG = "QueryUtils"
    fun fetchBooksData(url: String): List<GoogleBooks?>? {
        var arrayList: ArrayList<GoogleBooks?>? = null
        try {
            Thread.sleep(1000)
            val Url = createUrl(url)
            json = makeHttpRequest(Url)
            arrayList = extractJson(json)
        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: JSONException) {
            e.printStackTrace()
        } catch (e: InterruptedException) {
            e.printStackTrace()
        }
        return arrayList
    }

    private fun createUrl(url: String): URL? {
        var Url: URL? = null
        try {
            Url = URL(url)
        } catch (e: MalformedURLException) {
            e.printStackTrace()
        }
        return Url
    }

    @Throws(IOException::class)
    private fun makeHttpRequest(url: URL?): String? {
        var httpURLConnection: HttpURLConnection? = null
        lateinit var inputStream: InputStream
        var json: String? = null
        if (url != null) {
            httpURLConnection = url.openConnection() as HttpURLConnection
            httpURLConnection.readTimeout = 10000
            httpURLConnection.connectTimeout = 15000
            httpURLConnection.requestMethod = "GET"
            httpURLConnection.connect()
        }
        if (httpURLConnection!!.responseCode == 200) {
            inputStream = httpURLConnection.inputStream
            json = streamToJson(inputStream)
        }
        return json
    }

    @Throws(IOException::class)
    private fun streamToJson(inputStream: InputStream?): String {
        val output = StringBuilder()
        if (inputStream != null) {
            val inputStreamReader =
                InputStreamReader(inputStream, Charset.forName("UTF-8"))
            val bufferedReader = BufferedReader(inputStreamReader)
            var line = bufferedReader.readLine()
            while (line != null) {
                output.append(line)
                line = bufferedReader.readLine()
            }
        }
        return output.toString()
    }

    @Throws(JSONException::class)
    private fun extractJson(json: String?): ArrayList<GoogleBooks?> {
        var title = "title"
        var author = "author"
        var infoUrl = "infoUrl"
        var imageUrl = "imageUrl"
        var publisher = "publisher"
        val bookList: ArrayList<GoogleBooks?> = ArrayList<GoogleBooks?>()
        val jsonObject = JSONObject(json!!)
        val items = jsonObject.getJSONArray("items")
        for (i in 0 until items.length()) {
            val jsonObject1 = items.getJSONObject(i)
            val volumeInfo = jsonObject1.getJSONObject("volumeInfo")
            title = volumeInfo.getString("title")
            if (volumeInfo.has("publisher")) {
                publisher = volumeInfo.getString("publisher")
            }
            if (volumeInfo.has("authors")) {
                val authors = volumeInfo.getJSONArray("authors")
                author = authors.getString(0)
            }
            infoUrl = volumeInfo.getString("infoLink")
            if (volumeInfo.has("imageLinks")) {
                val imageLinks = volumeInfo.getJSONObject("imageLinks")
                imageUrl = imageLinks.getString("smallThumbnail")
            }
            val bookItem = GoogleBooks(title, author, infoUrl, imageUrl, publisher)
            bookList.add(bookItem)
        }
        return bookList
    }
}
