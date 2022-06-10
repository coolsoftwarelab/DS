package com.ds.soonda.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Message
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityDownloadContentsBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.manager.DownloadHelper
import com.ds.soonda.model.*
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.*
import java.io.File
import java.lang.Runnable
import java.util.*

class DownloadContentsActivity : AppCompatActivity() {

    private val UPDATE_PROGRESSBAR = 1000

    private lateinit var binder: ActivityDownloadContentsBinding
    private lateinit var downloadIdList: ArrayList<Long?>

    private var downloadTotalCount = 0
    private var handler: Handler? = null
    private val pollingTask = Runnable {
        pollingServerState()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityDownloadContentsBinding.inflate(layoutInflater)
        setContentView(binder.root)

        registerReceiver(downloadCompleteBr, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        handler = object : Handler(mainLooper) {
            override fun handleMessage(msg: Message?) {
                val progress = if (msg?.arg1 == null) 0 else msg.arg1
                binder.downloadProgress.progress = progress
            }
        }
    }

    override fun onResume() {
        super.onResume()
        pollingServerState()
    }

    private fun prepareDownload(adListJson: String) {
        //++ hjkwon temp for test
//        val adListJson = assets.open("test_json_1.txt").bufferedReader().use {
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

    private fun nextPhaseByState(adInfo: AdInfoDto?) {
        Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")
        when (adInfo?.state) {
            AD_RUNNING -> {
                stopServerPolling()
                val adJsonList = Gson().toJson(adInfo.ad)
                val adList = Gson().fromJson<List<Ad>>(adJsonList, object : TypeToken<List<Ad>>() {}.type)
                AdSequenceManager.getInstance().setAdList(adList as ArrayList<Ad>)
                startActivity(Intent(this, AdMainActivity::class.java))
                finish()
            }
            RANT_WAIT,
            AD_WAIT,
            WAIT -> {
                // do nothing
            }
            AD_WAIT_FOR_DOWNLOAD -> {
                stopServerPolling()
                clearAllOldFile()
                val adJson = Gson().toJson(adInfo.ad)
                prepareDownload(adJson)
            }
            ERROR -> {
                Utils.showSimpleAlert(this, adInfo.message)
            }
        }
    }

    private fun pollingServerState() {
        Log.d("JDEBUG", "DownloadContentsActivity pollingServerState()")

        // recursive
        handler?.postDelayed(pollingTask, App.serverPollingDelay)

        CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqServerAdInfo(App.uuid, "N")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    nextPhaseByState(adInfo)
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@DownloadContentsActivity, "Error : $errMsg")
                    stopServerPolling()
                }
            }
        }
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

                    // for get "adRunning"
                    handler?.postDelayed(pollingTask, App.serverPollingDelay)
                }
            }
        }
    }

    private fun clearAllOldFile() {
        val downloadAcroDir =
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).canonicalPath + "/acro/"

        if (!File(downloadAcroDir).exists()) {
            File(downloadAcroDir).mkdir()
        }
        val fileList = File(downloadAcroDir).listFiles()
        for (file in fileList) {
            if (file.exists()) {
                val result = file.deleteRecursively()
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