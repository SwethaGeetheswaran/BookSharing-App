package com.example.booksharingapp

class Post{
    var UID : String? = null
    var fullname: String? = null
    var postimage: String? = null
    var profileimage: String? = null
    var description: String? = null
    var Time: String? = null
    var Date: String? = null

    constructor():this("","","","","","",""){ }
    constructor(
        UID: String?,
        fullname: String?,
        postimage: String?,
        profileimage: String?,
        description: String?,
        Time: String?,
        Date: String?
    ) {
        this.UID = UID
        this.fullname = fullname
        this.postimage = postimage
        this.profileimage = profileimage
        this.description = description
        this.Time = Time
        this.Date = Date
    }
}