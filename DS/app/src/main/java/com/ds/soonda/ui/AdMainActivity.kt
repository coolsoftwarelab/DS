package com.ds.soonda.ui

import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.widget.LinearLayout
import android.widget.VideoView
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.databinding.ActivityAdMainBinding
import kotlinx.coroutines.*

class AdMainActivity : AppCompatActivity() {
    private lateinit var binder: ActivityAdMainBinding
    val DOWNLOAD_DIR_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        // 서버정보에따라 template에 동적으로 VideoView, ImageView 생성
        val videoView = VideoView(this)
        val videoView2 = VideoView(this)
        videoView.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        videoView2.layoutParams = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.MATCH_PARENT
        )
        val path1: String = DOWNLOAD_DIR_PATH + "/Download-1.mp4"
        val path2: String = DOWNLOAD_DIR_PATH + "/Download-2.mp4"
        videoView.setVideoPath(path1)
        videoView2.setVideoPath(path2)
//        videoView.setVideoURI(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
//        videoView2.setVideoURI(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
        binder.llContainer.addView(videoView)
        binder.llContainer.addView(videoView2)
        videoView.start()
        videoView2.start()

//        getServerData()
    }

//    private fun getServerData() {
//        val uuid = Utils.getUUID(this)
//        job = CoroutineScope(Dispatchers.IO).launch {
//            val apiService = ServerRepository.getServerInterface()
//            val response = apiService.getRentalDevice("1234")
//            withContext(Dispatchers.Main) {
//                // ui handling
//                if (response.isSuccessful) {
//                    val body = response.body()
//                    Log.d("JDUEBG", "body : $body")
//                } else {
//                    // error
//                }
//
//            }
//        }
//
//
//    }
}