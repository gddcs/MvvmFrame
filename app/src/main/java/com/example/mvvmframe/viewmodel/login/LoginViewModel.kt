package com.example.mvvmframe.viewmodel.login

import android.app.Application
import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.MutableLiveData
import android.databinding.ObservableField
import android.text.TextUtils
import android.util.Log
import com.example.mvvmframe.util.ToastUtil

class LoginViewModel(application: Application) : AndroidViewModel(application) {

    val username = ObservableField<String>()
    val password = ObservableField<String>()

    fun register () : MutableLiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        if(verifyData()){
            data.value = true
        }
        return data
    }

    fun login () : MutableLiveData<Boolean> {
        val data = MutableLiveData<Boolean>()
        if(verifyData()){
            Log.e("TTTT","11111111")
            data.value = false
            Thread.sleep(1000)
            data.value = true
        }
        return data
    }

    private fun verifyData () : Boolean{
        if (TextUtils.isEmpty(username.get())){
            ToastUtil.showToast("请输入用户名")
            return false
        }else if (TextUtils.isEmpty(password.get())){
            ToastUtil.showToast("请输入密码")
            return false
        }
        return true
    }

}