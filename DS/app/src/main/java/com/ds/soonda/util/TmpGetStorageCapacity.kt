package com.ds.soonda.util

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.os.storage.StorageManager
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.content.getSystemService
import java.io.File
import java.util.*


class TmpGetStorageCapacity {
    companion object {

        val ERROR = "error"


        // Checks if a volume containing external storage is available
// for read and write.
        fun isExternalStorageWritable(): Boolean {
            return Environment.getExternalStorageState() == Environment.MEDIA_MOUNTED
        }

        // Checks if a volume containing external storage is available to at least read.
        fun isExternalStorageReadable(): Boolean {
            return Environment.getExternalStorageState() in
                    setOf(Environment.MEDIA_MOUNTED, Environment.MEDIA_MOUNTED_READ_ONLY)
        }

        fun externalMemoryAvailable(): Boolean {
            return Environment.getExternalStorageState() ==
                    Environment.MEDIA_MOUNTED
        }

        fun getAvailableInternalMemorySize(): String? {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.getPath())
            val blockSize = stat.blockSizeLong
            val availableBlocks = stat.availableBlocksLong
            return formatSize(availableBlocks * blockSize)
        }

        fun getTotalInternalMemorySize(): String? {
            val path: File = Environment.getDataDirectory()
            val stat = StatFs(path.getPath())
            val blockSize = stat.blockSizeLong
            val totalBlocks = stat.blockCountLong
            return formatSize(totalBlocks * blockSize)
        }

        fun getAvailableExternalMemorySize(): String? {
            return if (externalMemoryAvailable()) {
                val path: File = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.getPath())
                val blockSize = stat.blockSizeLong
                val availableBlocks = stat.availableBlocksLong
                formatSize(availableBlocks * blockSize)
            } else {
                ERROR
            }
        }

        fun getTotalExternalMemorySize(): String? {
            return if (externalMemoryAvailable()) {
                val path: File = Environment.getExternalStorageDirectory()
                val stat = StatFs(path.getPath())
                val blockSize = stat.blockSizeLong
                val totalBlocks = stat.blockCountLong
                formatSize(totalBlocks * blockSize)
            } else {
                ERROR
            }
        }

        fun formatSize(size: Long): String? {
            var size = size
            var suffix: String? = null
            if (size >= 1024) {
                suffix = "KB"
                size /= 1024
                if (size >= 1024) {
                    suffix = "MB"
                    size /= 1024
                }
            }
            val resultBuffer = StringBuilder(java.lang.Long.toString(size))
            var commaOffset = resultBuffer.length - 3
            while (commaOffset > 0) {
                resultBuffer.insert(commaOffset, ',')
                commaOffset -= 3
            }
            if (suffix != null) resultBuffer.append(suffix)
            return resultBuffer.toString()
        }


        @RequiresApi(Build.VERSION_CODES.O)
        fun queryAvailableSpace(context: Context, fileDir: File) {
            // App needs 10 MB within internal storage.
            val NUM_BYTES_NEEDED_FOR_MY_APP = 1024 * 1024 * 10L;

            val storageManager = context.getSystemService<StorageManager>()!!
            val appSpecificInternalDirUuid: UUID = storageManager.getUuidForPath(fileDir)
            val availableBytes: Long =
                storageManager.getAllocatableBytes(appSpecificInternalDirUuid)
            Log.d("JDEBUG", "availableBytes : $availableBytes")
            if (availableBytes >= NUM_BYTES_NEEDED_FOR_MY_APP) {
                storageManager.allocateBytes(
                    appSpecificInternalDirUuid, NUM_BYTES_NEEDED_FOR_MY_APP
                )
            } else {
                val storageIntent = Intent().apply {
                    // To request that the user remove all app cache files instead, set
                    // "action" to ACTION_CLEAR_APP_CACHE.
//                    action = ACTION_MANAGE_STORAGE
                }
            }

        }
    }
}