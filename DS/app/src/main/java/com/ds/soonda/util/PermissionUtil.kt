package com.ds.soonda.util

import android.Manifest
import com.ds.soonda.util.PermissionUtil
import android.content.pm.PackageManager
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

object PermissionUtil {
    const val REQ_PERMISSION = 101
    const val REQ_PERMISSION_TO_SETTING = 102

    val REQUIRE_ESSENTIAL_PERMISSION_ARR = arrayOf(
        Manifest.permission.READ_EXTERNAL_STORAGE,
        Manifest.permission.WRITE_EXTERNAL_STORAGE
    )
    val REQUIRE_OPTIONAL_PERMISSION_ARR = arrayOf<String>()

    fun checkPermissions(ctx: Context?, permissionArr: Array<String>): Boolean {
        for (permission in permissionArr) {
            val permissionCheck = checkSelfPermission(ctx, permission)
            if (permissionCheck != PackageManager.PERMISSION_GRANTED && ctx is Activity) {
                ActivityCompat.requestPermissions(
                    (ctx as Activity?)!!,
                    permissionArr,
                    REQ_PERMISSION
                )
                return false
            }
        }
        return true
    }

    private fun checkSelfPermission(ctx: Context?, permission: String?): Int {
        return ContextCompat.checkSelfPermission(ctx!!, permission!!)
    }
}