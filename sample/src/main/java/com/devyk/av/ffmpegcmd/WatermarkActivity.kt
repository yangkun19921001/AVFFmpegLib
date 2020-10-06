package com.devyk.av.ffmpegcmd

import android.os.Bundle

/**
 * <pre>
 *     author  : devyk on 2020-09-28 22:11
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is WatermarkActivity
 * </pre>
 */

class WatermarkActivity :BaseActivity(){
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_watemark)

        title = "添加水印"
        setBackBtnVisible(true)
    }
}