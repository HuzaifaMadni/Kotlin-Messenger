package com.huzaifa.kotlinmessenger.models

import com.huzaifa.kotlinmessenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.newmessage_rows.view.*

class ChatItem(val message: String, val currentUser: User?): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_message_rows
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.newmessage_name.text = message

        Picasso.get().load(currentUser?.profileImage).into(viewHolder.itemView.newmessage_image)

    }

}