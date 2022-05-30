package com.ds.soonda.ui

import android.app.DownloadManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.databinding.ActivityDownloadContentsBinding
import com.ds.soonda.manager.DownloadHelper
import com.ds.soonda.model.Ad
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class DownloadContentsActivity : AppCompatActivity() {
    private val TMP_FILE_DOWN_PATH =
        "https://www.sample-videos.com/video123/mp4/720/big_buck_bunny_720p_1mb.mp4"

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
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityDownloadContentsBinding.inflate(layoutInflater)
        setContentView(binder.root)

        clearAllOldFile()

        registerReceiver(downloadCompleteBr, IntentFilter(DownloadManager.ACTION_DOWNLOAD_COMPLETE))

        // 광고 정보 리스트
        val adListJson = intent.getStringExtra("adList")
        val adList = Gson().fromJson<List<Ad>>(adListJson, object : TypeToken<List<Ad>>() {}.type)
        if (adList.isNullOrEmpty()) {
            Utils.showSimpleAlert(this, "다운로드할 데이터가 없습니다.\n확인 후 다시 시도해주세요")
            return
        }

        downloadIdList = ArrayList()
        for (ad in adList) {
            Log.d("JDEBUG", "ad index : ${ad.index}")
            val uri =
                Uri.parse(TMP_FILE_DOWN_PATH)
            val downloadId = DownloadHelper.download(this, uri)
            Log.d("JDEBUG", "downloadId : ${downloadId}")
            downloadIdList.add(downloadId)
        }
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
    }
}