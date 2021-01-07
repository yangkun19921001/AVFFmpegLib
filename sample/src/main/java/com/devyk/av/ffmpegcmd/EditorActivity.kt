package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import com.devyk.ffmpeglib.callback.ExecuteCallback
import com.devyk.av.ffmpegcmd.utils.UriUtils
import com.devyk.ffmpeglib.AVEditor
import com.devyk.ffmpeglib.entity.*
import com.devyk.ffmpeglib.util.VideoUitls
import kotlinx.android.synthetic.main.activity_editor.*

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

    private val CHOOSE_VIDEO = 10
    private val CHOOSE_IMAGE = 9
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
    private var imageUrl: String? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_editor)
        window.setBackgroundDrawableResource(android.R.color.white)
        initView()

        title = "视频编辑"
        setBackBtnVisible(true)
        tv_file?.setText(getVideoEntity()?.videoPath)
        videoUrl = getVideoEntity()?.videoPath
    }

    private fun getVideoEntity(): VideoEntity? {
        intent?.extras?.let { bundle ->
            return bundle.getParcelableArrayList<VideoEntity>(VideoListsActivity.VIDEO_ENTITY)?.get(0)
        }
        return null
    }


    private fun initView() {
        cb_clip = findViewById<CheckBox>(R.id.cb_clip)
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
        bt_logo?.setOnClickListener(this)

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


    }

    override fun onClick(v: View) {
        when (v.id) {
            R.id.bt_file -> chooseFile("video/*", CHOOSE_VIDEO)
            R.id.bt_exec -> execVideo()
            R.id.bt_logo -> chooseFile("image/*", CHOOSE_IMAGE)
        }
    }


    /**
     * 选择文件
     */
    private fun chooseFile(type: String, requestCode: Int) {
        val intent = Intent()
        intent.type = type
        intent.action = Intent.ACTION_GET_CONTENT
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        startActivityForResult(intent, requestCode)
    }

    @SuppressLint("MissingSuperCall")
    protected override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        when (requestCode) {
            CHOOSE_VIDEO -> if (resultCode == RESULT_OK) {
                videoUrl = UriUtils.getFilePathByUri(this@EditorActivity, data?.data)
                tv_file?.setText(videoUrl)
            }
            CHOOSE_IMAGE -> if (resultCode == RESULT_OK) {
                imageUrl = UriUtils.getFilePathByUri(this@EditorActivity, data?.data)
                tv_logo_path?.setText(imageUrl)
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
            if (cb_text!!.isChecked()) {
                var start = 0;
                var end = 2;
                for (i in 0..3) {
                    epVideo.addText(AVText(et_text_x?.text.toString().toInt(),
                            et_text_y?.text.toString().toInt(),
                            et_text_size.text.toString().toFloat(),
                            AVText.Color.White,
                            "sdcard/aveditor/MicrosoftYahei.ttf",
                            et_text?.text.toString().trim() + i,
                            AVText.Time(start.toInt(), end.toInt())));
                    start = end;
                    end = 2 + start;
                }
            }

            if (cb_add_logo!!.isChecked) {
                epVideo.addDraw(AVDraw(tv_logo_path.text.toString().trim(),
                        200, 200,
                        60f, 60f,
                        false,
                        0, 10
                ))
            }


            val outPath = "sdcard/aveditor/editor_${System.currentTimeMillis()}.mp4"


            AVEditor.exec(epVideo, OutputOption(outPath), object : ExecuteCallback {
                override fun onStart(executionId: Long?) {
                    initProgressDialog()
                    showProgressDialog()
                }

                override fun onSuccess(executionId: Long) {
                    dismissProgressDialog()
                    val outPath_ = "sdcard/aveditor/editor_${System.currentTimeMillis()}.mp4"
                    if (cb_reverse.isChecked)
                        AVEditor.reverse(outPath, outPath_, true, true, object : ExecuteCallback {
                            override fun onStart(executionId: Long?) {
                                initProgressDialog()
                                showProgressDialog()
                                showMessage("处理回放...")
                            }

                            override fun onProgress(v: Float) {
                                updateProgress(v)
                            }

                            override fun onSuccess(executionId: Long) {
                                Toast.makeText(this@EditorActivity, "编辑完成:$outPath", Toast.LENGTH_SHORT).show()
                                dismissProgressDialog()
                                val v = Intent(Intent.ACTION_VIEW)
                                v.setDataAndType(Uri.parse(outPath_), "video/mp4")
                                startActivity(v)
                            }

                            override fun onFailure(executionId: Long, error: String?) {
                                showMessage(error)
                                dismissProgressDialog()
                            }

                            override fun onCancel(executionId: Long) {
                                dismissProgressDialog()
                            }

                            override fun onFFmpegExecutionMessage(logMessage: LogMessage?) {
                                dismissProgressDialog()
                            }
                        })
                    else {
                        Toast.makeText(this@EditorActivity, "编辑完成:$outPath", Toast.LENGTH_SHORT).show()
                        dismissProgressDialog()
                        val v = Intent(Intent.ACTION_VIEW)
                        v.setDataAndType(Uri.parse(outPath), "video/mp4")
                        startActivity(v)
                    }
                }

                override fun onFailure(executionId: Long, error: String?) {
                    Toast.makeText(this@EditorActivity, "编辑失败:$error", Toast.LENGTH_SHORT).show()
                    dismissProgressDialog()
                    Log.d(TAG, "onFailure:$error")
                }

                override fun onCancel(executionId: Long) {
                    dismissProgressDialog()
                }

                override fun onFFmpegExecutionMessage(logMessage: LogMessage?) {
                    Log.d(TAG, "onFFmpegExecutionMessage:${logMessage?.text}")
                }


                override fun onProgress(progress: Float) {
                    updateProgress(progress)
                }
            })
        }
    }
}