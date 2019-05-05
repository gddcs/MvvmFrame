package com.example.mvvmframe

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.view.View
import com.example.mvvmframe.base.BaseActivity
import com.example.mvvmframe.viewmodel.NoViewModel
import com.example.mvvmframe.databinding.ActivityMainBinding
import com.example.mvvmframe.ui.login.LoginActivity

class MainActivity : BaseActivity<NoViewModel, ActivityMainBinding>() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        Handler().postDelayed({showContentView()},1000)
        setTitle("这是main activity")
        bindingView.tvMain.text = "这是main activity"
        bindingView.btLogin.setOnClickListener(View.OnClickListener {
            startActivity(Intent(this, LoginActivity::class.java))
        })
    }
}
