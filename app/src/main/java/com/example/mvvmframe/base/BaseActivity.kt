package com.example.mvvmframe.base

import android.arch.lifecycle.AndroidViewModel
import android.arch.lifecycle.ViewModelProvider
import android.arch.lifecycle.ViewModelProviders
import android.databinding.DataBindingUtil
import android.databinding.ViewDataBinding
import android.graphics.drawable.AnimationDrawable
import android.os.Build
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewStub
import android.widget.ImageView
import android.widget.RelativeLayout
import com.example.mvvmframe.R
import com.example.mvvmframe.databinding.ActivityBaseBinding
import com.example.mvvmframe.util.ClassUtil
import com.example.mvvmframe.util.CommonUtils
import com.example.mvvmframe.util.NoDoubleClickListenerUtil
import com.example.mvvmframe.util.StatusBarUtil

abstract class BaseActivity<VM : AndroidViewModel, VDB : ViewDataBinding> : AppCompatActivity() {

    // ViewModel
    protected lateinit var viewModel: VM
    // 布局view
    protected lateinit var bindingView: VDB
    // base公共布局
    private lateinit var mBaseBinding: ActivityBaseBinding
    private var loadingView: View ?= null
    private var errorView: View ?= null

    private var mAnimationDrawable: AnimationDrawable? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun setContentView(layoutResID: Int) {
        mBaseBinding = DataBindingUtil.inflate(LayoutInflater.from(this), R.layout.activity_base, null, false)
        bindingView = DataBindingUtil.inflate<VDB>(layoutInflater, layoutResID, null, false)

        // content
        val params =
            RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        bindingView.root.layoutParams = params
        val mContainer = mBaseBinding.getRoot().findViewById(R.id.container) as RelativeLayout
        mContainer.addView(bindingView.root)
        window.setContentView(mBaseBinding.getRoot())

        // 设置透明状态栏，兼容4.4
        StatusBarUtil.setColor(this, CommonUtils.getColor(R.color.colorPrimary), 0)
        loadingView = (findViewById(R.id.vs_loading) as ViewStub).inflate()
        val img = loadingView?.findViewById(R.id.img_progress) as ImageView

        // 加载动画
        mAnimationDrawable = img.drawable as AnimationDrawable
        // 默认进入页面就开启动画
        if (!(mAnimationDrawable?.isRunning ?: true)) {
            mAnimationDrawable?.start()
        }

        setToolBar()
        bindingView.root.visibility = View.GONE
        initViewModel()

    }

    /**
     * 设置titlebar
     */
    protected fun setToolBar() {
        setSupportActionBar(mBaseBinding.toolBar)
        val actionBar = supportActionBar
        if (actionBar != null) {
            //去除默认Title显示
            actionBar.setDisplayShowTitleEnabled(false)
            actionBar.setDisplayHomeAsUpEnabled(true)
            actionBar.setHomeAsUpIndicator(R.drawable.abc_ic_ab_back_material)
        }
        mBaseBinding.toolBar.setNavigationOnClickListener {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                finishAfterTransition()
            } else {
                onBackPressed()
            }
        }
    }

    /**
     * 初始化ViewModel
     */
    private fun initViewModel() {
        val viewModelClass: Class<VM>? = ClassUtil.getViewModel(this)
        viewModelClass?.let {
            this.viewModel = ViewModelProviders.of(this).get(it)
        }
    }

    override fun setTitle(title: CharSequence?) {
        mBaseBinding.toolBar.title = title
    }

    protected fun showLoading() {
        if (loadingView != null && loadingView?.visibility != View.VISIBLE) {
            loadingView?.visibility = View.VISIBLE
        }
        // 开始动画
        if (!(mAnimationDrawable?.isRunning() ?: true)) {
            mAnimationDrawable?.start()
        }
        bindingView.root.visibility?.let { bindingView.root.visibility = View.GONE }
        errorView?.let { errorView?.setVisibility(View.GONE) }
    }

    protected fun showContentView() {
        if (loadingView != null && loadingView?.visibility != View.GONE) {
            loadingView?.visibility = View.GONE
        }
        // 停止动画
        if (mAnimationDrawable?.isRunning()?:false) {
            mAnimationDrawable?.stop()
        }
        errorView?.let { errorView?.setVisibility(View.GONE) }
        if (bindingView.root.visibility != View.VISIBLE) {
            bindingView.root.visibility = View.VISIBLE
        }
    }

    protected fun showError() {
        if (loadingView != null && loadingView?.visibility != View.GONE) {
            loadingView?.visibility = View.GONE
        }
        // 停止动画
        if (mAnimationDrawable?.isRunning()?:false) {
            mAnimationDrawable?.stop()
        }

        if (errorView == null) {
            val viewStub = findViewById(R.id.vs_error_refresh) as ViewStub
            errorView = viewStub.inflate()
            // 点击加载失败布局
            errorView?.setOnClickListener(object : NoDoubleClickListenerUtil() {
                protected override fun onNoDoubleClick(v: View) {
                    showLoading()
                    onRefresh()
                }
            })
        } else {
            errorView?.setVisibility(View.VISIBLE)
        }
        if (bindingView.root.visibility != View.GONE) {
            bindingView.root.visibility = View.GONE
        }
    }

    /**
     * 失败后点击刷新
     */
    protected fun onRefresh() {

    }


}