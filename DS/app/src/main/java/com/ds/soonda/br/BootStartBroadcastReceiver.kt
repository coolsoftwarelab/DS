package com.ds.soonda.br

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.ds.soonda.ui.IntroActivity

class BootStartBroadcastReceiver: BroadcastReceiver() {
    override fun onReceive(context: Context?, intent: Intent?) {
        context?.startActivity(Intent(context, IntroActivity::class.java))
    }
}