package com.example.booksharingapp

class Book {
    var title: String? = null
    var Author: String? = null
    var bookImage: String? = null

    constructor() : this("", "", "")
    constructor(
        title: String?,
        Author: String?,
        bookImage: String?
    ) {
        this.title = title
        this.Author = Author
        this.bookImage = bookImage
    }

}