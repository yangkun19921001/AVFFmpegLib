package com.devyk.av.library

/**
 * 执行完成/错误 时的回调接口
 */
interface OnEditorListener {
    fun onSuccess()

    fun onFailure()

    fun onProgress(progress: Float)
}
