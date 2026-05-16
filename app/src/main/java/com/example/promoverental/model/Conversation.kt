package com.example.promoverental.model

data class Conversation(
    val lastMessage: Message,
    val otherUser: Profile,
    val unreadCount: Int = 0
)
