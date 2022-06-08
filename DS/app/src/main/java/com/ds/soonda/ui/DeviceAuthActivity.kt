package com.ds.soonda.ui

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import com.ds.soonda.application.App
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
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "Y")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")
                    binder.txtAuthNum.text = adInfo?.rentNumber
                }
            }
        }
    }

    private fun pollingServerState() {
        Log.d("JDEBUG", "DeviceAuthActivity pollingServerState()")
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "N")

            withContext(Dispatchers.Main) {
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "adInfo?.state : ${adInfo?.state}")
                    when (adInfo?.state) {
                        "error" -> {
                            Utils.showSimpleAlert(this@DeviceAuthActivity, adInfo.message)
                        }
                        "rantWait",
                        "wait" -> {
                            // 인증번호로 기기등록 될때까지 일정 시간마다 폴링
                            if (App.getActivityState() == App.ActivityState.FOREGROUND) {
                                Handler(Looper.getMainLooper()).postDelayed({
                                    pollingServerState()
                                }, 5_000)
                            }
                        }
                        "adWait" -> {
                            // 광고 송출 대기

                        }
                        "adRunning" -> {
                            // 광고 송출중. 인증번호로 서버에 렌트기기등록 성공하면 adWait 상태
                            val intent =
                                Intent(
                                    this@DeviceAuthActivity,
                                    DownloadContentsActivity::class.java
                                )
                            val adJson = Gson().toJson(adInfo.ad)
                            intent.putExtra("adList", adJson)
                            startActivity(intent)
                            finish()
                        }
                    }
                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@DeviceAuthActivity, "Error : $errMsg")
                }
            }
        }
    }

}