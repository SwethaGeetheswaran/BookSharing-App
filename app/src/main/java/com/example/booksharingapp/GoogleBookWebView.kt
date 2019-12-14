package com.example.booksharingapp

import android.os.Bundle
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.books_webview.*

// When the user clicks on a particular book, display it in a webView.
class GoogleBookWebView : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.books_webview)

        val book_Url = intent.getStringExtra("bookUrl")
        webview.setWebViewClient(webViewCLient())

        val settings = webview.settings
        settings.javaScriptEnabled = true

        webview.loadUrl(book_Url)

    }


    class webViewCLient : WebViewClient() {
        override fun shouldOverrideUrlLoading(view: WebView, url: String): Boolean {
            return false
        }
    }
}
