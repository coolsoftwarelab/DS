package com.ds.soonda.service

import com.ds.soonda.model.AdInfoDto
import retrofit2.Response
import retrofit2.http.POST
import retrofit2.http.Path

interface ApiService {
    //@POST("/_app/digitalSignage/device")

    @POST("/_app/digitalSignage/deviceTest/{deviceUUID}/{isRentalCertifyNumber}") // dev
    suspend fun reqServerAdInfo(
        @Path("deviceUUID") uuid: String,
        @Path("isRentalCertifyNumber") isRentalCertifyNumber: String
    ): Response<AdInfoDto>

}