package com.devyk.av.library

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log

import java.io.IOException
import java.util.ArrayList


/**
 * <pre>
 *     author  : devyk on 2020-09-28 20:30
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AVEditor 视频编辑
 * </pre>
 */
object AVEditor {

    private val DEFAULT_WIDTH = 720//默认输出宽度
    private val DEFAULT_HEIGHT = 1280//默认输出高度

    enum class Format {
        MP3, MP4
    }

    enum class PTS {
        VIDEO, AUDIO, ALL
    }

    /**
     * 处理单个视频
     *
     * @param epVideo      需要处理的视频
     * @param outputOption 输出选项配置
     */
    fun exec(epVideo: AVVideo, outputOption: OutputOption, onEditorListener: OnEditorListener) {
        var isFilter = false
        val epDraws = epVideo.epDraws
        //开始处理
        val cmd = AVCmdList()
        cmd.append("ffmpeg")
        cmd.append("-y")
        if (epVideo.videoClip) {
            cmd.append("-ss").append(epVideo.clipStart).append("-t").append(epVideo.clipDuration)
                .append("-accurate_seek")
        }
        cmd.append("-i").append(epVideo.videoPath)
        //添加图片或者动图
        if (epDraws.size > 0) {
            for (i in epDraws.indices) {
                if (epDraws[i].isAnimation) {
                    cmd.append("-ignore_loop")
                    cmd.append(0)
                }
                epDraws[i].picPath?.let { cmd.append("-i").append(it) }
            }
            cmd.append("-filter_complex")
            val filter_complex = StringBuilder()
            filter_complex.append("[0:v]")
                .append(if (epVideo.filters != null) epVideo.filters!!.toString() + "," else "")
                .append("scale=").append(if (outputOption.width_ == 0) "iw" else outputOption.width_).append(":")
                .append(if (outputOption.height_ == 0) "ih" else outputOption.height_)
                .append(if (outputOption.width_ == 0) "" else ",setdar=" + outputOption.getSar()).append("[outv0];")
            for (i in epDraws.indices) {
                filter_complex.append("[").append(i + 1).append(":0]").append(epDraws[i].picFilter).append("scale=")
                    .append(epDraws[i].picWidth).append(":")
                    .append(epDraws[i].picHeight).append("[outv").append(i + 1).append("];")
            }
            for (i in epDraws.indices) {
                if (i == 0) {
                    filter_complex.append("[outv").append(i).append("]").append("[outv").append(i + 1).append("]")
                } else {
                    filter_complex.append("[outo").append(i - 1).append("]").append("[outv").append(i + 1).append("]")
                }
                filter_complex.append("overlay=").append(epDraws[i].picX).append(":").append(epDraws[i].picY)
                    .append(epDraws[i].time)
                if (epDraws[i].isAnimation) {
                    filter_complex.append(":shortest=1")
                }
                if (i < epDraws.size - 1) {
                    filter_complex.append("[outo").append(i).append("];")
                }
            }
            cmd.append(filter_complex.toString())
            isFilter = true
        } else {
            val filter_complex = StringBuilder()
            if (epVideo.filters != null) {
                cmd.append("-filter_complex")
                filter_complex.append(epVideo.filters)
                isFilter = true
            }
            //设置输出分辨率
            if (outputOption.width_ != 0) {
                if (epVideo.filters != null) {
                    filter_complex.append(",scale=").append(outputOption.width_).append(":")
                        .append(outputOption.height_)
                        .append(",setdar=").append(outputOption.getSar())
                } else {
                    cmd.append("-filter_complex")
                    filter_complex.append("scale=").append(outputOption.width_).append(":").append(outputOption.height_)
                        .append(",setdar=").append(outputOption.getSar())
                    isFilter = true
                }
            }
            if (filter_complex.toString() != "") {
                cmd.append(filter_complex.toString())
            }
        }

        //输出选项
        cmd.append(outputOption.outputInfo.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
        if (!isFilter && outputOption.outputInfo.isEmpty()) {
            cmd.append("-vcodec")
            cmd.append("copy")
            cmd.append("-acodec")
            cmd.append("copy")
        } else {
            cmd.append("-preset")
            cmd.append("superfast")
        }
        cmd.append(outputOption.outPath)
        var duration = VideoUitls.getDuration(epVideo.videoPath)
        if (epVideo.videoClip) {
            val clipTime = ((epVideo.clipDuration - epVideo.clipStart) * 1000000).toLong()
            duration = if (clipTime < duration) clipTime else duration
        }
        //执行命令
        execCmd(cmd, duration, onEditorListener)
    }

    /**
     * 合并多个视频
     *
     * @param epVideos     需要合并的视频集合
     * @param outputOption 输出选项配置
     */
    fun merge(epVideos: List<AVVideo>, outputOption: OutputOption, onEditorListener: OnEditorListener) {
        //检测是否有无音轨视频
        var isNoAudioTrack = false
        for (epVideo in epVideos) {
            val mediaExtractor = MediaExtractor()
            try {
                mediaExtractor.setDataSource(epVideo.videoPath)
            } catch (e: IOException) {
                e.printStackTrace()
                return
            }

            val at = TrackUtils.selectAudioTrack(mediaExtractor)
            if (at == -1) {
                isNoAudioTrack = true
                mediaExtractor.release()
                break
            }
            mediaExtractor.release()
        }
        //设置默认宽高
        outputOption.width_ = if (outputOption.width_ == 0) DEFAULT_WIDTH else outputOption.width_
        outputOption.height_ = if (outputOption.height_ == 0) DEFAULT_HEIGHT else outputOption.height_
        //判断数量
        if (epVideos.size > 1) {
            val cmd = AVCmdList()
            cmd.append("ffmpeg")
            cmd.append("-y")
            //添加输入标示
            for (e in epVideos) {
                if (e.videoClip) {
                    cmd.append("-ss").append(e.clipStart).append("-t").append(e.clipDuration).append("-accurate_seek")
                }
                cmd.append("-i").append(e.videoPath)
            }
            for (e in epVideos) {
                val epDraws = e.epDraws
                if (epDraws.size > 0) {
                    for (ep in epDraws) {
                        if (ep.isAnimation) cmd.append("-ignore_loop").append(0)
                        ep.picPath?.let { cmd.append("-i").append(it) }
                    }
                }
            }
            //添加滤镜标识
            cmd.append("-filter_complex")
            val filter_complex = StringBuilder()
            for (i in epVideos.indices) {
                val filter = if (epVideos[i].filters == null) StringBuilder("") else epVideos[i].filters!!.append(",")
                filter_complex.append("[").append(i).append(":v]").append(filter).append("scale=")
                    .append(outputOption.width_).append(":").append(outputOption.height_)
                    .append(",setdar=").append(outputOption.getSar()).append("[outv").append(i).append("];")
            }
            //添加标记和处理宽高
            var drawNum = epVideos.size//图标计数器
            for (i in epVideos.indices) {
                for (j in 0 until epVideos[i].epDraws.size) {
                    filter_complex.append("[").append(drawNum++).append(":0]").append(epVideos[i].epDraws[j].picFilter)
                        .append("scale=")
                        .append(epVideos[i].epDraws[j].picWidth).append(":").append(
                            epVideos[i].epDraws[j]
                                .picHeight
                        ).append("[p").append(i).append("a").append(j).append("];")
                }
            }
            //添加图标操作
            for (i in epVideos.indices) {
                for (j in 0 until epVideos[i].epDraws.size) {
                    filter_complex.append("[outv").append(i).append("][p").append(i).append("a").append(j)
                        .append("]overlay=")
                        .append(epVideos[i].epDraws[j].picX).append(":")
                        .append(epVideos[i].epDraws[j].picY)
                        .append(epVideos[i].epDraws[j].time)
                    if (epVideos[i].epDraws[j].isAnimation) {
                        filter_complex.append(":shortest=1")
                    }
                    filter_complex.append("[outv").append(i).append("];")
                }
            }
            //开始合成视频
            for (i in epVideos.indices) {
                filter_complex.append("[outv").append(i).append("]")
            }
            filter_complex.append("concat=n=").append(epVideos.size).append(":v=1:a=0[outv]")
            //是否添加音轨
            if (!isNoAudioTrack) {
                filter_complex.append(";")
                for (i in epVideos.indices) {
                    filter_complex.append("[").append(i).append(":a]")
                }
                filter_complex.append("concat=n=").append(epVideos.size).append(":v=0:a=1[outa]")
            }
            if (filter_complex.toString() != "") {
                cmd.append(filter_complex.toString())
            }
            cmd.append("-map").append("[outv]")
            if (!isNoAudioTrack) {
                cmd.append("-map").append("[outa]")
            }
            cmd.append(outputOption.outputInfo.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray())
            cmd.append("-preset").append("superfast").append(outputOption.outPath)
            var duration: Long = 0
            for (ep in epVideos) {
                var d = VideoUitls.getDuration(ep.videoPath)
                if (ep.videoClip) {
                    val clipTime = ((ep.clipDuration - ep.clipStart) * 1000000).toLong()
                    d = if (clipTime < d) clipTime else d
                }
                if (d != 0L) {
                    duration += d
                } else {
                    break
                }
            }
            //执行命令
            execCmd(cmd, duration, onEditorListener)
        } else {
            throw RuntimeException("Need more than one video")
        }
    }

    /**
     * 无损合并多个视频
     *
     *
     * 注意：此方法要求视频格式非常严格，需要合并的视频必须分辨率相同，帧率和码率也得相同
     *
     * @param context          Context
     * @param epVideos         需要合并的视频的集合
     * @param outputOption     输出选项
     * @param onEditorListener 回调监听
     */
    fun mergeByLc(
        context: Context,
        epVideos: List<AVVideo>,
        outputOption: OutputOption,
        onEditorListener: OnEditorListener
    ) {
        val appDir = context.cacheDir.absolutePath + "/EpVideos/"
        val fileName = "ffmpeg_concat.txt"
        val videos = ArrayList<String>()
        for (e in epVideos) {
            videos.add(e.videoPath)
        }
        FileUtils.writeTxtToFile(videos, appDir, fileName)
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-f").append("concat").append("-safe")
            .append("0").append("-i").append(appDir + fileName)
            .append("-c").append("copy").append(outputOption.outPath)
        var duration: Long = 0
        for (ep in epVideos) {
            val d = VideoUitls.getDuration(ep.videoPath)
            if (d != 0L) {
                duration += d
            } else {
                break
            }
        }
        execCmd(cmd, duration, onEditorListener)
    }

    /**
     * 添加背景音乐
     *
     * @param videoin          视频文件
     * @param audioin          音频文件
     * @param output           输出路径
     * @param videoVolume      视频原声音音量(例:0.7为70%)
     * @param audioVolume      背景音乐音量(例:1.5为150%)
     * @param onEditorListener 回调监听
     */
    fun music(
        videoin: String,
        audioin: String,
        output: String,
        videoVolume: Float,
        audioVolume: Float,
        onEditorListener: OnEditorListener
    ) {
        val mediaExtractor = MediaExtractor()
        try {
            mediaExtractor.setDataSource(videoin)
        } catch (e: IOException) {
            e.printStackTrace()
            return
        }

        val at = TrackUtils.selectAudioTrack(mediaExtractor)
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-i").append(videoin)
        if (at == -1) {
            val vt = TrackUtils.selectVideoTrack(mediaExtractor)
            val duration = mediaExtractor.getTrackFormat(vt).getLong(MediaFormat.KEY_DURATION).toFloat() / 1000f / 1000f
            cmd.append("-ss").append("0").append("-t").append(duration).append("-i").append(audioin).append("-acodec")
                .append("copy").append("-vcodec").append("copy")
        } else {
            cmd.append("-i").append(audioin).append("-filter_complex")
                .append("[0:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=$videoVolume[a0];[1:a]aformat=sample_fmts=fltp:sample_rates=44100:channel_layouts=stereo,volume=$audioVolume[a1];[a0][a1]amix=inputs=2:duration=first[aout]")
                .append("-map").append("[aout]").append("-ac").append("2").append("-c:v")
                .append("copy").append("-map").append("0:v:0")
        }
        cmd.append(output)
        mediaExtractor.release()
        val d = VideoUitls.getDuration(videoin)
        execCmd(cmd, d, onEditorListener)
    }

    /**
     * 音视频分离
     *
     * @param videoin          视频文件
     * @param out              输出文件路径
     * @param format           输出类型
     * @param onEditorListener 回调监听
     */
    fun demuxer(videoin: String, out: String, format: Format, onEditorListener: OnEditorListener) {
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-i").append(videoin)
        when (format) {
            AVEditor.Format.MP3 -> cmd.append("-vn").append("-acodec").append("libmp3lame")
            AVEditor.Format.MP4 -> cmd.append("-vcodec").append("copy").append("-an")
        }
        cmd.append(out)
        val d = VideoUitls.getDuration(videoin)
        execCmd(cmd, d, onEditorListener)
    }

    /**
     * 音视频倒放
     *
     * @param videoin          视频文件
     * @param out              输出文件路径
     * @param vr               是否视频倒放
     * @param ar               是否音频倒放
     * @param onEditorListener 回调监听
     */
    fun reverse(videoin: String, out: String, vr: Boolean, ar: Boolean, onEditorListener: OnEditorListener) {
        if (!vr && !ar) {
            Log.e("ffmpeg", "parameter error")
            onEditorListener.onFailure()
            return
        }
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-i").append(videoin).append("-filter_complex")
        var filter = ""
        if (vr) {
            filter += "[0:v]reverse[v];"
        }
        if (ar) {
            filter += "[0:a]areverse[a];"
        }
        cmd.append(filter.substring(0, filter.length - 1))
        if (vr) {
            cmd.append("-map").append("[v]")
        }
        if (ar) {
            cmd.append("-map").append("[a]")
        }
        if (ar && !vr) {
            cmd.append("-acodec").append("libmp3lame")
        }
        cmd.append("-preset").append("superfast").append(out)
        val d = VideoUitls.getDuration(videoin)
        execCmd(cmd, d, onEditorListener)
    }

    /**
     * 音视频变速
     *
     * @param videoin          音视频文件
     * @param out              输出路径
     * @param times            倍率（调整范围0.25-4）
     * @param pts              加速类型
     * @param onEditorListener 回调接口
     */
    fun changePTS(videoin: String, out: String, times: Float, pts: PTS, onEditorListener: OnEditorListener) {
        if (times < 0.25f || times > 4.0f) {
            Log.e("ffmpeg", "times can only be 0.25 to 4")
            onEditorListener.onFailure()
            return
        }
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-i").append(videoin)
        var t = "atempo=$times"
        if (times < 0.5f) {
            t = "atempo=0.5,atempo=" + times / 0.5f
        } else if (times > 2.0f) {
            t = "atempo=2.0,atempo=" + times / 2.0f
        }
        Log.v("ffmpeg", "atempo:$t")
        when (pts) {
            AVEditor.PTS.VIDEO -> cmd.append("-filter_complex").append("[0:v]setpts=" + 1 / times + "*PTS").append("-an")
            AVEditor.PTS.AUDIO -> cmd.append("-filter:a").append(t)
            AVEditor.PTS.ALL -> cmd.append("-filter_complex").append("[0:v]setpts=" + 1 / times + "*PTS[v];[0:a]" + t + "[a]")
                .append("-map").append("[v]").append("-map").append("[a]")
        }
        cmd.append("-preset").append("superfast").append(out)
        val d = VideoUitls.getDuration(videoin)
        val dd = (d / times).toDouble()
        val ddd = dd.toLong()
        execCmd(cmd, ddd, onEditorListener)
    }

    /**
     * 视频转图片
     *
     * @param videoin            音视频文件
     * @param out                输出路径
     * @param w                    输出图片宽度
     * @param h                    输出图片高度
     * @param rate                每秒视频生成图片数
     * @param onEditorListener    回调接口
     */
    fun video2pic(videoin: String, out: String, w: Int, h: Int, rate: Float, onEditorListener: OnEditorListener) {
        if (w <= 0 || h <= 0) {
            Log.e("ffmpeg", "width and height must greater than 0")
            onEditorListener.onFailure()
            return
        }
        if (rate <= 0) {
            Log.e("ffmpeg", "rate must greater than 0")
            onEditorListener.onFailure()
            return
        }
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-i").append(videoin)
            .append("-r").append(rate).append("-s").append(w.toString() + "x" + h).append("-q:v").append(2)
            .append("-f").append("image2").append("-preset").append("superfast").append(out)
        val d = VideoUitls.getDuration(videoin)
        execCmd(cmd, d, onEditorListener)
    }

    /**
     * 图片转视频
     *
     * @param videoin            视频文件
     * @param out                输出路径
     * @param w                    输出视频宽度
     * @param h                    输出视频高度
     * @param rate                输出视频帧率
     * @param onEditorListener    回调接口
     */
    fun pic2video(videoin: String, out: String, w: Int, h: Int, rate: Float, onEditorListener: OnEditorListener) {
        if (w < 0 || h < 0) {
            Log.e("ffmpeg", "width and height must greater than 0")
            onEditorListener.onFailure()
            return
        }
        if (rate <= 0) {
            Log.e("ffmpeg", "rate must greater than 0")
            onEditorListener.onFailure()
            return
        }
        val cmd = AVCmdList()
        cmd.append("ffmpeg").append("-y").append("-f").append("image2").append("-i").append(videoin)
            .append("-vcodec").append("libx264")
            .append("-r").append(rate)
        //				.append("-b").append("10M");
        if (w > 0 && h > 0) {
            cmd.append("-s").append(w.toString() + "x" + h)
        }
        cmd.append(out)
        val d = VideoUitls.getDuration(videoin)
        execCmd(cmd, d, onEditorListener)
    }


    /**
     * 输出选项设置
     */
    class OutputOption(
        internal var outPath: String//输出路径
    ) {
        var frameRate = 0//帧率
        var bitRate = 0//比特率(一般设置10M)
        var outFormat = ""//输出格式(目前暂时只支持mp4,x264,mp3,gif)
        var width_ = 0//输出宽度
        var height_ = 0//输出高度
        private var sar = 6//输出宽高比

        /**
         * 获取输出信息
         *
         * @return 1
         */
        internal val outputInfo: String
            get() {
                val res = StringBuilder()
                if (frameRate != 0) {
                    res.append(" -r ").append(frameRate)
                }
                if (bitRate != 0) {
                    res.append(" -b ").append(bitRate).append("M")
                }
                if (!outFormat.isEmpty()) {
                    res.append(" -f ").append(outFormat)
                }
                return res.toString()
            }

        /**
         * 获取宽高比
         *
         * @return 1
         */
        fun getSar(): String {
            val res: String
            when (sar) {
                ONE_TO_ONE -> res = "1/1"
                FOUR_TO_THREE -> res = "4/3"
                THREE_TO_FOUR -> res = "3/4"
                SIXTEEN_TO_NINE -> res = "16/9"
                NINE_TO_SIXTEEN -> res = "9/16"
                else -> res = "$width_/$height_"
            }
            return res
        }

        fun setSar(sar: Int) {
            this.sar = sar
        }

        /**
         * 设置宽度
         *
         * @param width 宽
         */
        fun setWidth(width: Int) {
            var width = width
            if (width % 2 != 0) width -= 1
            this.width_ = width
        }

        /**
         * 设置高度
         *
         * @param height 高
         */
        fun setHeight(height: Int) {
            var height = height
            if (height % 2 != 0) height -= 1
            this.height_ = height
        }

        companion object {
            internal val ONE_TO_ONE = 1// 1:1
            internal val FOUR_TO_THREE = 2// 4:3
            internal val SIXTEEN_TO_NINE = 3// 16:9
            internal val NINE_TO_SIXTEEN = 4// 9:16
            internal val THREE_TO_FOUR = 5// 3:4
        }
    }

    /**
     * 开始处理
     *
     * @param cmd              命令
     * @param duration         视频时长（单位微秒）
     * @param onEditorListener 回调接口
     */
    fun execCmd(cmd: String, duration: Long, onEditorListener: OnEditorListener) {
        var cmd = cmd
        cmd = "ffmpeg $cmd"
        val cmds = cmd.split(" ".toRegex()).dropLastWhile { it.isEmpty() }.toTypedArray()
        FFmpegCmd.exec(cmds, duration, object : OnEditorListener {
            override fun onSuccess() {
                onEditorListener.onSuccess()
            }

            override fun onFailure() {
                onEditorListener.onFailure()
            }

            override fun onProgress(progress: Float) {
                onEditorListener.onProgress(progress)
            }
        })
    }

    /**
     * 开始处理
     *
     * @param cmd              命令
     * @param duration         视频时长（单位微秒）
     * @param onEditorListener 回调接口
     */
    private fun execCmd(cmd: AVCmdList, duration: Long, onEditorListener: OnEditorListener) {
        val cmds = cmd.toTypedArray()
        var cmdLog = ""
        for (ss in cmds) {
            cmdLog += cmds
        }
        Log.v("AVMediaF", "cmd:$cmdLog")
        FFmpegCmd.exec(cmds, duration, object : OnEditorListener {
            override fun onSuccess() {
                onEditorListener.onSuccess()
            }

            override fun onFailure() {
                onEditorListener.onFailure()
            }

            override fun onProgress(progress: Float) {
                onEditorListener.onProgress(progress)
            }
        })
    }
}
