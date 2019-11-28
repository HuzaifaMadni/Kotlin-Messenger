package com.huzaifa.kotlinmessenger.models

import android.os.Parcelable
import kotlinx.android.parcel.Parcelize

@Parcelize
class User (val uid:String, val name: String, val profileImage:String) : Parcelable {
    constructor() : this("", "", "")
}