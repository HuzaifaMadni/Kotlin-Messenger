package com.huzaifa.kotlinmessenger.models

class ChatMessage(val id: String, val message: String, val toId: String, val fromId: String, val timestamp: Long){
    constructor() : this("","", "", "", -1)
}