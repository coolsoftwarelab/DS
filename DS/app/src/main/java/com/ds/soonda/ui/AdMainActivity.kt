package com.ds.soonda.ui

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.os.Looper
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils


class AdMainActivity : AppCompatActivity() {
    private val DOWNLOAD_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path +"/acro/"

    private val adManager = AdSequenceManager.getInstance()

    private lateinit var handler: Handler
    private lateinit var binder: ActivityAdMainBinding
    private lateinit var adList: ArrayList<Ad>

    private var playAdIndex = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        adList = adManager.getAdList()
        handler = Handler(Looper.getMainLooper())


        prepareAd()

        pollingServerState()
    }

    private fun prepareAd() {
        if (playAdIndex > adList.size - 1) {
            // 모든 광고리스트 순회했다면 처음부터 다시 광고 시작
            playAdIndex = 0
        }

        val selectedAd = adList[playAdIndex]
        playAd(selectedAd)

        // ad time 시간 이후에 다음 ad로 전환
        if (App.getActivityState() == App.ActivityState.FOREGROUND) {
            handler.postDelayed({
                Log.d("JDEBUG", "postDelayed time ${selectedAd.time}")
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

        try {
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction
                .setCustomAnimations(
                    androidx.appcompat.R.anim.abc_fade_in,
                    androidx.appcompat.R.anim.abc_fade_out
                )
                .replace(R.id.fragment_container_view, fragment).commit()
            playAdIndex++
        } catch (e: IllegalStateException) {
            // When fragment destroyed
            Log.d("JDEBUG", "playAd IllegalStateException")
            e.printStackTrace()
            finish()
        }
    }

    // polling for end of Worker job
    private fun pollingServerState() {
        Log.d("JDEBUG", "pollingServerState()")

        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(App.uuid, "N")

            withContext(Dispatchers.Main) {
                /**
                 * todo (server bug : 기기반납해도 state가 adRunning 인 문제. 상태가 wait? 등으로 바뀌고 광고종료->인증화면 창으로 가야함.)
                 */
                if (response.isSuccessful) {
                    val adInfo: AdInfoDto? = response.body()
                    Log.d("JDEBUG", "polling adInfo?.state : ${adInfo?.state}")
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
                            if (App.getActivityState() == App.ActivityState.FOREGROUND) {
                                handler.postDelayed({
                                    pollingServerState()
                                }, 10 * 1000)
                            }
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