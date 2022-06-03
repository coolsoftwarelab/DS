package com.ds.soonda.util

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.content.Context
import android.os.Environment
import android.os.StatFs
import android.provider.Settings
import com.ds.soonda.model.AcroMediaFileType
import java.io.File
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
            builder.setPositiveButton("확인", null)
            builder.create().show()
        }

        fun getAcroMediaFileType(filePath: String): AcroMediaFileType {
            return if (filePath.contains("png") || filePath.contains("jpg")) {
                AcroMediaFileType.IMAGE
            } else {
                AcroMediaFileType.VIDEO
            }
        }

        fun getAvailableInternalMemorySize(): Long {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.path)
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
//            return TmpGetStorageCapacity.formatSize(availableBlocks * blockSize)
            return availableBlocks * blockSize / 1024 / 1024 / 1024 // GB
        }

    }
}


