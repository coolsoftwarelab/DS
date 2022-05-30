package com.ds.soonda.manager

import android.app.DownloadManager
import android.content.Context
import android.net.Uri
import android.os.Environment
import android.webkit.MimeTypeMap


class DownloadHelper {
    companion object {

        fun download(context: Context, uri: Uri): Long? {
            val fileExtension = MimeTypeMap.getFileExtensionFromUrl(uri.toString())

            val downloadManager =
                context.getSystemService(Context.DOWNLOAD_SERVICE) as DownloadManager?
            val req = DownloadManager.Request(uri)

            req.apply {
                setTitle("Download File")
                setDescription("Downloading....")
                setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE)
                setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                    "Download.$fileExtension"
                )
                setMimeType("*/*")
            }
            return downloadManager?.enqueue(req)
        }
    }

}