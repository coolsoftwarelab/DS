package com.ds.soonda.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityIntroBinding
import com.ds.soonda.model.*
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class IntroActivity : AppCompatActivity() {

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1001
    private val ONE_GIGABYTE = 1

    private lateinit var binder: ActivityIntroBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binder.root)

        // 여유 저장공간 확인
        val availableGb = Utils.getAvailableInternalMemorySize()
        if (availableGb < ONE_GIGABYTE) {  // under 1GB
            AlertDialog.Builder(this)
                .setMessage("저장공간이 부족합니다\n관리자에게 문의 바랍니다")
                .setPositiveButton(
                    "대여 취소"
                ) { _, _ -> finish() }
                .setCancelable(false)
                .create().show()
            return
        }

        // 저장권한 확인
        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 유저가 이전에 권한을 거부한 경우, 접근권한 안내메시지 출력
            val isPermissionRejected =
                App.SharedPrefHelper.getBoolean("is_storage_permission_rejected", false)
            if (isPermissionRejected) {
                Toast.makeText(
                    this,
                    "앱 사용을 위해 저장소 권한을 허용해주세요",
                    Toast.LENGTH_LONG
                ).show()
            }
            // Permission is not granted. No explanation needed, we can request the permission.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
                REQUEST_WRITE_EXTERNAL_STORAGE
            )
        } else {
            // 권한획득 완료. 초기화
            init()
        }
    }

    private fun init() {
        var uuid = App.SharedPrefHelper.getString("uuid", "")
        if (uuid.isNullOrEmpty()) {
            uuid = Utils.getUUID(this)
            App.SharedPrefHelper.edit().putString("uuid", uuid).apply()
        }
        App.uuid = uuid
        Log.d("JDEBUG", "uuid : $uuid")

        // 앱 구동 후 현재 기기에 대한 서버 state 확인.
        reqServerAdInfo(uuid, "N")
    }

    private fun reqServerAdInfo(uuid: String, reqRentalNumber: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()

            kotlin.runCatching {
                service.reqServerAdInfo(uuid, reqRentalNumber)
            }.onSuccess { response ->
                withContext(Dispatchers.Main) {
                    if (response.isSuccessful) {
                        val adInfo: AdInfoDto? = response.body()
                        nextPhaseByState(adInfo)
                    } else {
                        val errMsg = response.message()
                        Utils.showSimpleAlert(this@IntroActivity, "Error : $errMsg")
                    }
                }
            }.onFailure {
                withContext(Dispatchers.Main) {
                    Utils.showSimpleAlert(this@IntroActivity, it.toString())
                }
            }
        }
    }

    private fun nextPhaseByState(adInfo: AdInfoDto?) {
        var intent: Intent? = null

        when (adInfo?.state) {
            RANT_WAIT, WAIT -> {
                /**
                 * 대기상태(인증번호 미발급).
                 * 처음 reqAdData() 하면 서버에서 자동으로 기기등록 수행하고 "rantWait" 상태를 리턴해준다
                 * 인증번호 발급 요청 하고 성공하면 서버에서 "wait" 상태가되고 인증번호가 리턴됨
                 */
                App.serverPollingDelay = (adInfo.bgTimer * 1000).toLong() // millis to second
                intent = Intent(this, DeviceAuthActivity::class.java)
            }
            AD_WAIT_FOR_DOWNLOAD, AD_RUNNING, AD_WAIT -> {
                /**
                 * AD_WAIT. 광고 송출 대기. 기기등록되고 인증번호까지 인증된 상태. 컨텐츠 다운로드 필요
                 * AD_RUNNING. 광고 송출중. 광고중 앱 에러 후 앱 재시작 시 이 상태가 될 수 있다. 다시 컨텐츠 다운로드부터 검사
                 */
                intent = Intent(this, DownloadContentsActivity::class.java)
            }
            ERROR -> {
                Utils.showSimpleAlert(this, adInfo.message)
                return
            }
        }
        startActivity(intent)
        finish()
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            REQUEST_WRITE_EXTERNAL_STORAGE -> {
                // If request is cancelled, the result arrays are empty.
                if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                    init()
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    App.SharedPrefHelper.edit()
                        .putBoolean("is_storage_permission_rejected", true)
                        .apply()
                    Toast.makeText(
                        this,
                        "앱 사용을 위해 저장소 권한을 허용해주세요",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }
}