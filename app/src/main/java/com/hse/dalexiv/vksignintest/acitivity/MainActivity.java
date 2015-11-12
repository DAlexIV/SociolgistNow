package com.hse.dalexiv.vksignintest.acitivity;

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
import com.hse.dalexiv.vksignintest.downloader.VKDownloadManager;
import com.hse.dalexiv.vksignintest.model.Post;

import org.joda.time.DateTime;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private final int MY_REQUEST_CODE = 777;
    private TextView mText;
    private View mCoordinatorView;
    private ProgressBar mProgressBar;
    private ImageView mImageView;
    private DBHelper db;
    private WeakReference<Bitmap> testBitmap;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Going to fullscreen

        // Do other stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mText = (TextView) findViewById(R.id.mText);
        mCoordinatorView = findViewById(R.id.mysnack);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mImageView = (ImageView) findViewById(R.id.imageView);

        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            mProgressBar.setMax(100);
            requestPerms();
            downloadStuff();

        } else
            showException("Login was failed, sry", true);
    }

    private void downloadStuff() {
        ImageDownloader imageDownloader = new ImageDownloader(this) {
            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressBar.setProgress(values[0]);
            }

            @Override
            protected void onPostExecute(Uri uri) {
                mText.setText("Done!");

            }
        };

        VKDownloadManager downloader = new VKDownloadManager(new IShow() {
            @Override
            public void show(String text, boolean isLong) {
                showException(text, isLong);
            }
        });

        mText.setText("Checking permissions");
        mProgressBar.setProgress(10);
        downloader.checkPermissions();

        mText.setText("Initializing database");
        mProgressBar.setProgress(30);
        db = new DBHelper(this, null, null, 7);
        if (db.checkIfEmpty()) {
            mText.setText("Downloading links");
            Bundle res = downloader.downloadAllTimesAndLinks();
            Gson gson = new Gson();
            Post[] mPosts = gson.fromJson(res.getString("data"), Post[].class);
            Arrays.sort(mPosts);
            for (Post post : mPosts)
                db.insert(post);
        }

        DateTime curTime = new DateTime();
        DateTime timeToFind = new DateTime(2015, 1, 1,
                curTime.getHourOfDay(), curTime.getMinuteOfHour());

        Post random = db.getRandomPost();
        //mText.setText(mText.getText() +"\n" + "RANDOM!!!" + "\n" + random.toString());


        Post target = db.getClosestTime(new Post(timeToFind));
        //mText.setText(mText.getText() + target.toString());

        db.getEverything();
        mText.setText("All done");
        mProgressBar.setProgress(90);

        imageDownloader.execute(new String[]{random.getUrl(), "test.jpg"});
            /*
            for (Post post : fromDB)
                mText.setText(mText.getText() + post.toString());
    `       */
    }

    private Bitmap getBitmapFromFile(String name) {
        File sd = Environment.getExternalStorageDirectory();
        File image = new File(sd + "/", name);
        BitmapFactory.Options bmOptions = new BitmapFactory.Options();
        return BitmapFactory.decodeFile(image.getAbsolutePath(), bmOptions);
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
        if (isLong)
            Snackbar.make(mCoordinatorView, exceptionText, Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(mCoordinatorView, exceptionText, Snackbar.LENGTH_SHORT).show();

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case MY_REQUEST_CODE:
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED
                        && grantResults[1] == PackageManager.PERMISSION_GRANTED) {
                    testBitmap = new WeakReference<Bitmap>(getBitmapFromFile("test.jpg"));
                    mImageView.setImageBitmap(testBitmap.get());
                }
                else
                {
                    mText.setText("Sorry, we can't work without saving pics on your device");
                }
        }

    }
}
