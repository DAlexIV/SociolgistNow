package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.comms.IShow;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.downloader.ImageDownloader;
import com.hse.dalexiv.vksignintest.downloader.LoadImage;
import com.hse.dalexiv.vksignintest.downloader.VKDownloadManager;
import com.hse.dalexiv.vksignintest.model.Post;

import org.joda.time.DateTime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    public static final String IMAGE_NAME = "socio.jpg";
    public static final String IMAGE_NAME_FULL = "full.jpg";
    private final int MY_REQUEST_CODE = 777;
    private TextView mText;
    private ProgressBar mProgressBar;
    private DBHelper db;
    private Post target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Going to fullscreen

        // Do other stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mText = (TextView) findViewById(R.id.mText);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            mProgressBar.setMax(100);
            requestPerms();
            downloadStuff();

        } else
            showException("Login was failed, sry", true);
    }

    private void downloadStuff() {
        final ImageDownloader imageDownloader = new ImageDownloader(this) {
            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String path) {
                mText.setText("Done!");
                target.setUriToImage(path);
                // Start new activity with path intent
                Intent toPostActivity = new Intent(MainActivity.this, PostActivity.class);

                Gson gson = new Gson();
                String serializedPost = gson.toJson(target);
                toPostActivity.putExtra("post", serializedPost);

                startActivity(toPostActivity);
                finish();
            }
        };

        db = new DBHelper(this, null, null, 3);

        VKDownloadManager downloader = new VKDownloadManager(new IShow() {
            @Override
            public void show(String text, boolean isLong) {
                showException(text, isLong);
            }
        }) {
            @Override
            public void processResults(Post[] posts) {

                if (db.checkIfEmpty()) {
                    mText.setText("Downloading links");
                    Arrays.sort(posts);
                    for (Post post : posts)
                        db.insert(post);
                }

                target = db.getClosestTime(Post.createCurrentTimePost());
                //mText.setText(mText.getText() + target.toString());

                mText.setText("Downloading fresh pic");
                imageDownloader.execute(new String[]{target.getPreviewPicURL(), IMAGE_NAME});
            }
        };

        mText.setText("Checking permissions");
        mProgressBar.setProgress(10);
        downloader.checkPermissions();

        mText.setText("Initializing database");
        mProgressBar.setProgress(30);

            /*
            for (Post post : fromDB)
                mText.setText(mText.getText() + post.toString());
    `       */
    }



    private void requestPerms() {
        if (ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this,
                android.Manifest.permission.READ_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    || ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.READ_EXTERNAL_STORAGE))
                showException("We need to save photos somewhere, so agree pls", true);
            else
                ActivityCompat.requestPermissions(this,
                        new String[]{android.Manifest.permission.READ_EXTERNAL_STORAGE,
                                android.Manifest.permission.WRITE_EXTERNAL_STORAGE}, MY_REQUEST_CODE);
        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }


    private void showException(String exceptionText, boolean isLong) {
        mText.setText(exceptionText);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    mText.setText("Thanks");
                }
                else
                {
                    mText.setText("Sorry, we can't work without saving pics on your device");
                }
        }

    }
}
