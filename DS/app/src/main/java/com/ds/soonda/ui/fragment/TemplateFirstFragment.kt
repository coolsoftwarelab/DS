package com.ds.soonda.ui.fragment

import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.ds.soonda.R
import com.ds.soonda.databinding.FragmentTemplateFirstBinding
import com.ds.soonda.model.AdInfoDto
import com.ds.soonda.model.AcroMediaFileType
import com.ds.soonda.util.Utils

class TemplateFirstFragment : Fragment() {
    private lateinit var binder: FragmentTemplateFirstBinding

    override fun onAttach(context: Context) {
        super.onAttach(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binder = FragmentTemplateFirstBinding.inflate(inflater)
        return binder.root
    }

    fun setContentsInfo(vararg filePath: String) {
        val type = Utils.getAcroMediaFileType(filePath[0])
        if (type == AcroMediaFileType.IMAGE) {
            binder.image1.visibility = View.VISIBLE
            binder.image1.setImageURI(Uri.parse(filePath[0]))
        } else {
            binder.video1.visibility = View.VISIBLE
            binder.video1.setVideoPath(filePath[0])
        }
    }


}