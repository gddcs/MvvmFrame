package com.example.mvvmframe.http

import com.example.mvvmframe.bean.login.LoginBean
import io.reactivex.Observable
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.POST

interface BBRHttpClient {
    class Builder{
        companion object{
            val API_WAN_ANDROID = "https://www.wanandroid.com/"

            fun getWanAndroidServer() : BBRHttpClient{
                return HttpUtils.instance.getBuilder(API_WAN_ANDROID).build().create(BBRHttpClient::class.java);
            }
        }
    }

    @FormUrlEncoded
    @POST("user/login")
    fun login(@Field("username") username: String, @Field("password") password: String): Observable<LoginBean>
}