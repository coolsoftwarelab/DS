package com.ds.soonda.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.databinding.ActivityDownloadContentsBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.manager.DownloadHelper
import com.ds.soonda.model.Ad
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import java.util.function.Predicate

class DownloadContentsActivity : AppCompatActivity() {
//    private val TMP_FILE_DOWN_PATH =
//        "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"

    lateinit var binder: ActivityDownloadContentsBinding
    lateinit var downloadIdList: ArrayList<Long?>

    private val downloadCompleteBr = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downloadIdList.contains(id)) {
                downloadIdList.remove(id)

                // Download complete all
                if (downloadIdList.size == 0) {
                    Toast.makeText(
                        this@DownloadContentsActivity,
                        "Download complete",
                        Toast.LENGTH_SHORT
                    ).show()
                    binder.txtDownload.text = "다운로드가 모두 완료되었습니다"
                    downloadIdList.clear()
                    startActivity(Intent(this@DownloadContentsActivity, AdMainActivity::class.java))
                    finish()
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.N)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityDownloadContentsBinding.inflate(layoutInflater)
        setContentView(binder.root)

        clearAllOldFile()

        registerReceiver(downloadCompleteBr, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // 광고 정보 리스트
        var adListJson = intent.getStringExtra("adList")
        // Todo : hjkwon temp for test
        adListJson = assets.open("testjson.txt").bufferedReader().use {
            it.readText()
        }

        Log.d("JDEBUG", "adListJson : $adListJson ")

        val adList = Gson().fromJson<List<Ad>>(adListJson, object : TypeToken<List<Ad>>() {}.type)
        if (adList.isNullOrEmpty()) {
            Utils.showSimpleAlert(this, "다운로드할 데이터가 없습니다.\n확인 후 다시 시도해주세요")
            return
        }

        AdSequenceManager.getInstance().setAdList(adList as ArrayList<Ad>)
        downloadIdList = ArrayList()
        for (ad in adList) {
            val uri1 =
                Uri.parse(ad.url1)
            val uri2 =
                Uri.parse(ad.url2)
            val uri3 =
                Uri.parse(ad.url3)

            var downloadId1: Long? = 0
            var downloadId2: Long? = 0
            var downloadId3: Long? = 0
            if (ad.url1.isNotEmpty()) {
                downloadId1 = DownloadHelper.download(this, uri1)
            }

            if (ad.url2.isNotEmpty()) {
                downloadId2 = DownloadHelper.download(this, uri2)

            }

            if (ad.url3.isNotEmpty()) {
                downloadId3 = DownloadHelper.download(this, uri3)
            }

//            val downloadId2 = DownloadHelper.download(this, uri2)
//            val downloadId3 = DownloadHelper.download(this, uri3)
            Log.d("JDEBUG", "downloadId 1 : $downloadId1")
            Log.d("JDEBUG", "downloadId 2 : $downloadId2")
            Log.d("JDEBUG", "downloadId 3 : $downloadId3")

            downloadIdList.add(downloadId1)
            downloadIdList.add(downloadId2)
            downloadIdList.add(downloadId3)
        }
        downloadIdList.removeIf { it==0L }
    }

    private fun clearAllOldFile() {
        val downloadDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
        Log.d("JDEBUG", "downloadDir path : ${downloadDir.path}")

        val fileList = downloadDir.listFiles()
        Log.d("JDEBUG", "fileList size : ${fileList.size}")
        for (file in fileList) {
            if (file.exists()) {
                val result = file.deleteRecursively()
                Log.d("JDEBUG", "del result : ${result}")
            }
        }

//        MediaScannerConnection.scanFile(
//            this,
//            downloadDir.list(),
//            null
//        ) { path, uri ->
//            Log.d("JDEBUG", "Scanned " + path + ":")
//            Log.d("JDEBUG", "-> uri=" + uri)
//
//            val fileList = downloadDir.listFiles()
//            Log.d("JDEBUG", "fileList size : ${fileList.size}")
//            for (file in fileList) {
//                if (file.exists()) {
//                    val result = file.deleteRecursively()
//                    Log.d("JDEBUG", "del result : ${result}")
//                }
//            }
//        }
    }
}