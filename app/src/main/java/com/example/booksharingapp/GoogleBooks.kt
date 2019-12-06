package com.example.booksharingapp

import android.os.Parcel
import android.os.Parcelable

//Implemented Parcelable to serialize the book Object so that it can pass over.
class GoogleBooks : Parcelable {
    var title: String?
    var author: String?
    var infoUrl: String?
    var imageUrl: String?
    var publisher: String?

    constructor(
        title: String?,
        author: String?,
        infoUrl: String?,
        imageUrl: String?,
        publisher: String?
    ) {
        this.title = title
        this.author = author
        this.infoUrl = infoUrl
        this.imageUrl = imageUrl
        this.publisher = publisher
    }

    private constructor(`in`: Parcel) {
        title = `in`.readString()
        author = `in`.readString()
        infoUrl = `in`.readString()
        imageUrl = `in`.readString()
        publisher = `in`.readString()
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(parcel: Parcel, i: Int) {
        parcel.writeString(title)
        parcel.writeString(author)
        parcel.writeString(infoUrl)
        parcel.writeString(imageUrl)
        parcel.writeString(publisher)
    }

    companion object CREATOR : Parcelable.Creator<GoogleBooks> {
        override fun createFromParcel(parcel: Parcel): GoogleBooks {
            return GoogleBooks(parcel)
        }

        override fun newArray(size: Int): Array<GoogleBooks?> {
            return arrayOfNulls(size)
        }
    }
}