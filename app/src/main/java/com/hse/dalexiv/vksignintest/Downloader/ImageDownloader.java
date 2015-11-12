package com.hse.dalexiv.vksignintest.downloader;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by dalex on 11/12/2015.
 */
public abstract class ImageDownloader extends AsyncTask<String[], Integer, Uri> {
    private final String TAG = this.getClass().getSimpleName();
    private WeakReference<Activity> mContext;
    private final int MY_REQUEST_CODE = 777;

    public ImageDownloader(Activity activity) {
        mContext = new WeakReference<Activity>(activity);
    }

    @Override
    protected Uri doInBackground(String[]... params) {
        int count;
        try {

            if (ContextCompat.checkSelfPermission(mContext.get(),
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED
                    || ContextCompat.checkSelfPermission(mContext.get(),
                    android.Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(mContext.get(),
                        android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                        || ActivityCompat.shouldShowRequestPermissionRationale(mContext.get(),
                        android.Manifest.permission.READ_EXTERNAL_STORAGE))
                    ; // We are not suppose to request something
                else
                    ActivityCompat.requestPermissions(mContext.get(),
                            new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
            }

            String urlText = params[0][0];
            String filename = params[0][1];

            URL url = new URL(urlText);
            URLConnection conection = url.openConnection();
            conection.connect();
            // getting file length
            int lenghtOfFile = conection.getContentLength();

            // input stream to read file - with 8k buffer
            InputStream input = new BufferedInputStream(url.openStream(), 8192);

            File myOnlyFile = new File(Environment.getDownloadCacheDirectory(), "temp.jpg");

            // Output stream to write file
            OutputStream output = new FileOutputStream(Environment.getDownloadCacheDirectory() + "/"
                    + filename);

            byte data[] = new byte[1024];

            long total = 0;

            while ((count = input.read(data)) != -1) {
                total += count;
                // publishing the progress....
                // After this onProgressUpdate will be called
                publishProgress((int) ((total * 100) / lenghtOfFile));

                // writing data to file
                output.write(data, 0, count);
            }

            // flushing output
            output.flush();

            // closing streams
            output.close();
            input.close();

            return Uri.parse(filename);
        } catch (Exception e) {
                Log.e(TAG, e.getMessage());
            return null;
        }

    }

    @Override
    protected abstract void onProgressUpdate(Integer... values);

    @Override
    protected abstract void onPostExecute(Uri uri);
}
