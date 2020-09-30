package com.devyk.av.library

/**
 * <pre>
 *     author  : devyk on 2020-09-30 14:48
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is VideoInfo
 * </pre>
 */
data class VideoInfo(
    val duration: String?,
    val keyTitle: String?,
    val mimetype: String?,
    val bitrate: String? ="1000000",
    val fps: String? ="20",
    val videoWidth: String? = "1280",
    val videoHeight: String? = "720"
)