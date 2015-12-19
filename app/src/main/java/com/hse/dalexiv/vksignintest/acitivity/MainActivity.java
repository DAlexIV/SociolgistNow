package com.hse.dalexiv.vksignintest.acitivity;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.ProgressBar;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.app.AppConstants;
import com.hse.dalexiv.vksignintest.comms.IShow;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.downloader.ImageDownloader;
import com.hse.dalexiv.vksignintest.downloader.VKDownloadManager;
import com.hse.dalexiv.vksignintest.model.Post;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Random;

public class MainActivity extends AppCompatActivity {
    private final String TAG = this.getClass().getSimpleName();
    private final static Random gen = new Random();
    private final int DB_VERSION = 3;
    private ProgressBar mProgressBar;
    private ImageView mBackground;
    private DBHelper db;
    private Post target;

    private ArrayList<Integer> backs = new ArrayList<Integer>() {{
        add(R.drawable.loading_image);
        add(R.drawable.loading_image2);
        add(R.drawable.loading_image3);
    }};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Going to fullscreen

        // Do other stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        mBackground = (ImageView) findViewById(R.id.background);
        setRandomBack();

        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);
        mProgressBar.setMax(100);

        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            downloadStuffAndStartActivity();

        } else
            showException(getString(R.string.bad_auth), true);
    }

    private void downloadStuffAndStartActivity() {
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

        db = new DBHelper(this, null, null, DB_VERSION);

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
            downloader.checkPermissionsAndThenDownload();
        } else {
            getCurrentPostAndDownloadPic(imageDownloader);
        }

    }

    private void getCurrentPostAndDownloadPic(ImageDownloader imageDownloader) {
        target = db.getClosestTime(Post.createCurrentTimePost());
        //mText.setText(mText.getText() + target.toString());

        imageDownloader.execute(new String[]{target.getPreviewPicURL(), AppConstants.IMAGE_NAME});
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

    private void showException(final String exceptionText, boolean isLong) {
        if (exceptionText != null) {
            if (exceptionText.equals(getString(R.string.sorry_text))
                    || exceptionText.equals(getString(R.string.bad_auth))) {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(exceptionText).setPositiveButton((exceptionText.equals(getString(R.string.sorry_text))) ? R.string.join : R.string.ok,
                        new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                if (exceptionText.equals(getString(R.string.bad_auth))) {
                                    finish();
                                    startActivity(new Intent(MainActivity.this, SignInActivity.class));
                                } else if (exceptionText.equals(getString(R.string.sorry_text))) {
                                    finish();
                                    goToVKGroup();

                                }
                            }
                        }).create().show();
                Log.d(TAG, exceptionText);
            } else {
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setMessage(exceptionText).setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                }).create().show();
                Log.d(TAG, exceptionText);
            }
        }

    }

    public void goToVKGroup() {
        Intent toBrowser = new Intent(Intent.ACTION_VIEW);
        toBrowser.setData(Uri.parse(AppConstants.GROUP_URL));
        startActivity(toBrowser);
    }

    private void setRandomBack() {

        mBackground.setImageDrawable(ContextCompat.getDrawable(this,
                backs.get(gen.nextInt(backs.size()))));
    }
}
