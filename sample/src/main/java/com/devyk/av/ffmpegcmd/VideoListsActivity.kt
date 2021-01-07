package com.devyk.av.ffmpegcmd

import android.annotation.SuppressLint
import android.database.Cursor
import android.media.MediaMetadataRetriever
import android.os.Bundle
import android.os.Handler
import android.os.Message
import android.provider.MediaStore
import android.provider.MediaStore.Video
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.devyk.av.ffmpegcmd.adapter.VideoListsAdapter
import com.devyk.av.ffmpegcmd.utils.SpacingDecoration
import com.devyk.ffmpeglib.entity.VideoEntity
import com.devyk.ffmpeglib.entity.VideoInfo
import kotlinx.android.synthetic.main.activity_video_lists.*
import java.io.File
import java.util.concurrent.CopyOnWriteArrayList


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
    private var mVideoEntity: CopyOnWriteArrayList<VideoEntity> =
        CopyOnWriteArrayList<VideoEntity>()

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
//                mVideoAdapter?.setList(mVideoEntity)
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
            TYPE_VIDEO_CLIP -> title = "选择要剪辑的视频"
            TYPE_VIDEO_WATERMARK -> title = "选择添加水印的视频"
            TYPE_VIDEO_EDITOR -> title = "选择要编辑的视频"
            TYPE_VIDEO_MERGE -> title = "选择要合并的视频(多选)"
        }

    }

    private fun initView() {
        recyclerView.setLayoutManager(
            StaggeredGridLayoutManager(
                2,
                StaggeredGridLayoutManager.VERTICAL
            )
        );
        recyclerView.addItemDecoration(SpacingDecoration(this, 10f, 10f, true));
        mVideoAdapter = VideoListsAdapter(mVideoEntity)
        recyclerView.adapter = mVideoAdapter;
        mVideoAdapter?.setOnItemClickListener(this)
        getVideoData()
    }

    /**
     * 加载设备中的视频数据
     */
    private fun getVideoData() {
        getVideos()
//        mVideoEntity.addAll()
        Log.d(TAG, "视频 size:${mVideoEntity.size}")
        mVideoAdapter?.notifyDataSetChanged()
//        mHandler.sendEmptyMessage(MSG_NOTIFY_DATA_CHANGED)
    }


    /**
     * 获取本机视频列表
     * @return
     */
    fun getVideos(): ArrayList<VideoEntity> {

        val videos = ArrayList<VideoEntity>()

        var c: Cursor? = null
        var path = ""
        var id = -1
        var name = ""
        var size = 0L;
        var duration = 0L


        // String[] mediaColumns = { "_id", "_data", "_display_name",
        // "_size", "date_modified", "duration", "resolution" };
        c = contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            null,
            null,
            null,
            MediaStore.Video.Media.DEFAULT_SORT_ORDER
        )
        while (c!!.moveToNext()) {
            try {
                c!!.getString(c!!.getColumnIndexOrThrow(MediaStore.Video.Media.DATA))?.let {
                    path = it
                }
                val file = File(path)

                if (!file.exists()) {
                    continue
                }
                c!!.getInt(c!!.getColumnIndexOrThrow(MediaStore.Video.Media._ID))?.let {
                    id = it
                }// 视频的id?

                try {
                    c!!.getString(c!!.getColumnIndexOrThrow(MediaStore.Video.Media.DISPLAY_NAME))
                        .let {
                            name = it
                        } // 视频名称
                } catch (err: java.lang.Exception) {
                    Log.e(TAG, err.message)
                }

                c!!.getLong(c!!.getColumnIndexOrThrow(MediaStore.Video.Media.SIZE)).let {
                    size = it
                }// 大小
//                c!!.getLong(c!!.getColumnIndexOrThrow(MediaStore.Video.Media.DURATION)).let {
//                    duration = it
//                }

                duration = getVideoDurationFromMediaMetadata(path)

                // 时长
                mVideoEntity.add(VideoEntity(id, name, path, duration, size, false))
            } catch (e: Exception) {
                e.printStackTrace()
                Log.e(TAG, e.toString())
            }
        }
        if (c != null) {
            c!!.close()
        }
        return videos
    }

    private fun getVideoDurationFromMediaMetadata(path: String): Long {
        var duration: Long = 0

        if (!TextUtils.isEmpty(path)) {
            val mmr = MediaMetadataRetriever()
            try {
                mmr.setDataSource(path)
                if (mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION) != null)
                    duration =
                        java.lang.Long.parseLong(mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION))
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