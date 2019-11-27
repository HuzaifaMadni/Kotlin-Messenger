package com.huzaifa.kotlinmessenger

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.huzaifa.kotlinmessenger.LatestMessagesActivity.Companion.currentUser
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupAdapter
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.activity_chat_log.*
import kotlinx.android.synthetic.main.activity_new_message.*
import kotlinx.android.synthetic.main.chat_message_rows_reverse.view.*
import kotlinx.android.synthetic.main.newmessage_rows.view.*

class ChatLogActivity : AppCompatActivity() {

    val adapter = GroupAdapter<GroupieViewHolder>()

    var user: User? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_chat_log)

        chat_log_recyclerview.adapter = adapter

        user = intent.getParcelableExtra("USER_KEY")
        supportActionBar?.title = user?.name

        listenForMessages()

        send_message_btn.setOnClickListener {
            performMessageSend()
        }
    }

    private fun listenForMessages() {
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("USER_KEY")
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId")

        ref.addChildEventListener(object: ChildEventListener{
            override fun onCancelled(p0: DatabaseError) {
            }

            override fun onChildMoved(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildChanged(p0: DataSnapshot, p1: String?) {
            }

            override fun onChildAdded(p0: DataSnapshot, p1: String?) {
                val chatMessage = p0.getValue(ChatMessage::class.java)
                if (chatMessage!=null){
                    if(chatMessage.fromId == FirebaseAuth.getInstance().uid){
                        adapter.add(ChatItem(chatMessage.message, currentUser!!))
                    } else {
                        adapter.add((ChatItemReverse(chatMessage.message, user!!)))
                    }
                }
            }

            override fun onChildRemoved(p0: DataSnapshot) {
            }

        })
    }

    class ChatMessage(val id: String, val message: String, val toId: String, val fromId: String, val timestamp: Long){
        constructor() : this("","", "", "", -1)
    }
    private fun performMessageSend() {
        val message = enter_message.text.toString()
        val fromId = FirebaseAuth.getInstance().uid
        val user = intent.getParcelableExtra<User>("USER_KEY")
        val toId = user?.uid

        val ref = FirebaseDatabase.getInstance().getReference("/user-messages/$fromId/$toId").push()

        val to_ref = FirebaseDatabase.getInstance().getReference("/user-messages/$toId/$fromId").push()

        val chatmessage = ChatMessage(ref.key!!, message, toId!!, FirebaseAuth.getInstance().uid!!, System.currentTimeMillis()/1000)
        ref.setValue(chatmessage)
            .addOnSuccessListener {
                Toast.makeText(this, "Success", Toast.LENGTH_LONG).show()
                enter_message.text.clear()
                //chat_log_recyclerview.scrollToPosition(adapter.itemCount)
            }

        to_ref.setValue(chatmessage)

        val latestMessageRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$toId/$fromId")
        latestMessageRef.setValue(chatmessage)

        val latestMessagetoRef = FirebaseDatabase.getInstance().getReference("/latest-messages/$fromId/$toId")
        latestMessagetoRef.setValue(chatmessage)
    }
}

class ChatItem(val message: String, val currentUser: User?): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_message_rows
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.newmessage_name.text = message

        Picasso.get().load(currentUser?.profileImage).into(viewHolder.itemView.newmessage_image)

    }

}

class ChatItemReverse(val message: String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_message_rows_reverse
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chat_message_reverse.text = message

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.chat_image_reverse)
    }

}
