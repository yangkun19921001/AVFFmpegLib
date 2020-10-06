# 视音频编辑器

## 前言

有时候我们想对音视频进行加工处理，比如视频编辑、添加字幕、裁剪等功能处理，虽然 Github 上开源了一些比较不错的项目，但是如果我们想在此项目上进行二次开发，比如我想拿到该项目的动态库基于 OpenH264 来进行对 YUV 编码，这个时候有可能该动态库没有集成 OpenH64 库，所以为了扩展性，我就自己弄了一套万能的库，基本上包含了所有常用的音视频处理库，你不用再去进行编译。编译完成的头文件和动态库可以在该项目的 core/cpp 目录自行获取(**已完全开源**)。

##介绍

**视音频编辑器** 主要移至  **FFmpeg v4.4-dev + libx264 + freetype + fontconfig + fribidi + openh264 +libfdk-aac + gnutls + speex + libwebp + lame +opus + opencore-amr + https **等库编译的适用于 Android 平台的音视频编辑、视频剪辑的快速处理框架，目前内置了音视频剪辑、编辑、多个视频文件合并、字幕、水印、倒放等功能，也可以传入 **FFmpeg** 命令来进行处理。

**我们先来看一下内置功能的效果:**

**视频剪辑:**

![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20201006154236.gif)

**视频合并:**

![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20201006154858.gif)

**视频编辑:**

![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20201006155506.gif)



## 如何使用

###1. 添加依赖

```shell
implementation 'com.devyk.ffmpeglib:AVFFmpegCore:1.0.1'
```

###2. 功能 API 介绍

**回调处理**

```kotlin
public interface ExecuteCallback {
		/**
		*开始处理
		*/
    void onStart(Long executionId);
    /**
     * 如果外部传递了当前操作视频的时长，那么返回的是百分比进度，反之返回的是操作视频对应的微妙时长
     *
     * @param v
     */
    void onProgress(float v);
		/**
		*处理成功
		*/
    void onSuccess(long executionId);
		/**
		*处理失败
		*/
    void onFailure(long executionId, String error);
		/**
		*取消处理
		*/
    void onCancel(long executionId);
		/**
		* ffmpeg 执行的 log
		*/
    void onFFmpegExecutionMessage(LogMessage logMessage);
}
```



**AVEditor**

- 剪辑

  ```kotlin
  AVVideo:
  //start:开始的时间 单位秒
  //duration: 剪辑多少秒
  fun clip(start: Float, duration: Float)
  
  AVEditor:
  fun exec(
    epVideo: AVVideo, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

- 旋转

  ```kotlin
  AVVideo:
  //rotation:旋转角度(仅支持90,180,270度旋转)
  //isFlip:是否镜像
  fun rotation(rotation: Int, isFlip: Boolean)
  AVEditor:
  fun exec(
    epVideo: AVVideo, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

  

- 裁剪

  ```kotlin
  AVVideo:
  //width: 裁剪的宽
  //height: 裁剪的高
  //x: 从 x 点开始
  //y: 从 y 点开始
  fun crop(
    width: Float, 
    height: Float, 
    x: Float, y: Float)
  AVEditor:
  fun exec(
    epVideo: AVVideo, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

  

- 添加文字水印

  ```kotlin
  AVVideo:
  fun addText(avText: AVText)
  AVEditor:
  fun exec(
    epVideo: AVVideo, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

  

- 添加图片水印

  ```kotlin
  AVVideo:
  fun addDraw(epDraw: AVDraw)
  AVEditor:
  fun exec(
    epVideo: AVVideo, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

  

- 视频合并

  ```kotlin
  AVEditor:
  fun merge(
    epVideos: List<AVVideo>, 
    outputOption: OutputOption, 
    executeCallback: ExecuteCallback)
  ```

  

- 添加背景音乐

  ```kotlin
  AVEditor:
  music(
    			videoin: String,
          audioin: String,
          output: String,
          videoVolume: Float,
          audioVolume: Float,
          executeCallback: ExecuteCallback
      ) 
  ```

  

- 音视频分离

  ```kotlin
  AVEditor:
  fun demuxer(
    inSource: String, outSource: String, 
    format: Format, 
    executeCallback: ExecuteCallback)
  ```

  

- 视频倒放

  ```kotlin
  AVEditor:
  fun reverse(
    videoin: String, out: String, 
    vr: Boolean,//视频是否倒放
    ar: Boolean, //音频是否倒放
    executeCallback: ExecuteCallback)
  ```

  

- 视频转图片

  ```kotlin
  AVEditor:
  fun video2pic(
    videoin: String, //视频输入文件
    out: String,  //图片输出路径-目录
    w: Int, h: Int, //输出图片的宽高
    rate: Float, //每秒视频生成图片数
    executeCallback: ExecuteCallback)
  ```

  

- 视频转 Gif

  ```kotlin
  AVEditor:
  fun video2Gif(
          videoin: String,
          gifOut: String,
          startDuration: Int,
          stopDuration: Int,
          executeCallback: ExecuteCallback
      )
  ```

- 自定义命令

  ```kotlin
  AVEditor:
  //cmd：FFmpeg 命令
  //duration: 处理视频的时长，可以通过 VideoUitls.getDuration(videoPath) 来获取
  fun execCmd(cmd: String, duration: Long, executeCallback: ExecuteCallback) 
  ```

  

## FFmpeg 编译小技巧

有时候我们发现 Github 上一些基于 FFmpeg 开发的比较好的项目，比如 [ijkplayer](https://github.com/bilibili/ijkplayer) ，[RxFFmpeg](https://github.com/microshow/RxFFmpeg) 等，我们想基于它做二次开发，由于我们不知道怎么编译，也不知道编译 FFmpeg 到底需要开启哪些节点，这个时候我就想拿某些项目的编译脚本，基于它来进行二次编译。一般来说有些项目不会开源编译 FFmpeg 的脚本。这个时候我们可以通过拿到开源项目的静态或者动态库，这里我就以 [RxFFmpeg](https://github.com/microshow/RxFFmpeg)  来举例，可以看看我是如果拿到它的编译脚本.

1、先 clone  [RxFFmpeg](https://github.com/microshow/RxFFmpeg) 

```shell
git clone https://github.com/microshow/RxFFmpeg.git
```

2、关联 librxffmpeg-core.so

通过该 so  我们知道它应该就是 FFmpeg 编译之后的动态库，现在我们通过 cmake 的方式关联到该 so 

```cmake
cmake_minimum_required(VERSION 3.4.1)
#JNI 路径
set(FFMpeg_include_PATH ${CMAKE_SOURCE_DIR})
include_directories(${FFMpeg_include_PATH}/include/)
add_library(RxFFmpeg SHARED IMPORTED)
set_target_properties(RxFFmpeg PROPERTIES IMPORTED_LOCATION ${PROJECT_SOURCE_DIR}/../../../libs/${CMAKE_ANDROID_ARCH_ABI}/librxffmpeg-core.so)
find_library(
        log-lib
        log)
FILE(GLOB JNI_ALL_C ${JNI_PATH}/*.cpp)
add_library(
        ffmpeg-tools
        SHARED
        ${JNI_ALL_C}
)
target_link_libraries(
        ffmpeg-tools
        RxFFmpeg
${log-lib}
)
```

3、编写 JNI 函数，拿到编译脚本

```c++
//
// Created by DevYK on 2020-10-02.
//
#include <android/log.h>
extern "C"
{
#include "libavutil/avutil.h"
}
#include <jni.h>
#define  AV_TAG   "AVLOG"
#define LOGE(format, ...)  __android_log_print(ANDROID_LOG_ERROR, AV_TAG, format, ##__VA_ARGS__)
int JNI_OnLoad(JavaVM *javaVM, void *pVoid) {
    const char *config = avutil_configuration();
    LOGE("FFMPEG VERSION%s \n", av_version_info());
    LOGE("FFMPEG configuration %s \n", avutil_configuration());
    return JNI_VERSION_1_6;
}

```

通过 debug 查看 config 指针指向内存中的信息如下:
![](https://devyk.oss-cn-qingdao.aliyuncs.com/blog/20201006175319.png)

嗯，拿到了它的编译信息，然后我们就可以基于它来完善我们项目的编译，我们可以编译出比它的功能更加丰富，就如开头介绍一般，我添加了市面上常用的一些 C++ 库，基本达到了万能了吧。



## 总结

项目地址：[AVFFmpegLib](https://github.com/yangkun19921001/AVFFmpegLib)

这里就不在介绍如何编译了，感兴趣的可以看 [mobile-ffmpeg](https://github.com/tanersener/mobile-ffmpeg) 项目，我这里也是基于它进行二次封装开发。

##参考
- [EpMedia](https://github.com/yangjie10930/EpMedia)
- [FFmpeg 常用命令](https://blog.csdn.net/kingvon_liwei/article/details/79271361)
- [mobile-ffmpeg](https://github.com/tanersener/mobile-ffmpeg)
- [Android 音视频编辑经验总结及开源工程分享](https://blog.csdn.net/u011495684/article/details/78437060)