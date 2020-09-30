package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.app.ProgressDialog
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import com.devyk.av.ffmpegcmd.entity.VideoEntity
import com.devyk.av.library.*
import java.util.ArrayList

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:47
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is MergeActivity
 * </pre>
 */
class MergeActivity : BaseActivity(), View.OnClickListener {


    private val CHOOSE_FILE = 11
    private var tv_add: TextView? = null
    private var bt_add: Button? = null
    private var bt_merge: Button? = null
    private var videoList: MutableList<AVVideo>? = null
    private var mProgressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_merge)
        title = "视频合并"
        setBackBtnVisible(true)
        initView()

        getVideoEntityLists()?.forEach { videoEntity ->
            tv_add?.setText("${tv_add?.getText()} ${videoEntity.videoPath} \n")
            val epVideo = AVVideo(videoEntity.videoPath)
            videoList?.add(epVideo)
        }
    }

    private fun getVideoEntityLists(): ArrayList<VideoEntity>? {
        intent?.extras?.let { bundle ->
            return bundle.getParcelableArrayList<VideoEntity>(VideoListsActivity.VIDEO_ENTITY)
        }
        return null
    }

    private fun initView() {
        tv_add = findViewById(R.id.tv_add) as TextView
        bt_add = findViewById(R.id.bt_add) as Button
        bt_merge = findViewById(R.id.bt_merge) as Button
        videoList = ArrayList()
        mProgressDialog = ProgressDialog(this)
        mProgressDialog!!.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL)
        mProgressDialog!!.max = 100
        mProgressDialog!!.setCancelable(false)
        mProgressDialog!!.setCanceledOnTouchOutside(false)
        mProgressDialog!!.setTitle("正在处理")
        bt_add!!.setOnClickListener(this)
        bt_merge!!.setOnClickListener(this)
    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_add -> chooseFile()
            R.id.bt_merge -> mergeVideo()
        }
    }


    /**
     * 选择文件
     */
    private fun chooseFile() {
        val intent = Intent()
        intent.type = "video/*"
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, CHOOSE_FILE)
    }


    @SuppressLint("MissingSuperCall")
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHOOSE_FILE -> if (resultCode == RESULT_OK) {
                try {
                    val filePathForN = UriUtils.getFilePathForN(data?.getData(), this@MergeActivity)
                    tv_add?.setText("${tv_add?.getText()} $filePathForN \n")
                    val epVideo = AVVideo(filePathForN)
                    videoList?.add(epVideo)
                } catch (r: Exception) {
                    Log.e("error:", r.toString())
                }
            }
        }
    }

    /**
     * 合并视频
     */
    private fun mergeVideo() {
        if (videoList!!.size > 1) {
            mProgressDialog!!.progress = 0
            mProgressDialog!!.show()
            val outPath = "sdcard/aveditor/outmerge.mp4"
            val outputOption = AVEditor.OutputOption(outPath);
            outputOption.setWidth(720)
            outputOption.setHeight(1280)
            AVEditor.merge(videoList!!, AVEditor.OutputOption(outPath), object : OnEditorListener {
                override fun onSuccess() {
                    runOnUiThread {
                        Toast.makeText(this@MergeActivity, "编辑完成:$outPath", Toast.LENGTH_SHORT).show()
                        mProgressDialog!!.dismiss()
                        val v = Intent(Intent.ACTION_VIEW)
                        v.setDataAndType(Uri.parse(outPath), "video/mp4")
                        startActivity(v)
                    }
                }

                override fun onFailure() {
                    runOnUiThread {
                        Toast.makeText(this@MergeActivity.applicationContext, "编辑失败", Toast.LENGTH_SHORT).show()
                        mProgressDialog!!.dismiss()
                    }
                }

                override fun onProgress(v: Float) {
                    runOnUiThread {
                        mProgressDialog!!.progress = (v * 100).toInt()
                    }
                }

            })
        } else {
            Toast.makeText(this, "至少添加两个视频", Toast.LENGTH_SHORT).show()
        }
    }


    companion object {
        fun open(context: Context) {
            val intent = Intent(context, MergeActivity::class.java)
            context.startActivity(intent)
        }
    }

}
