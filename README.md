
## 视频编辑器
这是一个通过移至 FFmpeg 命令模式的一个版本，主要基于 ffmpeg 命令对音视频做一些加工处理。
编译环境 FFmpeg4.2.3 + fdk-aac-0.1.6 + libx264 + OpenSSL_1_1_1g + NDK21 clang ，想进行二次编辑开发可以参考当前项目的 ./shell 脚本




##编译遇见的问题
- ERROR: openssl not found（修改后继续编辑configure文件大约 line 6353处（修改检查openssl逻辑，enable_openssl））
 ```
 check_pkg_config openssl openssl openssl/ssl.h OPENSSL_init_ssl ||
 # 添加此处
 check_lib openssl openssl/ssl.h OPENSSL_init_ssl -lssl -lcrypto ||
 # 添加完成
 check_pkg_config openssl openssl openssl/ssl.h SSL_library_init ||

 ```

##参考
- [EpMedia](https://github.com/yangjie10930/EpMedia)
- [FFmpeg 常用命令](https://blog.csdn.net/kingvon_liwei/article/details/79271361)
- [Android 获取视频缩略图(获取视频每帧数据)的优化方案](https://cloud.tencent.com/developer/article/1388510)
  - 原理是通过 MediaCodec 进行解码为 YUV 数据，在规定的时间间隔进行将 YUV - > Bitmap 的一个过程。为了先演示效果，先暂时用这个库，后面会单独作为一个功能模块来输出。