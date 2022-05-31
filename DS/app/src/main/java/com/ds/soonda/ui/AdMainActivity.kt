package com.ds.soonda.ui

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.R
import com.ds.soonda.databinding.ActivityAdMainBinding
import com.ds.soonda.ui.fragment.TemplateFirstFragment
import com.ds.soonda.ui.fragment.TemplateSecondFragment

class AdMainActivity : AppCompatActivity() {
    private lateinit var binder: ActivityAdMainBinding
    private val DOWNLOAD_DIR_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        //Fragment setting
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        val t1Fragment = TemplateFirstFragment()
        fragmentTransaction.add(R.id.fragment_container_view, t1Fragment).commit()

        Handler().postDelayed({
//            val t2Fragment = TemplateSecondFragment(
//                "$DOWNLOAD_DIR_PATH/Download-1.mp4",
//                "$DOWNLOAD_DIR_PATH/Download-2.mp4"
//            )
            val t2Fragment = TemplateSecondFragment(
                "$DOWNLOAD_DIR_PATH/iv1.png",
                "$DOWNLOAD_DIR_PATH/Download-2.mp4"
            )

            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_view, t2Fragment).commit()
        }, 3000)


        // 서버정보에따라 template에 동적으로 VideoView, ImageView 생성
//        val videoView = VideoView(this)
//        val videoView2 = VideoView(this)
//        videoView.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//        )
//        videoView2.layoutParams = LinearLayout.LayoutParams(
//            LinearLayout.LayoutParams.MATCH_PARENT,
//            LinearLayout.LayoutParams.MATCH_PARENT
//        )
//        val path1: String = "$DOWNLOAD_DIR_PATH/Download-1.mp4"
//        val path2: String = "$DOWNLOAD_DIR_PATH/Download-2.mp4"
//        videoView.setVideoPath(path1)
//        videoView2.setVideoPath(path2)
////        videoView.setVideoURI(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
////        videoView2.setVideoURI(Uri.parse("https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"))
//        binder.llContainer.addView(videoView)
//        binder.llContainer.addView(videoView2)
//        videoView.start()
//        videoView2.start()

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
//                    Log.d("JDEBUG", "body : $body")
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