package com.huzaifa.kotlinmessenger.models

import com.huzaifa.kotlinmessenger.R
import com.squareup.picasso.Picasso
import com.xwray.groupie.GroupieViewHolder
import com.xwray.groupie.Item
import kotlinx.android.synthetic.main.chat_message_rows_reverse.view.*

class ChatItemReverse(val message: String, val user: User): Item<GroupieViewHolder>(){
    override fun getLayout(): Int {
        return R.layout.chat_message_rows_reverse
    }

    override fun bind(viewHolder: GroupieViewHolder, position: Int) {
        viewHolder.itemView.chat_message_reverse.text = message

        Picasso.get().load(user.profileImage).into(viewHolder.itemView.chat_image_reverse)
    }

}
