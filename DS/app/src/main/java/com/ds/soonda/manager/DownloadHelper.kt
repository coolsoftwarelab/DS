package com.ds.soonda.manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.util.Log
import android.webkit.MimeTypeMap
import org.apache.commons.io.FilenameUtils


class DownloadHelper {
    companion object {

        fun download(context: Context, uri: Uri): Long? {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            val req = DownloadManager.Request(uri)

            FilenameUtils.getName(uri.toString())
            Log.d(
                "JDEBUG",
                "FilenameUtils.getName(uri.toString()) :  ${FilenameUtils.getName(uri.toString())}"
            )


            req.apply {
                setTitle("Download File")
                setDescription("Downloading....")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationInExternalPublicDir(
                    Environment.DIRECTORY_DOWNLOADS,
                    "/acro/" + FilenameUtils.getName(uri.toString())
                )
                setMimeType("*/*")
            }
            return downloadManager?.enqueue(req)
        }
    }

}