package com.berec.speedtest;

import android.content.Context;
import android.net.Uri;

import java.io.File;

import static com.berec.speedtest.ApiConfig.Bits;
import static com.berec.speedtest.ApiConfig.MB;
import static com.berec.speedtest.ApiConfig.SECONDS;

public class FileCache {
    File cacheDir;
    private Context context;

    public FileCache(Context context) {
        this.context = context;
        // Find the dir to save files to internal storage
        cacheDir = context.getFilesDir();
        if (!cacheDir.exists())
            cacheDir.mkdirs();
    }

    public static String fileName(String url){

        String filename = Uri.parse(url).getLastPathSegment();

        return filename;
    }


    public void clear() {
        String[] files = context.fileList();
        if (files == null)
            return;
        for (String f : files)
            context.deleteFile(f);
    }

    public static double getCurrentProgress(double transferred, long startTime, long endTime) {
        // time taken in miliseconds
        double timeTakenMills = Math.floor(endTime - startTime);
        // time taken in seconds
        double timeTakenSecs = timeTakenMills / SECONDS;
        // file size taken in Mbits
        transferred = (double) (transferred * Bits / MB);

        return  timeTakenSecs > 0 ? (double) (transferred / timeTakenSecs) : 0;
    }
}
