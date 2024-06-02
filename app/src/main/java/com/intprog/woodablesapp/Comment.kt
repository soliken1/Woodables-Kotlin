package com.intprog.woodablesapp

import java.util.Date

data class Comment(
    val username: String = "",
    val comment: String = "",
    val postId: String = "",
    val date: Date? = null
)