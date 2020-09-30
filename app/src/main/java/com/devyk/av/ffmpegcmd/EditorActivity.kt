package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import com.devyk.av.ffmpegcmd.entity.VideoEntity
import com.devyk.av.library.*

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:49
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is EditorActivity
 * </pre>
 */
class EditorActivity : BaseActivity(), View.OnClickListener {

    private val CHOOSE_FILE = 10
    private var cb_clip: CheckBox? = null
    private var cb_crop: CheckBox? = null
    private var cb_rotation: CheckBox? = null
    private var cb_mirror: CheckBox? = null
    private var cb_text: CheckBox? = null
    private var et_clip_start: EditText? = null
    private var et_clip_end: EditText? = null
    private var et_crop_x: EditText? = null
    private var et_crop_y: EditText? = null
    private var et_crop_w: EditText? = null
    private var et_crop_h: EditText? = null
    private var et_rotation: EditText? = null
    private var et_text_x: EditText? = null
    private var et_text_y: EditText? = null
    private var et_text: EditText? = null
    private var tv_file: TextView? = null
    private var bt_file: Button? = null
    private var bt_exec: Button? = null
    private var videoUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        window.setBackgroundDrawableResource(android.R.color.white)
        initView()

        title = "视频编辑"
        setBackBtnVisible(true)
        tv_file?.setText(getVideoEntity()?.videoPath)
    }

    private fun getVideoEntity(): VideoEntity? {
        intent?.extras?.let { bundle ->
            return bundle.getParcelableArrayList<VideoEntity>(VideoListsActivity.VIDEO_ENTITY)?.get(0)
        }
        return null
    }


    private fun initView() {
        cb_clip = findViewById(R.id.cb_clip) as CheckBox
        cb_crop = findViewById(R.id.cb_crop) as CheckBox
        cb_rotation = findViewById(R.id.cb_rotation) as CheckBox
        cb_mirror = findViewById(R.id.cb_mirror) as CheckBox
        cb_text = findViewById(R.id.cb_text) as CheckBox
        et_clip_start = findViewById(R.id.et_clip_start) as EditText
        et_clip_end = findViewById(R.id.et_clip_end) as EditText
        et_crop_x = findViewById(R.id.et_crop_x) as EditText
        et_crop_y = findViewById(R.id.et_crop_y) as EditText
        et_crop_w = findViewById(R.id.et_crop_w) as EditText
        et_crop_h = findViewById(R.id.et_crop_h) as EditText
        et_rotation = findViewById(R.id.et_rotation) as EditText
        et_text_x = findViewById(R.id.et_text_x) as EditText
        et_text_y = findViewById(R.id.et_text_y) as EditText
        et_text = findViewById(R.id.et_text) as EditText
        tv_file = findViewById(R.id.tv_file) as TextView
        bt_file = findViewById(R.id.bt_file) as Button
        bt_exec = findViewById(R.id.bt_exec) as Button
        bt_file?.setOnClickListener(this)
        bt_exec?.setOnClickListener(this)
        cb_mirror?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                cb_rotation!!.setChecked(true)
            }
        })
        cb_rotation?.setOnCheckedChangeListener(CompoundButton.OnCheckedChangeListener { buttonView, isChecked ->
            if (!isChecked) {
                cb_mirror?.setChecked(false)
            }
        })

        initProgressDialog()

    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_file -> chooseFile()
            R.id.bt_exec -> execVideo()
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
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHOOSE_FILE -> if (resultCode == RESULT_OK) {
                videoUrl = UriUtils.getFilePathByUri(this@EditorActivity, data?.data)
                tv_file?.setText(videoUrl)
            }
        }
    }

    public fun execVideo() {
        if (videoUrl != null && "" != videoUrl) {

            val epVideo = AVVideo(videoUrl!!)
            if (cb_clip!!.isChecked())
                epVideo.clip(
                    et_clip_start?.getText().toString().trim().toFloat(),
                    et_clip_end?.getText().toString().trim().toFloat()
                )
            if (cb_crop!!.isChecked())
                epVideo.crop(
                    et_crop_w?.getText().toString().trim().toFloat(),
                    et_crop_h?.getText().toString().trim().toFloat(),
                    et_crop_x?.getText().toString().trim().toFloat(),
                    et_crop_y?.getText().toString().trim().toFloat()
                );

            if (cb_rotation!!.isChecked())
                epVideo.rotation(et_rotation?.getText().toString().trim().toInt(), cb_mirror!!.isChecked());
//            if (cb_text!!.isChecked())
//                epVideo.addText(EpText());

            showProgressDialog()
            val outPath = "sdcard/aveditor/merge_${System.currentTimeMillis()}.mp4"

            AVEditor.exec(epVideo, AVEditor.OutputOption(outPath), object : OnEditorListener {
                override fun onSuccess() {

                    runOnUiThread {
                        Toast.makeText(this@EditorActivity, "编辑完成:$outPath", Toast.LENGTH_SHORT).show()
                        dismissProgressDialog()
                        val v = Intent(Intent.ACTION_VIEW)
                        v.setDataAndType(Uri.parse(outPath), "video/mp4")
                        startActivity(v)
                    }

                }

                override fun onFailure() {
                    runOnUiThread {
                        Toast.makeText(this@EditorActivity, "编辑失败", Toast.LENGTH_SHORT).show()
                        dismissProgressDialog()
                    }
                }

                override fun onProgress(progress: Float) {
                    runOnUiThread {
                        updateProgress(progress * 100)
                    }
                }
            })
        }
    }
}