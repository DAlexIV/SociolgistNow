package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.downloader.ImageDownloader;
import com.hse.dalexiv.vksignintest.downloader.LoadImage;
import com.hse.dalexiv.vksignintest.model.Post;

import java.lang.ref.WeakReference;

public class PostActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    ProgressBar mProgressBar;
    ImageView mImageView;
    TextView mTextView;
    TextView mLikesTextView;
    TextView mCommsTextView;
    SwipeRefreshLayout mSwipeRefresh;
    DBHelper db;

    WeakReference<Bitmap> currentPic;
    Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mProgressBar = (ProgressBar) findViewById(R.id.progBar);
        mImageView = (ImageView) findViewById(R.id.imgView);
        mTextView = (TextView) findViewById(R.id.txtView);
        mLikesTextView = (TextView) findViewById(R.id.likesText);
        mCommsTextView = (TextView) findViewById(R.id.commsText);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.primary, R.color.primaryDark, R.color.colorAccent);

        mProgressBar.setVisibility(View.GONE);

        Gson gson = new Gson();
        String json = getIntent().getStringExtra("post");
        mPost = gson.fromJson(json, Post.class);

        db = new DBHelper(this, null, null, 3);

        showPost();
    }

    public void cardClick(View v) {
        if (v != mImageView) {
            Intent toBrowser = new Intent(Intent.ACTION_VIEW);
            toBrowser.setData(Uri.parse(mPost.getPostURL()));
            startActivity(toBrowser);
        }
    }

    public void imageClick(View v) {
        ImageDownloader downloadFull = new ImageDownloader(this) {
            @Override
            protected void onPostExecute(String uri) {
                Intent toGallery = new Intent(Intent.ACTION_VIEW);
                toGallery.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + uri),
                        "image/*");
                startActivity(toGallery);

                mProgressBar.setVisibility(View.GONE);
            }

            @Override
            protected void onProgressUpdate(Integer... values) {
                mProgressBar.setProgress(values[0]);
            }
        };
        downloadFull.execute(new String[]{mPost.getFullPicURL(), "full.jpg"});
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
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

    public void showPost() {
        currentPic
                = new WeakReference<>(LoadImage.getBitmapFromFile(mPost.getUriToImage()));
        mImageView.setImageBitmap(currentPic.get());

        mTextView.setText(mPost.getText());
        mLikesTextView.setText(Integer.toString(mPost.getLikes()));
        mCommsTextView.setText(Integer.toString(mPost.getComments()));
    }

    @Override
    public void onRefresh() {

        mPost = db.getClosestTime(Post.createCurrentTimePost());

        final ImageDownloader imageDownloader = new ImageDownloader(this) {
            @Override
            protected void onProgressUpdate(Integer... values) {
            }

            @Override
            protected void onPostExecute(String path) {
                mPost.setUriToImage(path);
                showPost();
                mSwipeRefresh.setRefreshing(false);
            }
        };

        imageDownloader.execute(new String[]{mPost.getPreviewPicURL(), MainActivity.IMAGE_NAME});


    }
}
