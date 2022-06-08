package com.ds.soonda.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.*
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityDownloadContentsBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.manager.DownloadHelper
import com.ds.soonda.model.Ad
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.util.*
import kotlin.collections.ArrayList

class DownloadContentsActivity : AppCompatActivity() {

    private val UPDATE_PROGRESSBAR = 1000

    private lateinit var binder: ActivityDownloadContentsBinding
    private lateinit var downloadIdList: ArrayList<Long?>
    private var downloadTotalCount = 0
    private var handler: Handler? = null
    private val pollingTask = Runnable {
        pollingServerState()
    }

    private val downloadCompleteBr = object : BroadcastReceiver() {
        override fun onReceive(context: Context?, intent: Intent?) {
            val id = intent?.getLongExtra(DownloadManager.EXTRA_DOWNLOAD_ID, -1)

            if (downloadIdList.contains(id)) {
                downloadIdList.remove(id)

                val progress: Int =
                    ((downloadTotalCount - downloadIdList.size).toDouble() / downloadTotalCount * 100).toInt()
                Log.d("JDEBUG", "progress : $progress")
                val message = Message.obtain()
                message.what = UPDATE_PROGRESSBAR
                message.arg1 = progress
                handler?.sendMessage(message)

                // Download complete all
                if (downloadIdList.size == 0) {
                    Toast.makeText(
                        this@DownloadContentsActivity,
                        "Download complete",
                        Toast.LENGTH_SHORT
                    ).show()
                    binder.txtDownload.text = "다운로드 완료!"
                    binder.txtNextStep.visibility = View.VISIBLE
                    binder.downloadProgress.visibility = View.GONE
                    downloadIdList.clear()
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityDownloadContentsBinding.inflate(layoutInflater)
        setContentView(binder.root)

        handler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message?) {
                val progress = if (msg?.arg1 == null) 0 else msg.arg1
                binder.downloadProgress.progress = progress
            }
        }

        registerReceiver(downloadCompleteBr, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        clearAllOldFile()

        pollingServerState()
    }

    override fun onResume() {
        super.onResume()
        pollingServerState()
    }

    private fun prepareDownload(adListJson: String) {
        // 광고 정보 리스트
        var adListJson = intent.getStringExtra("adList")

        //++ Todo : hjkwon temp for test
//        adListJson = assets.open("test_json_2.txt").bufferedReader().use {
//            it.readText()
//        }
        //--

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

            Log.d("JDEBUG", "downloadId 1 : $downloadId1")
            Log.d("JDEBUG", "downloadId 2 : $downloadId2")
            Log.d("JDEBUG", "downloadId 3 : $downloadId3")

            downloadIdList.add(downloadId1)
            downloadIdList.add(downloadId2)
            downloadIdList.add(downloadId3)
        }

        downloadIdList.removeAll(Collections.singleton(0L))
        downloadTotalCount = downloadIdList.size
    }

    private fun clearAllOldFile() {
        Log.d("JDEBUG", "clearAllOldFile() ")
        val downloadAcroDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canonicalPath + "/acro/"

        if (!File(downloadAcroDir).exists()) {
            File(downloadAcroDir).mkdir()
        }
        val fileList = File(downloadAcroDir).listFiles()
        Log.d("JDEBUG", "downloadDir fileList size : ${fileList.size}")
        for (file in fileList) {
            if (file.exists()) {
                val result = file.deleteRecursively()
                Log.d("JDEBUG", "del result : $result")
            }
        }
    }

    private fun pollingServerState() {
        Log.d("JDEBUG", "DownloadContentsActivity pollingServerState()")

        // recursive
        handler?.postDelayed(pollingTask, 5000)

        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "N")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")
                    when (adInfo?.state) {
                        "adRunning" -> {
                            startActivity(
                                Intent(
                                    this@DownloadContentsActivity,
                                    AdMainActivity::class.java
                                )
                            )
                            finish()
                        }
                        "RANT_WAIT",
                        "adWait",
                        "wait" -> {
                            // do nothing
                        }
                        "adWaitForDownload" -> {
                            stopServerPolling()
                            val adJson = Gson().toJson(adInfo.ad)
                            prepareDownload(adJson)
                        }
                        "error" -> {
                            Utils.showSimpleAlert(this@DownloadContentsActivity, adInfo.message)
                        }
                    }
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@DownloadContentsActivity, "Error : $errMsg")
                    stopServerPolling()
                }
            }
        }
    }

    private fun stopServerPolling() {
        handler?.removeCallbacks(pollingTask)
    }

    override fun onPause() {
        stopServerPolling()
        super.onPause()
    }
}