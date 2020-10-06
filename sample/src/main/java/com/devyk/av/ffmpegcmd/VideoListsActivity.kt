package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.devyk.av.ffmpegcmd.adapter.VideoListsAdapter
import com.devyk.av.ffmpegcmd.utils.SpacingDecoration
import com.devyk.ffmpeglib.entity.VideoEntity
import kotlinx.android.synthetic.main.activity_video_lists.*
import java.io.File

/**
 * <pre>
 *     author  : devyk on 2020-09-29 15:17
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoListsActivity 视频列表
 * </pre>
 */
public class VideoListsActivity : BaseActivity(), VideoListsAdapter.OnItemClickListener {


    //视频列表适配器
    private var mVideoAdapter: VideoListsAdapter? = null
    //视频数据集合
    private var mVideoEntity: MutableList<VideoEntity> = mutableListOf<VideoEntity>()

    //视频数据集合
    private var mSelectVideoList: ArrayList<VideoEntity> = ArrayList<VideoEntity>()

    //视频数据更新，通知 adapter
    private val MSG_NOTIFY_DATA_CHANGED = 0x1
    //传递过来的 open_ui_type
    private var OPEN_UI_TYPE = TYPE_VIDEO_CLIP

    companion object {
        val TYPE_VIDEO_CLIP = 0x1
        val TYPE_VIDEO_MERGE = 0x2
        val TYPE_VIDEO_WATERMARK = 0x3
        val TYPE_VIDEO_EDITOR = 0x4
        val OPEN_UI_NAME = "OPEN_UI_TYPE"
        val VIDEO_ENTITY = "VideoEntity"
    }

    @SuppressLint("HandlerLeak")
    public var mHandler = object : Handler() {

        override fun handleMessage(msg: Message) {
            if (msg.what == MSG_NOTIFY_DATA_CHANGED) {
                mVideoAdapter?.notifyDataSetChanged()
            }
        }
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_lists)
        OPEN_UI_TYPE = intent.getIntExtra(OPEN_UI_NAME, OPEN_UI_TYPE)
        initTitle()
        setBackBtnVisible(true)
        initView()
    }

    private fun initTitle() {
        when (OPEN_UI_TYPE) {
            TYPE_VIDEO_CLIP->  title = "选择要剪辑的视频"
            TYPE_VIDEO_WATERMARK ->title = "选择添加水印的视频"
            TYPE_VIDEO_EDITOR ->title = "选择要编辑的视频"
            TYPE_VIDEO_MERGE ->title =  "选择要合并的视频(多选)"
        }

    }

    private fun initView() {
        recyclerView.setLayoutManager(StaggeredGridLayoutManager(2, StaggeredGridLayoutManager.VERTICAL));
        recyclerView.addItemDecoration(SpacingDecoration(this, 10f, 10f, true));
        mVideoAdapter = VideoListsAdapter(mVideoEntity)
        recyclerView.adapter = mVideoAdapter;
        mVideoAdapter?.setOnItemClickListener(this)
        next
        getVideoData()
    }

    /**
     * 子线程加载设备中的视频数据
     */
    private fun getVideoData() {
        Thread {
            loadVideoList()
        }.start()
    }

    private fun loadVideoList() {
        val contentResolver = contentResolver
        val projection =
            arrayOf(MediaStore.Video.Media._ID, MediaStore.Video.Media.DISPLAY_NAME, MediaStore.Video.Media.DATA)
        val orderBy = MediaStore.Video.Media.DISPLAY_NAME
        val uri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        val cursor = contentResolver.query(uri, projection, null, null, orderBy)
        if (cursor != null && cursor.count > 0) {
            cursor.moveToFirst()
        } else {
            return
        }
        val fileNum = cursor.count
        for (count in 0 until fileNum) {
            val videoPath = cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DATA))
            val file = File(videoPath)

            if (!file.exists()) {
                cursor.moveToNext()
                continue
            }
            val video = VideoEntity()
            video.id = (cursor.getInt(cursor.getColumnIndex(MediaStore.Video.Media._ID)))
            try {
                video.videoName = (cursor.getString(cursor.getColumnIndex(MediaStore.Video.Media.DISPLAY_NAME)))
            } catch (error: Exception) {
                Log.e(TAG, error.message)
            }

            video.videoPath = (videoPath)

            val videoPathLowerCase = videoPath.toLowerCase()
            if (!videoPathLowerCase.endsWith("mp4") && !videoPathLowerCase.endsWith("3gp") && !videoPathLowerCase.endsWith(
                    "mkv"
                ) && !videoPathLowerCase.endsWith("avi")
            ) {
                cursor.moveToNext()
                continue
            }
            var duration: Long = 0
            try {
                if (duration == 0L) {
                    duration = getVideoDurationFromMediaMetadata(video.videoPath)
                }
                if (duration > 0) {
                    video.videoDuration = (duration)
                    video.videoSize = (cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)))
                    mVideoEntity.add(video)
                }
            } catch (e: Exception) {
                Log.e("error:", e.message)
                mVideoEntity.add(video)
            }
            cursor.moveToNext()
        }
        cursor.close()
        mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED)
    }

    private fun getVideoDurationFromMediaMetadata(path: String): Long {
        var duration: Long = 0

        if (!TextUtils.isEmpty(path)) {
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(path)
                duration = java.lang.Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
            } catch (e: Exception) {
                e.printStackTrace()
            }

        }
        return duration
    }

    override fun onDestroy() {
        super.onDestroy()
        mHandler.removeMessages(MSG_NOTIFY_DATA_CHANGED)
    }


    fun next(view: View) {
        //限制
        when (OPEN_UI_TYPE) {
            TYPE_VIDEO_CLIP,
            TYPE_VIDEO_WATERMARK,
            TYPE_VIDEO_EDITOR -> {
                if (mSelectVideoList.size == 0) {
                    showMessage("至少选择一个文件")
                    return
                }
            }
            TYPE_VIDEO_MERGE -> {
                if (mSelectVideoList.size <= 1) {
                    showMessage("至少选择两个文件")
                    return
                }
            }
        }

        var bundle = Bundle()
        bundle.putParcelableArrayList(VIDEO_ENTITY, mSelectVideoList)
        when (OPEN_UI_TYPE) {
            TYPE_VIDEO_CLIP -> openUI(this, ClipActivity::class.java, bundle)
            TYPE_VIDEO_MERGE -> openUI(this, MergeActivity::class.java, bundle)
            TYPE_VIDEO_WATERMARK -> openUI(this, WatermarkActivity::class.java, bundle)
            TYPE_VIDEO_EDITOR -> openUI(this, EditorActivity::class.java, bundle)
        }
    }

    override fun onItemClick(position: Int, videoEntity: VideoEntity) {
        //去重
        if (mSelectVideoList.contains(videoEntity)) {
            mSelectVideoList.remove(videoEntity)
            //update 选中状态
            updateSelect(position)
            return
        }

        //限制
        when (OPEN_UI_TYPE) {
            TYPE_VIDEO_CLIP,
            TYPE_VIDEO_WATERMARK,
            TYPE_VIDEO_EDITOR -> {
                if (mSelectVideoList.size >= 1) {
                    showMessage("最多只能选择一个文件。")
                    return
                }
            }
        }

        //update 选中状态
        updateSelect(position)

        //添加
        mSelectVideoList.add(videoEntity)


    }

    private fun updateSelect(position: Int) {
        mVideoAdapter?.data?.get(position)?.let { videoEntity ->
            videoEntity.isSelect = !videoEntity.isSelect
            mVideoAdapter?.notifyItemChanged(position)
        }
    }
}