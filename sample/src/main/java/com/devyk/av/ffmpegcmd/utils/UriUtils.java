package com.devyk.av.ffmpegcmd.utils;

import android.annotation.SuppressLint;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.OpenableColumns;
import android.util.Log;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

/**
 * <pre>
 *     author  : devyk on 2020-09-28 21:12
 *     blog    : https://juejin.im/user/578259398ac2470061f3a3fb/posts
 *     github  : https://github.com/yangkun19921001
 *     mailbox : yang1001yk@gmail.com
 *     desc    : This is UriUtils
 * </pre>
 */
@SuppressLint("NewApi")
public class UriUtils {



	public static String getFilePathByUri(Context context, Uri uri) {

		return getFilePathForN(uri,context);
	}



	public static String getFilePathForN(Uri uri, Context context) {
		Uri returnUri = uri;
		Cursor returnCursor = context.getContentResolver().query(returnUri, null, null, null, null);
		/*
		 * Get the column indexes of the data in the Cursor,
		 *     * move to the first row in the Cursor, get the data,
		 *     * and display it.
		 * */
		int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
		int sizeIndex = returnCursor.getColumnIndex(OpenableColumns.SIZE);
		returnCursor.moveToFirst();
		String name = (returnCursor.getString(nameIndex));
		String size = (Long.toString(returnCursor.getLong(sizeIndex)));
		File file = new File(context.getFilesDir(), name);
		try {
			InputStream inputStream = context.getContentResolver().openInputStream(uri);
			FileOutputStream outputStream = new FileOutputStream(file);
			int read = 0;
			int maxBufferSize = 1 * 1024 * 1024;
			int bytesAvailable = inputStream.available();

			//int bufferSize = 1024;
			int bufferSize = Math.min(bytesAvailable, maxBufferSize);

			final byte[] buffers = new byte[bufferSize];
			while ((read = inputStream.read(buffers)) != -1) {
				outputStream.write(buffers, 0, read);
			}
			Log.e("File Size", "Size " + file.length());
			inputStream.close();
			outputStream.close();
			Log.e("File Path", "Path " + file.getPath());
			Log.e("File Size", "Size " + file.length());
		} catch (Exception e) {
			Log.e("Exception", e.getMessage());
		}
		return file.getPath();
	}

	}

