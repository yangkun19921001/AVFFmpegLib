package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.content.Intent
import android.media.MediaPlayer
import android.os.Bundle
import android.util.Log
import android.view.SurfaceHolder
import com.devyk.av.ffmpegcmd.widget.RangeSlider
import android.view.View
import kotlinx.android.synthetic.main.activity_clip.*
import android.net.Uri
import android.widget.Toast
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.LinearLayoutManager
import com.devyk.av.ffmpegcmd.adapter.ThumbnailAdapter
import com.devyk.av.ffmpegcmd.utils.FileUtils
import com.devyk.av.ffmpegcmd.utils.TimeUtil
import com.devyk.ffmpeglib.AVEditor
import com.devyk.ffmpeglib.callback.ExecuteCallback
import com.devyk.ffmpeglib.entity.*
import com.devyk.ffmpeglib.util.VideoUitls
import java.io.File


/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:47
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is ClipActivity
 * </pre>
 */
public class ClipActivity : BaseActivity(), SurfaceHolder.Callback {
    //Android MediaPlayer
    var mediaPlayer: MediaPlayer? = null

    //视频数据集合
    private var mThumbnail: MutableList<File> = mutableListOf<File>()

    //滑动的开始时间
    private var mStartTime: Long = 0
    //滑动的结束时间
    private var mEndTime: Long = 0
    //总的时间
    private var mTotalTime: Long = 0
    //视频信息
    private var mVideoInfo: VideoInfo? = null
    //播放是否暂停
    private var mIsPlsyer = false;
    //上层传递过来的视频数据
    private var mVideoEntity: VideoEntity? = null

    @SuppressLint("ResourceType")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_clip)
        //添加 surface 预览回调
        surface.holder.addCallback(this)

        //标签
        title = "视频剪辑"
        setBackBtnVisible(true)
        //获取上层传递过来的视频信息
        mVideoEntity = getVideoEntity()


        //获取视频元数据
        mVideoInfo = VideoUitls.getVideoInfo(mVideoEntity!!.videoPath)
        mTotalTime = mVideoInfo!!.duration!!.toLong()

        //根据视频宽高动态修改预览宽高
        changeSurfaceSize(mVideoInfo!!.videoWidth!!.toInt(), mVideoInfo!!.videoHeight!!.toInt())

        //总时长
        val format = TimeUtil.format(mTotalTime)
        tv_duration.setText(format)
        tv_end_time.setText(format)

        //水平布局
        video_thumbnails.setLayoutManager(
                LinearLayoutManager(
                        applicationContext,
                        LinearLayoutManager.HORIZONTAL,
                        false
                )
        )

        //缩略图适配器
        mThumbnail.clear()
        var mThumbnailAdapter = ThumbnailAdapter(mThumbnail)
        video_thumbnails.setAdapter(mThumbnailAdapter)

        //1s 获取一次缩略图
        setThumbnailData(mThumbnailAdapter)

        //滑动截取时间控件改变的回调
        range_seek_bar.setRangeChangeListener(object : RangeSlider.OnRangeChangeListener {
            override fun onRangeChange(view: RangeSlider, type: Int, lThumbIndex: Int, rThumbIndex: Int) {
                mStartTime = (lThumbIndex.toFloat() / 100 * mTotalTime).toLong()
                mEndTime = (rThumbIndex.toFloat() / 100 * mTotalTime).toLong()
                val duration = mEndTime - mStartTime
                runOnUiThread {
                    when (type) {
                        RangeSlider.TYPE_LEFT -> mediaPlayer?.seekTo((mStartTime).toInt())
                        RangeSlider.TYPE_RIGHT -> mediaPlayer?.seekTo((mEndTime).toInt())
                    }
                    tv_end_time.setText(TimeUtil.format(mEndTime))
                    tv_duration.setText(TimeUtil.format(duration))
                }
            }
        })

        //surface 点击事件
        surface.setOnClickListener {
            if (mIsPlsyer) {
                iv_play_status.setVisibility(View.GONE)
                mediaPlayer?.start()
            } else {
                iv_play_status.setVisibility(View.VISIBLE)
                mediaPlayer?.pause()
            }
            mIsPlsyer = !mIsPlsyer

        }
    }

    private fun setThumbnailData(mThumbnailAdapter: ThumbnailAdapter) {
        var mFile = File("sdcard/aveditor/thumbnail/")
        FileUtils.deleteDirectory(mFile)
        mFile.mkdirs()

        AVEditor.video2pic(
                mVideoEntity!!.videoPath,
                "${mFile.absolutePath}/${System.currentTimeMillis()}_%3d.jpg",
                60,
                60,
                1.0f,
                object : ExecuteCallback {
                    override fun onStart(executionId: Long?) {
                        initProgressDialog()
                        showProgressDialog()
                    }

                    override fun onSuccess(executionId: Long) {
                        val e = Log.e(TAG, "video2pic ok")
                        var files = mFile?.listFiles()
                        files.forEach { file ->
                            mThumbnail.add(file)
                        }
                        mThumbnailAdapter.notifyDataSetChanged()
                        dismissProgressDialog()
                    }

                    override fun onFailure(executionId: Long, error: String?) {
                        Log.e(TAG, "video2pic error:${error}")
                        showMessage(error)
                        dismissProgressDialog()
                    }

                    override fun onCancel(executionId: Long) {
                    }

                    override fun onFFmpegExecutionMessage(logMessage: LogMessage?) {
                        Log.d(TAG, logMessage?.text)
                    }

                    override fun onProgress(progress: Float) {
                        Log.e(TAG, "video2pic onProgress:$progress")
                        updateProgress(progress)
                    }
                })
    }

    private fun getVideoEntity(): VideoEntity? {
        intent?.extras?.let { bundle ->
            return bundle.getParcelableArrayList<VideoEntity>(VideoListsActivity.VIDEO_ENTITY)?.get(0)
        }
        return null
    }


    /**
     * 开始裁剪
     */
    fun startClipVideo(view: View) {
        var edit = AVVideo(mVideoEntity!!.videoPath);
        //单位为 s
        var startClip = mStartTime / 1000f
        var duration = mEndTime / 1000f - startClip
        val outFile = "sdcard/aveditor/clip_${System.currentTimeMillis()}.mp4"
        val outputOption = OutputOption(outFile)
        //根据开始位置和结束位置进行裁剪
        edit.clip(
                startClip,
                duration
        )
        mVideoInfo?.let { videoInfo ->
            outputOption.width_ = videoInfo.videoWidth!!.toInt()
            outputOption.height_ = videoInfo.videoHeight!!.toInt()
        }

        AVEditor.exec(
                edit,
                outputOption
                ,
                object : ExecuteCallback {
                    override fun onStart(executionId: Long?) {
                        initProgressDialog()
                        showProgressDialog()
                    }

                    override fun onSuccess(executionId: Long) {
                        Toast.makeText(applicationContext, "剪辑完成!", Toast.LENGTH_LONG).show()
                        dismissProgressDialog()
                        val v = Intent(Intent.ACTION_VIEW)
                        v.setDataAndType(Uri.parse(outFile), "video/mp4")
                        startActivity(v)
                    }

                    override fun onFailure(executionId: Long, error: String?) {
                        Toast.makeText(applicationContext, "剪辑失败!${error}", Toast.LENGTH_LONG).show()
                        dismissProgressDialog()
                        showMessage(error)
                        Log.e(TAG, "startClipVideo onFailure:$error")
                    }

                    override fun onCancel(executionId: Long) {
                    }

                    override fun onFFmpegExecutionMessage(logMessage: LogMessage?) {
                        Log.d(TAG, logMessage?.text);
                    }

                    override fun onProgress(progress: Float) {
                        Log.e(TAG, "startClipVideo onProgress:$progress")
                        updateProgress(progress)

                    }
                })

    }


    override fun onDestroy() {
        super.onDestroy()
        mThumbnail.clear()
    }


    override fun surfaceChanged(holder: SurfaceHolder?, format: Int, width: Int, height: Int) {
        mVideoEntity?.let {
            if (mediaPlayer != null) {
                mediaPlayer?.stop()
                mediaPlayer?.release()
            }
            mediaPlayer = MediaPlayer()
            mediaPlayer?.let { mediaPlayer ->
                mediaPlayer.setDataSource(mVideoEntity?.videoPath)
                mediaPlayer.setDisplay(holder)
                mediaPlayer.prepare()
                mediaPlayer.start()
            }
        }
    }

    override fun surfaceDestroyed(holder: SurfaceHolder?) {
        mediaPlayer?.stop()
        mediaPlayer?.release()
        mediaPlayer = null
    }

    override fun surfaceCreated(holder: SurfaceHolder?) {}


    private fun changeSurfaceSize(videoWidth: Int?, videoHeight: Int?) {
        var layoutParams = surface.getLayoutParams() as ConstraintLayout.LayoutParams
        layoutParams.width = videoWidth!!.toInt();
        layoutParams.height = videoHeight!!.toInt();
        layoutParams.verticalBias = 0.5f;
        layoutParams.horizontalBias = 0.5f;
        surface.setLayoutParams(layoutParams);
    }
}