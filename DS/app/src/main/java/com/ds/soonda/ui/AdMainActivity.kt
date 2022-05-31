package com.ds.soonda.ui

import android.content.Intent
import android.os.*
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
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
import org.apache.commons.io.FilenameUtils;


class AdMainActivity : AppCompatActivity() {

    //    private val DOWNLOAD_PATH =
//        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path
    private val adManager = AdSequenceManager.getInstance()

    private lateinit var binder: ActivityAdMainBinding
    private lateinit var adList: ArrayList<Ad>

    private var playAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        adList = adManager.getAdList()
        prepareAd()

        // todo 폴링 중 무슨 상태가되면 worker End 인지?
        Handler(Looper.getMainLooper()).postDelayed({
            pollingServerState()
        }, 10 * 1000)
    }

    private fun prepareAd() {
        if (playAdIndex > adList.size - 1) {
            // 모든 광고 순회 후 처음부 다시 광고 시작
            playAdIndex = 0
        }

        val selectedAd = adList[playAdIndex]
        playAd(selectedAd)

        // ad time 시간 이후에 다음 ad로 전환
        Handler(Looper.getMainLooper()).postDelayed({
            prepareAd()
        }, selectedAd.time * 1000L)
    }

    private fun playAd(ad: Ad) {
        val filePath1 = FilenameUtils.getName(ad.url1)
        val filePath2 = FilenameUtils.getName(ad.url2)
        val filePath3 = FilenameUtils.getName(ad.url3)
        Log.d("JDEBUG", "filePath1 : $filePath1")

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
        val fragmentTransaction = supportFragmentManager.beginTransaction()
        fragmentTransaction.replace(R.id.fragment_container_view, fragment).commit()
        playAdIndex++
    }

    // polling for end of Worker job
    private fun pollingServerState() {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "N")

            // 폴링 중 어느 시점이든 종료 점 필요
            if (response.isSuccessful) {
                val adInfo: AdInfoDto? = response.body()
                Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")

                // todo needed withContext(Dispatcher.Main)??
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