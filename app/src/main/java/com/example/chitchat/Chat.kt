package com.example.chitchat
data class Chats(var count: Int, var date: String, var messages: List<Chat>)
data class Chat(
    var _id: String,
    var client: String,
    var date: String,
    var dislikes: Int,
    var ip: String,
    var likes: Int,
    var loc: List<Float>,
    var message: String
    )