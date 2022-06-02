package com.ds.soonda.ui

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaScannerConnection
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.ds.soonda.application.App
import com.ds.soonda.databinding.ActivityIntroBinding
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.repository.ServerRepository
import com.ds.soonda.util.Utils
import com.google.gson.Gson
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.apache.commons.io.FilenameUtils

/**
 * 기기 등록 및 확인
 */
class IntroActivity : AppCompatActivity() {
    private lateinit var binder: ActivityIntroBinding

    private val REQUEST_WRITE_EXTERNAL_STORAGE = 1001

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityIntroBinding.inflate(layoutInflater)
        setContentView(binder.root)

//        Log.d("JDEBUG", "getAvailableInternalMemorySize : ${TmpGetStorageCapacity.getAvailableInternalMemorySize()}")
//        Log.d("JDEBUG", "getAvailableExternalMemorySize : ${TmpGetStorageCapacity.getAvailableExternalMemorySize()}")
//        Log.d("JDEBUG", "getTotalInternalMemorySize : ${TmpGetStorageCapacity.getTotalInternalMemorySize()}")
//        Log.d("JDEBUG", "isExternalStorageWritable : ${TmpGetStorageCapacity.isExternalStorageWritable()}")
//        Log.d("JDEBUG", "isExternalStorageReadable : ${TmpGetStorageCapacity.isExternalStorageReadable()}")
//        Log.d("JDEBUG", "queryAvailableSpace : ${TmpGetStorageCapacity.queryAvailableSpace(this@IntroActivity, Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS))}")

        // for test goto main
        // ++
//        startActivity(Intent(this@IntroActivity, AdMainActivity::class.java))
//        // uuid for reqAdData
//        var uuid = App.SharedPrefHelper.getString("uuid", "")
//        if (uuid.isNullOrEmpty()) {
//            uuid = Utils.getUUID(this)
//            App.SharedPrefHelper.edit().putString("uuid", uuid).apply()
//        }
//        App.uuid = uuid
//        if (true) return
        // --


        checkPermission()
    }

    private fun checkPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            // 유저가 이전에 권한을 거부한 경우, 접근권한 안내메시지 출력
            val isPermissionRejeted =
                App.SharedPrefHelper.getBoolean("is_storage_permission_rejected", false)
            Log.d("JDEBUG", "isPermissionRejeted : $isPermissionRejeted")
            if (isPermissionRejeted) {
                Toast.makeText(
                    this@IntroActivity,
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
            // Permission has already been granted
            init()
        }
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
                    App.SharedPrefHelper.edit().putBoolean("is_storage_permission_rejected", true)
                        .apply()
                    Toast.makeText(
                        this@IntroActivity,
                        "앱 사용을 위해 저장소 권한을 허용해주세요",
                        Toast.LENGTH_LONG
                    ).show()
                    finish()
                }
                return
            }
        }
    }

    private fun init() {
        // uuid for reqAdData
        var uuid = App.SharedPrefHelper.getString("uuid", "")
        if (uuid.isNullOrEmpty()) {
            uuid = Utils.getUUID(this)
            App.SharedPrefHelper.edit().putString("uuid", uuid).apply()
        }
        App.uuid = uuid

        // 앱 구동 후 현재 서버 state 상태 확인.
        reqAdData(uuid, "N")
    }


    private fun reqAdData(uuid: String, reqRentalNumber: String) {
        val job = CoroutineScope(Dispatchers.IO).launch {
            val service = ServerRepository.getServerInterface()
            val response = service.reqAdData(uuid, reqRentalNumber)

            withContext(Dispatchers.Main) {
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
                            Log.d(
                                "JDEBUG",
                                "adJson :  ${adJson}"
                            )
                            intent.putExtra("adList", adJson)
                            startActivity(intent)
                            finish()

                        }
                    }

                    // Todo : state 이 정상이라면 광고 이미지 및 영상 다운로드?

                } else {
                    val errMsg = response.message()
                    Utils.showSimpleAlert(this@IntroActivity, "Error : $errMsg")
                }
            }
        }
    }
}