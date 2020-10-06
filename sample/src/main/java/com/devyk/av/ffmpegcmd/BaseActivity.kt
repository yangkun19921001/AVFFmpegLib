package com.devyk.av.ffmpegcmd

import android.Manifest
import android.app.Activity
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:47
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is BaseActivity
 * </pre>
 */
open class BaseActivity : AppCompatActivity() {


    public var TAG = javaClass.simpleName

    private var mProgressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        initProgressDialog()

        if (Build.VERSION.SDK_INT > 23) {
            requestPermissions(
                arrayOf(
                    Manifest.permission.WRITE_EXTERNAL_STORAGE,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                ), 11
            )
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (grantResults[0] == 0) {

        }
    }

    public fun initProgressDialog() {
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog!!.max = 100
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setTitle("正在处理")
    }

    fun showProgressDialog() {
        mProgressDialog!!.progress = 0
        mProgressDialog!!.show()
    }

    fun hideProgressDialog() {
        mProgressDialog!!.hide()
    }

    fun dismissProgressDialog() {
        mProgressDialog!!.dismiss()
    }

    fun updateProgress(percent: Float) {
        mProgressDialog!!.progress = percent.toInt()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            finish()
            return true

        }
        return super.onOptionsItemSelected(item)
    }

    protected fun setBackBtnVisible(visible: Boolean) {
        val actionBar = supportActionBar
        actionBar?.setDisplayHomeAsUpEnabled(visible)
    }

    open fun <T : AppCompatActivity> openUI(context: Context, cls: Class<T>, openType: Int) {
        val intent = Intent(context, cls)
        intent.putExtra(VideoListsActivity.OPEN_UI_NAME,openType)
        context.startActivity(intent)
    }

    open fun <T : AppCompatActivity> openUI(context: Context, cls: Class<T>, bundle: Bundle?) {
        val intent = Intent(context, cls)
        bundle?.let { intent.putExtras(it) }
        context.startActivity(intent)
    }

    public fun showMessage(message: String?) {
        Toast.makeText(applicationContext, message, Toast.LENGTH_LONG).show()
    }
}
