package com.devyk.av.ffmpegcmd.adapter

import com.bumptech.glide.Glide
import com.chad.library.adapter.base.BaseQuickAdapter
import com.chad.library.adapter.base.viewholder.BaseViewHolder
import com.devyk.av.ffmpegcmd.R
import com.devyk.av.ffmpegcmd.utils.TimeUtil
import com.devyk.ffmpeglib.entity.VideoEntity
import com.devyk.ffmpeglib.util.VideoUitls

/**
 * <pre>
 *     author  : devyk on 2020-09-29 15:26
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoListsAdapter
 * </pre>
 */
public class VideoListsAdapter : BaseQuickAdapter<VideoEntity, BaseViewHolder> {

    constructor(data: MutableList<VideoEntity>?) : super(R.layout.adapter_video_lists, data)

    private var mOnItemClickListener: OnItemClickListener? = null

    override fun convert(holder: BaseViewHolder, item: VideoEntity) {
        holder.setText(R.id.tv_duration, TimeUtil.format(item.videoDuration))
            .setVisible(R.id.iv_select, item.isSelect)
            .setText(R.id.tv_size,VideoUitls.getSize(item.videoSize))
        Glide.with(context).load(item.videoPath).into(holder.getView(R.id.iv))
        holder.itemView.setOnClickListener {
            mOnItemClickListener?.onItemClick(holder.adapterPosition, item)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(position: Int, videoEntity: VideoEntity)
    }

    fun setOnItemClickListener(listener: OnItemClickListener) {
        mOnItemClickListener = listener
    }

}