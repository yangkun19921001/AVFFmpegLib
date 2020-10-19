package com.devyk.av.ffmpegcmd

import android.os.Bundle
import android.util.Log
import android.view.View
import com.devyk.ffmpeglib.callback.ExecuteCallback
import com.devyk.av.ffmpegcmd.utils.AppUtils
import com.devyk.ffmpeglib.AVEditor
import com.devyk.ffmpeglib.entity.LogMessage
import com.devyk.ffmpeglib.util.VideoUitls
import kotlinx.android.synthetic.main.activity_main.*

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:47
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is MainActivity
 * </pre>
 */
class MainActivity : BaseActivity() {


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        tv_app_version.setText("v${AppUtils.getVersionName(this)}")
    }
    /**
     * 视频合并
     */
    fun mergeVideo(view: View) {
        openUI(this, VideoListsActivity::class.java, VideoListsActivity.TYPE_VIDEO_MERGE)
    }

    /**
     * 视频剪辑
     */
    fun clipVideo(view: View) {
        openUI(this, VideoListsActivity::class.java, VideoListsActivity.TYPE_VIDEO_CLIP)
    }

    /**
     * 视频编辑
     */
    fun editorVideo(view: View) {
        openUI(this, VideoListsActivity::class.java, VideoListsActivity.TYPE_VIDEO_EDITOR)
    }

    /**
     * 视频添加水印
     */
    fun watermarkVideo(view: View) {
        openUI(this, VideoListsActivity::class.java, VideoListsActivity.TYPE_VIDEO_WATERMARK)
    }
}
