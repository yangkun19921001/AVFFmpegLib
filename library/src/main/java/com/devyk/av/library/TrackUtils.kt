package com.devyk.av.library

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:45
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is TrackUtils
 * </pre>
 */
object TrackUtils {

    private val TAG = "TrackUtils"

    /**
     * 查找视频轨道
     * @param extractor
     * @return
     */
    fun selectVideoTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("video/")) {
                Log.d(TAG, "Extractor selected track $i ($mime): $format")
                return i
            }
        }
        return -1
    }

    /**
     * 查找音频轨道
     * @param extractor
     * @return
     */
    fun selectAudioTrack(extractor: MediaExtractor): Int {
        val numTracks = extractor.trackCount
        for (i in 0 until numTracks) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME)
            if (mime!!.startsWith("audio/")) {
                Log.d(TAG, "Extractor selected track $i ($mime): $format")
                return i
            }
        }
        return -1
    }
}
