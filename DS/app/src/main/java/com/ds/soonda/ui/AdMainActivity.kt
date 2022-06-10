package com.ds.soonda.ui

import android.content.Intent
import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import android.view.Window
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ds.soonda.R
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityAdMainBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.model.*
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.ui.fragment.*
import com.ds.soonda.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils


class AdMainActivity : AppCompatActivity() {
    private val DOWNLOAD_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path + "/acro/"

    private val adManager = AdSequenceManager.getInstance()

    private lateinit var handler: Handler
    private lateinit var binder: ActivityAdMainBinding
    private lateinit var adList: ArrayList<Ad>

    private val pollingTask = Runnable {
        pollingServerState()
    }
    private var playAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        requestWindowFeature(Window.FEATURE_NO_TITLE);
        window.setFlags(
            WindowManager.LayoutParams.FLAG_FULLSCREEN,
            WindowManager.LayoutParams.FLAG_FULLSCREEN
        );
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        adList = adManager.getAdList()
        handler = Handler(mainLooper)
        prepareAd()
    }

    override fun onResume() {
        super.onResume()
        pollingServerState()
    }

    override fun onPause() {
        stopServerPolling()
        super.onPause()
    }

    private fun prepareAd() {
        // 모든 광고리스트 순회했다면 처음부터 다시 광고 시작
        if (playAdIndex > adList.size - 1) {
            playAdIndex = 0
        }

        val selectedAd = adList[playAdIndex]
        playAd(selectedAd)

        // ad time 시간 이후에 다음 ad로 전환
        if (App.activityState == App.ActivityState.FOREGROUND) {
            handler.postDelayed({
                prepareAd()
            }, selectedAd.time * 1000L)
        }
    }

    private fun playAd(ad: Ad) {
        val filePath1 = DOWNLOAD_PATH + FilenameUtils.getName(ad.url1)
        val filePath2 = DOWNLOAD_PATH + FilenameUtils.getName(ad.url2)
        val filePath3 = DOWNLOAD_PATH + FilenameUtils.getName(ad.url3)

        Log.d("JDEBUG", "playAd filePath1 : $filePath1")
        Log.d("JDEBUG", "playAd filePath2 : $filePath2")
        Log.d("JDEBUG", "playAd filePath3 : $filePath3")

        // select Template 1~5
        val fragment: Fragment = when (ad.template) {
            1 -> TemplateFirstFragment(filePath1)
            2 -> TemplateSecondFragment(filePath1, filePath2)
            3 -> TemplateThirdFragment(filePath1, filePath2, filePath3)
            4 -> TemplateFourthFragment(filePath1, filePath2)
            5 -> TemplateFifthFragment(filePath1, filePath2, filePath3)
            else -> Fragment()  // empty
        }

        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction
                .setCustomAnimations(R.anim.slide_in_right, R.anim.slide_out_left)
                .replace(R.id.fragment_container_view, fragment)
                .commit()
            playAdIndex++
        } catch (e: IllegalStateException) {
            // When fragment destroyed
            e.printStackTrace()
            Log.d("JDEBUG", "playAd IllegalStateException")
            finish()
        }
    }

    // polling for end of Worker job
    private fun pollingServerState() {
        Log.d("JDEBUG", "AdMainAcitivty pollingServerState()")

        CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqServerAdInfo(App.uuid, "N")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "polling adInfo?.state : ${adInfo?.state}")
                    when (adInfo?.state) {
                        RANT_WAIT -> {
                            // 기기 반납되면 rantWait 상태가 됨
                            val intent =
                                Intent(this@AdMainActivity, DeviceAuthActivity::class.java)
                            startActivity(intent)
                            finish()
                        }
                        WAIT, AD_WAIT -> {}
                        AD_RUNNING -> {
                            // 광고 송출중. 인증번호로 서버에 렌트기기등록 성공하면 adWait 상태
                            if (App.activityState == App.ActivityState.FOREGROUND) {
                                handler.postDelayed(pollingTask, App.serverPollingDelay)
                            }
                        }
                        ERROR -> Utils.showSimpleAlert(this, adInfo.message)
                    }
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@AdMainActivity, "Error : $errMsg")
                }
            }
        }
    }

    private fun stopServerPolling() {
        handler.removeCallbacks(pollingTask)
    }

}