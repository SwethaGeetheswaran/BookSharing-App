package com.example.booksharingapp



import android.content.AsyncTaskLoader
import android.content.Context


// Use loaders to fetch GoogleBooks Api query.
class GoogleBooksLoader(context: Context?, url: String?) : AsyncTaskLoader<List<GoogleBooks?>?>(context) {
    var url: String? = null
    override fun onStartLoading() {
        super.onStartLoading()
        forceLoad()
    }

    override fun loadInBackground(): List<GoogleBooks?>? {
        if (url == null) {
            return null
        }
        arrayList = QueryUtils.fetchBooksData(url!!)
        return arrayList
    }

    companion object {
        var arrayList: List<GoogleBooks?>? = null
    }

    init {
        if (url != null) {
            this.url = url
        }
    }
}
