package com.huzaifa.kotlinmessenger.Chat

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.huzaifa.kotlinmessenger.Chat.LatestMessagesActivity.Companion.currentUser
import com.huzaifa.kotlinmessenger.R
import com.huzaifa.kotlinmessenger.models.ChatItem
import com.huzaifa.kotlinmessenger.models.ChatItemReverse
import com.huzaifa.kotlinmessenger.models.ChatMessage
import com.huzaifa.kotlinmessenger.models.User
import com.huzaifa.kotlinmessenger.utils.Constants
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import kotlinx.android.synthetic.main.activity_chat_log.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()
    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        chat_log_recyclerview.adapter = adapter
        user = intent.getParcelableExtra(Constants.USER_KEY)
        supportActionBar?.title = user?.name

        listenForMessages()

        send_message_btn.setOnClickListener {
            performMessageSend()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(Constants.USER_KEY)
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {}
            override fun onChildMoved(p0: DataSnapshot, p1: String?) {}
            override fun onChildChanged(p0: DataSnapshot, p1: String?) {}
            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage!=null){
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatItem(chatMessage.message, currentUser!!))
                    } else {
                        adapter.add((ChatItemReverse(chatMessage.message, user!!)))
                    }
                }

                chat_log_recyclerview.scrollToPosition(adapter.itemCount-1)
            }
            override fun onChildRemoved(p0: DataSnapshot) {}

        })
    }

    private fun performMessageSend() {
        val message = enter_message.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>(Constants.USER_KEY)
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()
        val to_ref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatmessage = ChatMessage(ref.key!!, message, toId!!, FirebaseAuth.getInstance().uid!!, System.currentTimeMillis()/1000)
        ref.setValue(chatmessage)
            .addOnSuccessListener {
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                enter_message.text.clear()
                chat_log_recyclerview.scrollToPosition(adapter.itemCount-1)
            }

        to_ref.setValue(chatmessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRef.setValue(chatmessage)

        val latestMessagetoRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagetoRef.setValue(chatmessage)
    }
}



