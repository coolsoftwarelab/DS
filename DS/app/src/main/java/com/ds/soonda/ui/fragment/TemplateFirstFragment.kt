package com.ds.soonda.ui.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.VideoView
import androidx.fragment.app.Fragment
import com.ds.soonda.databinding.FragmentTemplateFirstBinding
import com.ds.soonda.model.AcroMediaFileType
import com.ds.soonda.util.Utils

class TemplateFirstFragment(vararg adFilePath: String) : Fragment() {

    private lateinit var binder: FragmentTemplateFirstBinding

    private val adFilePath: Array<String> = adFilePath as Array<String>

    private lateinit var imageResArr: Array<ImageView>
    private lateinit var videoResArr: Array<VideoView>

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder = FragmentTemplateFirstBinding.inflate(inflater)
        return binder.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        imageResArr = arrayOf(binder.image1)
        videoResArr = arrayOf(binder.video1)

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onResume() {
        super.onResume()
        setContents()
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
                    it.isLooping = true
                }
                videoView.start()
            }
        }
    }

    override fun onDestroyView() {
        for(view in videoResArr) {
            view.stopPlayback()
        }
        super.onDestroyView()
    }
}