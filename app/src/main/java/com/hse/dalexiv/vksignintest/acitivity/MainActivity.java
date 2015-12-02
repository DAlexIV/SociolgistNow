package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.app.AppConstants;
import com.hse.dalexiv.vksignintest.comms.IShow;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.downloader.ImageDownloader;
import com.hse.dalexiv.vksignintest.downloader.VKDownloadManager;
import com.hse.dalexiv.vksignintest.model.Post;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final int MY_REQUEST_CODE = 777;
    private ProgressBar mProgressBar;
    private DBHelper db;
    private Post target;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Going to fullscreen

        // Do other stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            requestPerms();
            downloadStuff();

        } else
            showException("Login was failed, sry", true);
    }

    private void downloadStuff() {
        final ImageDownloader imageDownloader = new ImageDownloader(this) {
            @Override
            protected void onProgressUpdate(Integer... values) {
                if (mProgressBar.isIndeterminate())
                    mProgressBar.setIndeterminate(false);
                mProgressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(String path) {
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
        }, this) {
            @Override
            public void processResults(Post[] posts) {
                Arrays.sort(posts);
                for (Post post : posts)
                    db.insert(post);
                getCurrentPostAndDownloadPic(imageDownloader);
            }
        };

        mProgressBar.setIndeterminate(true);

        if (db.checkIfEmpty()) {
            downloader.checkPermissions();
            downloader.downloadAllTimesAndLinks();
        } else {
            getCurrentPostAndDownloadPic(imageDownloader);
        }

    }

    private void getCurrentPostAndDownloadPic(ImageDownloader imageDownloader) {
        target = db.getClosestTime(Post.createCurrentTimePost());
        //mText.setText(mText.getText() + target.toString());

        imageDownloader.execute(new String[]{target.getPreviewPicURL(), AppConstants.IMAGE_NAME});
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
        if (exceptionText.equals(getString(R.string.noInGroup)))

        Log.d(TAG, exceptionText);
    }
}
