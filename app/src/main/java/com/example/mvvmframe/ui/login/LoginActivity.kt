package com.example.mvvmframe.ui.login

import android.arch.lifecycle.Observer
import android.os.Bundle
import android.util.Log
import android.view.View
import com.example.mvvmframe.R
import com.example.mvvmframe.base.BaseActivity
import com.example.mvvmframe.databinding.ActivityLoginBinding
import com.example.mvvmframe.viewmodel.login.LoginViewModel

class LoginActivity : BaseActivity<LoginViewModel, ActivityLoginBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        setTitle("这是登录界面")
        showContentView()
        bindingView.loginviewmodel = viewModel

        bindingView.btLogin.setOnClickListener { viewModel.login().observe(this, Observer {
            loadSuccess(it) }) }
        bindingView.btRegister.setOnClickListener { viewModel.register().observe(this, Observer { loadSuccess(it) }) }
    }

    /**
     * 注册或登录成功
     */
    fun loadSuccess(aBoolean: Boolean?) {
        if (aBoolean != null && aBoolean) {
            finish()
        }
    }
}
