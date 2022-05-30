package com.ds.soonda.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.provider.Settings
import java.io.UnsupportedEncodingException
import java.util.*

class Utils {
    companion object {
        fun generateRandomNumber(): String {
            var result = ""
            val randomNum = List(6) {
                Random().nextInt(9)
            }

            randomNum.forEach {
                result += it
            }
            return result
        }

        @SuppressLint("HardwareIds")
        fun getUUID(context: Context): String {
            var uuid: UUID? = null
            val androidId =
                Settings.Secure.getString(context.contentResolver, Settings.Secure.ANDROID_ID)
            uuid =
                if (androidId == null || androidId.isEmpty() || androidId == "9774d56d682e549c") {
                    UUID.randomUUID()
                } else {
                    try {
                        UUID.nameUUIDFromBytes(androidId.toByteArray(charset("utf8")))
                    } catch (e: UnsupportedEncodingException) {
                        e.printStackTrace()
                        UUID.randomUUID()
                    }
                }
            return uuid.toString()
        }

        fun showSimpleAlert(context: Context, msg: String) {
            val builder = AlertDialog.Builder(context)
            builder.setTitle("알림")
            builder.setMessage(msg)
            builder.create().show()
        }
    }

}


