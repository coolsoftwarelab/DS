package com.ds.soonda.ui

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityIntroBinding
import com.ds.soonda.model.Ad
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * 기기 등록 및 확인
 */
class IntroActivity : AppCompatActivity() {
    private lateinit var binder: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binder.root)

        // uuid for reqAdData
        var uuid = App.SharedPrefHelper.getString("uuid", "")
        if (uuid.isNullOrEmpty()) {
            uuid = Utils.getUUID(this)
            App.SharedPrefHelper.edit().putString("uuid", uuid).apply()
        }
        App.uuid = uuid

        // 앱 구동 후 현재 서버 state 상태 확인
        reqAdData(uuid, "N")
    }

    private fun reqAdData(uuid: String, reqRentalNumber: String) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(uuid, reqRentalNumber)

            if (response.isSuccessful) {
                val adInfo: AdInfoDto? = response.body()

                when (adInfo?.state) {
                    "error" -> {
                        Utils.showSimpleAlert(this@IntroActivity, adInfo.message)
                    }
                    "wait" -> {
                        // 대기상태(인증번호 발급), isRentalCertifyNumber=Y 로 전송시 인증번호를 신규로 발급한다.
                        val intent =
                            Intent(this@IntroActivity, DeviceAuthActivity::class.java)
                        intent.putExtra("rentNumber", adInfo.rentNumber)
                        startActivity(intent)
                        finish()
                    }
                    "rantWait" -> {
                        // 대기상태(인증번호 미발급), isRentalCertifyNumber=N로 전송시 인증번호는 발급하지 않는다.
                        // 처음 reqAdData() 하면 서버에서 자동으로 기기등록 수행하고 rantWait 상태를 리턴해준다
                        // 인증번호 발급 요청 하고 성공하면 서버에서 "wait" 상태가되고 인증번호가 리턴됨
                        reqAdData(uuid, "Y")
                    }
                    "adWait" -> {
                        // 광고 송출 대기
                    }
                    "adRunning" -> {
                        // 광고 송출중. 광고중 앱 에러 후 재진입 시 이 상태가 될 수 있다. 다시 컨텐츠 다운로드부터 검사
                        val intent =
                            Intent(this@IntroActivity, DownloadContentsActivity::class.java)
                        val adJson = Gson().toJson(adInfo.ad)
                        intent.putExtra("adList", adJson)
                        startActivity(intent)
                        finish()

                    }
                }

//                val adList = adInfo?.ad
//                App.alreadyReceivedRentNum = (adInfo?.rentNumber?.isNotEmpty() == true)
//
//                withContext(Dispatchers.Main) {
//                    binder.txtAuthNum.text = adInfo?.state + "\n" + adInfo?.rentNumber
//                    // Todo : state 이 정상이라면 광고 이미지 및 영상 다운로드
//
//                }
            } else {
                val errMsg = response.message()
                Utils.showSimpleAlert(this@IntroActivity, "Error : $errMsg")
            }
        }
    }
}