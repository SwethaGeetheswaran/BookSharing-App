package com.example.booksharingapp

class Messages{
    var date: String? = null
    var time: String? = null
    var from: String? = null
    var message:String? = null
    var to:String? = null

    constructor():this("","","","","")

    constructor(date: String?, time: String?, from: String?, message: String?, to: String?) {
        this.date = date
        this.time = time
        this.from = from
        this.message = message
        this.to = to
    }
}