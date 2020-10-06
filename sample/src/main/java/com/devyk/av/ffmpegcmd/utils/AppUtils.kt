package com.devyk.av.ffmpegcmd.utils

import android.content.pm.PackageManager
import android.R.attr.versionName
import android.content.Context
import android.content.pm.PackageInfo



/**
 * <pre>
 *     author  : devyk on 2020-09-28 22:34
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is AppUtils
 * </pre>
 */
object AppUtils {


    /**
     * get App versionName
     * @param context
     * @return
     */
    fun getVersionName(context: Context): String {
        val packageManager = context.getPackageManager()
        val packageInfo: PackageInfo
        var versionName = ""
        try {
            packageInfo = packageManager.getPackageInfo(context.getPackageName(), 0)
            versionName = packageInfo.versionName
        } catch (e: PackageManager.NameNotFoundException) {
            e.printStackTrace()
        }

        return versionName
    }
}