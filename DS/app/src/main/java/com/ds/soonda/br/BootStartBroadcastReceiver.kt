package com.ds.soonda.br

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.os.SystemClock
import android.util.Log
import com.ds.soonda.application.App
import com.ds.soonda.manager.NetworkStateManager
import com.ds.soonda.ui.IntroActivity

class BootStartBroadcastReceiver : BroadcastReceiver() {
    private val DELAY_TIME_SEC = 10 * 1000

    override fun onReceive(context: Context?, intent: Intent?) {
        Log.d("JDEBUG", "onReceive context : $context")
        if (context == null) {
            return
        }

        /**
         * 네트워크 연결상태이고 앱이 실행되지 않은 상태라면 앱 실행
         * 네트워트가 연결되어있지 않았다면 N 시간 대기 후 현재 BR 재귀호출
         */
        if (NetworkStateManager.checkNetworkState(context) &&
            App.activityState == App.ActivityState.NONE
        ) {
            context.startActivity(Intent(context, IntroActivity::class.java))
        } else {
            if (App.activityState != App.ActivityState.NONE) {
                return
            }

            val alarmMgr = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
            val alarmIntent =
                Intent(context, BootStartBroadcastReceiver::class.java).let { intent ->
                    PendingIntent.getBroadcast(context, 0, intent, 0)
                }

            alarmMgr.set(
                AlarmManager.ELAPSED_REALTIME_WAKEUP,
                SystemClock.elapsedRealtime() + DELAY_TIME_SEC,
                alarmIntent
            )
        }
    }
}