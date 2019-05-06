package com.example.mvvmframe.viewmodel.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import android.text.TextUtils
import android.util.Log
import com.example.mvvmframe.http.BBRHttpClient
import com.example.mvvmframe.util.ToastUtil
import io.reactivex.Scheduler
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.schedulers.Schedulers

/**
 * @author GDDCS
 * create at 2019/5/6 9:40
 * description:
 */

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val username = ObservableField<String>()
    val password = ObservableField<String>()

    fun register(): MutableLiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        if (verifyData()) {
            data.value = true
        }
        return data
    }

    fun login(): MutableLiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        if (verifyData()) {
            BBRHttpClient.Builder.getWanAndroidServer().login(username.get() ?: "", password.get() ?: "")
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.io())
                .subscribe(
                    {
                        if (it != null && it.data != null) {
                            data.value = true
                        } else {
                            it.let {
                                ToastUtil.showToast(it.errorMsg)
                            }
                            data.value = false
                        }
                    },
                    {
                        ToastUtil.showToast(it.toString())
                    })
        }
        return data
    }

    private fun verifyData(): Boolean {
        if (TextUtils.isEmpty(username.get())) {
            ToastUtil.showToast("请输入用户名")
            return false
        } else if (TextUtils.isEmpty(password.get())) {
            ToastUtil.showToast("请输入密码")
            return false
        }
        return true
    }

}