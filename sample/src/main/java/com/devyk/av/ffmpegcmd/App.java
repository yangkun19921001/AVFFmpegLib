package com.devyk.av.ffmpegcmd;

import android.app.Application;
import android.os.Environment;

import com.devyk.av.ffmpegcmd.utils.FileUtils;

import java.io.File;

/**
 * <pre>
 *     author  : devyk on 2020-10-02 13:30
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is App
 * </pre>
 */
public class App extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        if (!new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "aveditor").exists()) {
            new File(Environment.getExternalStorageDirectory().getPath() + File.separator + "aveditor").mkdirs();
        }
        FileUtils.INSTANCE.copyFilesFassets(getApplicationContext(),"ttf", Environment.getExternalStorageDirectory().getPath()+ File.separator + "aveditor");
    }
}
