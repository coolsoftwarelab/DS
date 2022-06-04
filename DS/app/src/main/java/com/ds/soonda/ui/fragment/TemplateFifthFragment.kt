package com.ds.soonda.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.ds.soonda.databinding.FragmentTemplateFifthBinding
import com.ds.soonda.model.AcroMediaFileType
import com.ds.soonda.util.Utils

class TemplateFifthFragment(vararg adFilePath: String) : Fragment() {

    private val adFilePath: Array<String> = adFilePath as Array<String>

    private lateinit var binder: FragmentTemplateFifthBinding

    private lateinit var imageResArr: Array<ImageView>
    private lateinit var videoResArr: Array<VideoView>

    private var isVideoViewReadyCnt = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder = FragmentTemplateFifthBinding.inflate(inflater)
        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageResArr = arrayOf(binder.image1, binder.image2, binder.image3)
        videoResArr = arrayOf(binder.video1, binder.video2, binder.video3)

        setContents()

        super.onViewCreated(view, savedInstanceState)
    }

    private fun setContents() {
        for ((index, path) in adFilePath.withIndex()) {
            val type = Utils.getAcroMediaFileType(path)

            if (type == AcroMediaFileType.IMAGE) {
                val imageView = imageResArr[index]
                imageView.visibility = View.VISIBLE
                imageView.setImageURI(Uri.parse(path))
            } else {
                val videoView = videoResArr[index]
                videoView.visibility = View.VISIBLE
                videoView.setVideoPath(path)
                videoView.requestFocus()
                videoView.setOnPreparedListener {
                    if(++isVideoViewReadyCnt == videoResArr.size-1) {
                        for (view in videoResArr) {
                            view.start()
                        }
                    }
                }
//                videoView.start()
            }
        }
    }
}