package com.devyk.av.library

import android.util.Log

import java.io.File
import java.io.RandomAccessFile


/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:14
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FileUtils
 * </pre>
 */
object FileUtils {

    /**
     * 此类用于生成合并视频所需要的文档
     * @param strcontent 视频路径集合
     * @param filePath 生成的地址
     * @param fileName 生成的文件名
     */
    fun writeTxtToFile(strcontent: List<String>, filePath: String, fileName: String) {
        //生成文件夹之后，再生成文件，不然会出错
        makeFilePath(filePath, fileName)
        val strFilePath = filePath + fileName
        // 每次写入时，都换行写
        var strContent = ""
        for (i in strcontent.indices) {
            strContent += "file " + strcontent[i] + "\r\n"
        }
        try {
            val file = File(strFilePath)
            //检查文件是否存在，存在则删除
            if (file.isFile && file.exists()) {
                file.delete()
            }
            file.parentFile!!.mkdirs()
            file.createNewFile()
            val raf = RandomAccessFile(file, "rwd")
            raf.seek(file.length())
            raf.write(strContent.toByteArray())
            raf.close()
            Log.e("TestFile", "写入成功:$strFilePath")
        } catch (e: Exception) {
            Log.e("TestFile", "Error on write File:$e")
        }

    }

    //创建路径
    fun makeFilePath(filePath: String, fileName: String): File? {
        var file: File? = null
        makeRootDirectory(filePath)
        try {
            file = File(filePath + fileName)
            if (!file.exists()) {
                file.createNewFile()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return file
    }

    //创建文件夹
    fun makeRootDirectory(filePath: String) {
        var file: File? = null
        try {
            file = File(filePath)
            if (!file.exists()) {
                file.mkdir()
            }
        } catch (e: Exception) {
            Log.i("error:", e.toString() + "")
        }

    }
}
