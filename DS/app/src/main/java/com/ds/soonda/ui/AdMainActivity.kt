package com.ds.soonda.ui

import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import com.ds.soonda.R
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityAdMainBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.model.Ad
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.ui.fragment.*
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils;


class AdMainActivity : AppCompatActivity() {
    private val DOWNLOAD_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

    private val adManager = AdSequenceManager.getInstance()

    private lateinit var binder: ActivityAdMainBinding
    private lateinit var adList: ArrayList<Ad>

    private var playAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        adList = adManager.getAdList()

        //++ for test
//        val downloadDir =
//            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
//        Log.d("JDEBUG", "downloadDir : $downloadDir ")
//        adList.clear()
//        adList.add(
//            Ad(
//                1,
//                "${downloadDir}/big_buck_bunny_720p_1mb-1.mp4",
//                "",
//                "",
//                1,
//                3,
//                1
//            )
//        )
//
//        adList.add(
//            Ad(
//                3,
//                "${downloadDir}/big_buck_bunny_720p_1mb-1.mp4",
//                "${downloadDir}/big_buck_bunny_720p_1mb-1.mp4",
//                "${downloadDir}/robot.png",
//                2,
//                5,
//                1
//            )
//        )
        // --
        prepareAd()

        // todo 폴링 중 무슨 상태가되면 worker End 인지 알아야함.
        Handler(Looper.getMainLooper()).postDelayed({
            pollingServerState()
        }, 10 * 1000)
    }

    private fun prepareAd() {
        if (playAdIndex > adList.size - 1) {
            // 모든 광고리스트 순회했다면 처음부터 다시 광고 시작
            playAdIndex = 0
        }

        val selectedAd = adList[playAdIndex]
        playAd(selectedAd)

        // ad time 시간 이후에 다음 ad로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            Log.d("JDEBUG", "postDelayed time ${selectedAd.time}")
            prepareAd()
        }, selectedAd.time * 1000L)
    }

    private fun playAd(ad: Ad) {
        val filePath1 = DOWNLOAD_PATH + "/" + FilenameUtils.getName(ad.url1)
        val filePath2 = DOWNLOAD_PATH + "/" + FilenameUtils.getName(ad.url2)
        val filePath3 = DOWNLOAD_PATH + "/" + FilenameUtils.getName(ad.url3)

        Log.d("JDEBUG", "playAd filePath1 : $filePath1")

        // select Template 1~5
        val fragment: Fragment = when (ad.template) {
            1 -> {
                TemplateFirstFragment(filePath1)
            }
            2 -> {
                TemplateSecondFragment(
                    filePath1, filePath2
                )
            }
            3 -> {
                TemplateThirdFragment(
                    filePath1, filePath2, filePath3
                )
            }
            4 -> {
                TemplateFourthFragment(
                    filePath1, filePath2
                )
            }
            5 -> {
                TemplateFifthFragment(
                    filePath1, filePath2, filePath3
                )
            }
            else -> {
                Fragment()  // empty
            }
        }

        // todo : activity foreground/background checking and exception handling before fragment transaction

        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction
            .setCustomAnimations(
                androidx.appcompat.R.anim.abc_fade_in,
                androidx.appcompat.R.anim.abc_fade_out
            )
            .replace(R.id.fragment_container_view, fragment).commit()
        playAdIndex++
    }

    // polling for end of Worker job
    private fun pollingServerState() {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "N")

            withContext(Dispatchers.Main) {
                // 폴링 중 어느 시점이든 종료 점 필요
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")
                    when (adInfo?.state) {
                        "error" -> {
                            Utils.showSimpleAlert(this@AdMainActivity, adInfo.message)
                        }
                        "rantWait" -> {

                        }
                        "wait" -> {
                        }
                        "adWait" -> {
                            // 광고 송출 대기
                        }
                        "adRunning" -> {
                            // 광고 송출중. 인증번호로 서버에 렌트기기등록 성공하면 adWait 상태
                        }
                    }
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@AdMainActivity, "Error : $errMsg")
                }
            }
        }
    }
}