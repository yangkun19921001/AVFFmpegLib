package com.devyk.av.library;

/**
 * <pre>
 *     author  : devyk on 2020-09-28 20:12
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is FFmpegCmd ffmpeg 命令处理
 * </pre>
 */
public class FFmpegCmd {
	/**
	 * 加载所有相关链接库
	 */
	static {
		System.loadLibrary("ffmpeg-tools");
	}

	private static OnEditorListener listener;
	private static long duration;

	/**
	 * 调用底层执行
	 *
	 * @param argc
	 * @param argv
	 * @return
	 */
	public static native int exec(int argc, String[] argv);

	public static native int exit();

	public static void onExecuted(int ret) {
		if (listener != null) {
			if (ret == 0) {
				listener.onProgress(1);
				listener.onSuccess();
				listener = null;
			} else {
				listener.onFailure();
				listener = null;
			}
		}
	}

	public static void onProgress(float progress) {
		if (listener != null) {
			if (duration != 0) {
				listener.onProgress(progress / (duration / 1000000) * 0.95f);
			}
		}
	}


	/**
	 * 执行ffmoeg命令
	 *
	 * @param cmds
	 * @param listener
	 */
	public static void exec(String[] cmds, long duration, OnEditorListener listener) {
		FFmpegCmd.listener = listener;
		FFmpegCmd.duration = duration;
		exec(cmds.length, cmds);
	}
}
