package com.example.booksharingapp

class UsersList{
    var firstName: String? = null
    var lastName: String? = null
    var ProfileImage: String? = null

    constructor():this("","","")

    constructor(firstName: String?, lastName: String?, profileimage: String?) {
        this.firstName = firstName
        this.lastName = lastName
        this.ProfileImage = profileimage
    }


}