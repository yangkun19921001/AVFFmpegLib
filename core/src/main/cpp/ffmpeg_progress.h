//
// Created by YANGJIE8 on 2020/5/22.
//

#include <jni.h>

int register_progress_call(JNIEnv *pInterface, jclass pVoid);
void callJavaMethodProgress(JNIEnv *env, jclass clazz, float ret);
/**
 * 进度回调
 * @param pro
 * @return
 */
int ffmpeg_progress(double pro);