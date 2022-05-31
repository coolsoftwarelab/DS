package com.ds.soonda.ui

import android.os.Bundle
import android.os.Environment
import android.os.Handler
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.ds.soonda.R
import com.ds.soonda.databinding.ActivityAdMainBinding
import com.ds.soonda.manager.AdSequenceManager
import com.ds.soonda.ui.fragment.*
import org.apache.commons.io.FilenameUtils;


class AdMainActivity : AppCompatActivity() {

    private val DOWNLOAD_PATH =
        Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS).path

    private lateinit var binder: ActivityAdMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binder = ActivityAdMainBinding.inflate(layoutInflater)
        setContentView(binder.root)

        playAd()
    }

    private fun playAd() {
        val adManager = AdSequenceManager.getInstance()
        val adList = adManager.getAdList()

        for (ad in adList) {
            lateinit var fragment: Fragment
            val filePath1 = FilenameUtils.getName(ad.url1)
            val filePath2 = FilenameUtils.getName(ad.url2)
            val filePath3 = FilenameUtils.getName(ad.url3)
            Log.d("JDEBUG", "filePath1 : $filePath1")
            when (ad.template) {
                1 -> {
                    fragment = TemplateFirstFragment(filePath1)
                }
                2 -> {
                    fragment = TemplateSecondFragment(
                        filePath1, filePath2
                    )
                }
                3 -> {
                    fragment = TemplateThirdFragment(
                        filePath1, filePath2, filePath3
                    )
                }
                4 -> {
                    fragment = TemplateFourthFragment(
                        filePath1, filePath2
                    )
                }
                5 -> {
                    fragment = TemplateFifthFragment(
                        filePath1, filePath2, filePath3
                    )
                }
            }
            val fragmentTransaction = supportFragmentManager.beginTransaction()
            fragmentTransaction.replace(R.id.fragment_container_view, fragment).commit()
        }

//        Handler().postDelayed({
//            val t2Fragment = TemplateSecondFragment(
//                "$DOWNLOAD_PATH/iv1.png",
//                "$DOWNLOAD_PATH/Download-2.mp4"
//            )
//
//            val fragmentTransaction = supportFragmentManager.beginTransaction()
//            fragmentTransaction.replace(R.id.fragment_container_view, t2Fragment).commit()
//    }, 3000)
    }
}