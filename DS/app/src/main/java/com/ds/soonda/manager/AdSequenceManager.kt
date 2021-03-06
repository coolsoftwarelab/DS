package com.ds.soonda.manager

import com.ds.soonda.model.Ad

class AdSequenceManager private constructor() {

    private var adList: ArrayList<Ad> = ArrayList()

    fun setAdList(list: ArrayList<Ad>) {
        adList = list
    }

    fun getAdList(): ArrayList<Ad> {
         return adList
    }

    companion object {
        private val INSTANCE = AdSequenceManager()

        fun getInstance(): AdSequenceManager {
            return INSTANCE
        }
    }

}