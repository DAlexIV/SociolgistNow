package com.hse.dalexiv.vksignintest.downloader;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Environment;

import java.io.File;

/**
 * Created by dalex on 11/19/2015.
 */
public class LoadImage {
    public static Bitmap getBitmapFromFile(String name, Context context) {
        File sd = context.getExternalCacheDir();
        File image = new File(sd, name);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
    }
}
