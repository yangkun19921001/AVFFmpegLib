package com.devyk.av.library

import android.annotation.TargetApi
import android.media.MediaExtractor
import android.media.MediaFormat
import android.media.MediaMetadataRetriever
import android.os.Build

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:32
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoUitls
 * </pre>
 */
object VideoUitls {

    /**
     * 获取视频信息
     *
     * @param url
     * @return 视频时长（单位微秒）
     */
    fun getDuration(url: String): Long {
        try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(url)
            var videoExt = TrackUtils.selectVideoTrack(mediaExtractor)
            if (videoExt == -1) {
                videoExt = TrackUtils.selectAudioTrack(mediaExtractor)
                if (videoExt == -1) {
                    return 0
                }
            }
            val mediaFormat = mediaExtractor.getTrackFormat(videoExt)
            val res =
                if (mediaFormat.containsKey(MediaFormat.KEY_DURATION)) mediaFormat.getLong(MediaFormat.KEY_DURATION) else 0//时长
            mediaExtractor.release()
            return res
        } catch (e: Exception) {
            return 0
        }

    }

    /**
     * 获取音轨数量
     *
     * @param url
     * @return
     */
    fun getChannelCount(url: String): Int {
        try {
            val mediaExtractor = MediaExtractor()
            mediaExtractor.setDataSource(url)
            val audioExt = TrackUtils.selectAudioTrack(mediaExtractor)
            if (audioExt == -1) {
                return 0
            }
            val mediaFormat = mediaExtractor.getTrackFormat(audioExt)
            val channel =
                if (mediaFormat.containsKey(MediaFormat.KEY_CHANNEL_COUNT)) mediaFormat.getInteger(MediaFormat.KEY_CHANNEL_COUNT) else 1
            mediaExtractor.release()
            return channel
        } catch (e: Exception) {
            return 0
        }

    }


    /**
     * 获取源数据的宽高
     */
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    fun getVideoInfo(url: String): VideoInfo {
        var metadataRetriever = MediaMetadataRetriever();
        metadataRetriever.setDataSource(url);
        // 获得时长
        val duration = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_DURATION)
        // 获得名称
        val keyTitle = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_TITLE)
        // 获得媒体类型
        val mimetype = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_MIMETYPE)
        // 获得码率
        val bitrate = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_BITRATE)
        //获取视频宽
        val videoWidth = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_WIDTH)
        //获取视频高
        val videoHeight = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_HEIGHT)
        //帧率
        val fps = findMetadata(metadataRetriever, MediaMetadataRetriever.METADATA_KEY_VIDEO_FRAME_COUNT)
        return VideoInfo(duration, keyTitle, mimetype, bitrate, fps,videoWidth, videoHeight)
    }

    private fun findMetadata(metadataRetriever: MediaMetadataRetriever?, type: Int) =
        metadataRetriever?.extractMetadata(type)
}
