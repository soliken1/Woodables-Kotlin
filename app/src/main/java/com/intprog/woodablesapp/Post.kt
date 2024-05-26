package com.intprog.woodablesapp

class Post {
    // Getters and setters
    @kotlin.jvm.JvmField
    var title: String? = null
    var message: String? = null
    var userName: String? = null
    var status: String? = null

    constructor()
    constructor(title: String?, message: String?, userName: String?, status: String?) {
        this.title = title
        this.message = message
        this.userName = userName
        this.status = status
    }
}
