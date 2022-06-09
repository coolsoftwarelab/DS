package com.ds.soonda.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ds.soonda.application.App
import com.ds.soonda.model.*
import com.ds.soonda.databinding.ActivityRentalAuthenticationBinding
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

/**
 * 인증번호 출력
 */
class DeviceAuthActivity : AppCompatActivity() {

    lateinit var binder: ActivityRentalAuthenticationBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityRentalAuthenticationBinding.inflate(layoutInflater)
        setContentView(binder.root)

        reqRentNumber()
        pollingServerState()
    }

    private fun reqRentNumber() {
        CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqServerAdInfo(App.uuid, "Y")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    nextPhaseByState(adInfo)
                    binder.txtAuthNum.text = adInfo?.rentNumber
                }
            }
        }
    }

    private fun nextPhaseByState(adInfo: AdInfoDto?) {
        Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")

        when (adInfo?.state) {
            RANT_WAIT, WAIT -> {
                // 인증번호로 기기등록 될때까지 일정 시간마다 폴링
                if (App.activityState == App.ActivityState.FOREGROUND) {
                    Handler(mainLooper).postDelayed({
                        pollingServerState()
                    }, App.serverPollingDelay)
                }
            }
            AD_WAIT_FOR_DOWNLOAD, AD_RUNNING -> {
                // 광고 송출용 컨텐츠 다운로드화면으로 이동
                val intent =
                    Intent(
                        this,
                        DownloadContentsActivity::class.java
                    )
                val adJson = Gson().toJson(adInfo.ad)
                intent.putExtra("adList", adJson)
                startActivity(intent)
                finish()
            }
            AD_WAIT -> {
                // 광고 송출 대기
            }
            ERROR -> {
                Utils.showSimpleAlert(this, adInfo.message)
                return
            }
        }
    }

    private fun pollingServerState() {
        Log.d("JDEBUG", "DeviceAuthActivity pollingServerState()")
        CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqServerAdInfo(App.uuid, "N")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    nextPhaseByState(adInfo)
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@DeviceAuthActivity, "Error : $errMsg")
                }
            }
        }
    }
}