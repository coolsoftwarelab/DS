package com.ds.soonda.model

import android.os.Parcelable
import com.google.gson.Gson
import kotlinx.parcelize.Parcelize

@Parcelize
data class AdInfoDto(
    val state: String,
    val message: String,
    val rentNumber: String,
    val bgTimer: Int,
    val adCount: Int,
    val ad: ArrayList<Ad>
) : Parcelable

// 광고정보
@Parcelize
data class Ad(
    val template: Int,
    val url1: String,
    val url2: String,
    val url3: String,
    val index: Int, // 1~1000
    val time: Int,  // 광고송출시간
    val effect: Int
) : Parcelable {
    fun toJson() {
        Gson().newBuilder().create().toJson(this)
    }
}



