package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
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
    private volatile boolean isImageUpdating;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarUnder;

    private ImageView mImageView;
    private TextView mTextView;
    private TextView mLikesTextView;
    private TextView mCommsTextView;
    private SwipeRefreshLayout mSwipeRefresh;
    private MyFabbyScrollView mFabbyScrollView;
    private FloatingActionButton mFAB;

    private DBHelper db;
    private WeakReference<Bitmap> currentPic;
    private Post mPost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mProgressBar = (ProgressBar) findViewById(R.id.progBar);
        mImageView = (ImageView) findViewById(R.id.imgView);
        mTextView = (TextView) findViewById(R.id.txtView);
        mLikesTextView = (TextView) findViewById(R.id.likesText);
        mCommsTextView = (TextView) findViewById(R.id.commsText);
        mProgressBarUnder = (ProgressBar) findViewById(R.id.progBarUnder);
        mFabbyScrollView = (MyFabbyScrollView) findViewById(R.id.scroll);
        mFAB = (FloatingActionButton) findViewById(R.id.fab);

        mFabbyScrollView.setFab(mFAB);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.primary, R.color.primaryDark, R.color.colorAccent);

        mProgressBar.setVisibility(View.GONE);
        mProgressBarUnder.setVisibility(View.GONE);

        Gson gson = new Gson();
        String json = getIntent().getStringExtra("post");
        mPost = gson.fromJson(json, Post.class);

        db = new DBHelper(this, null, null, 3);

        showPost();

        isImageUpdating = false;
    }

    public void cardClick(View v) {
        if (v != mImageView) {
            Intent toBrowser = new Intent(Intent.ACTION_VIEW);
            toBrowser.setData(Uri.parse(mPost.getPostURL()));
            startActivity(toBrowser);
        }
    }

    public void imageClick(View v) {
        if (!isImageUpdating) {
            isImageUpdating = true;
            ImageDownloader downloadFull = new ImageDownloader(this) {
                @Override
                protected void onPostExecute(String uri) {
                    Intent toGallery = new Intent(Intent.ACTION_VIEW);
                    toGallery.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + uri),
                            "image/*");
                    startActivity(toGallery);

                    mProgressBar.setVisibility(View.GONE);
                    mProgressBar.setProgress(0);
                    isImageUpdating = false;
                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    mProgressBar.setProgress(values[0]);
                }
            };
            downloadFull.execute(new String[]{mPost.getFullPicURL(), MainActivity.IMAGE_NAME_FULL});
            mProgressBar.setVisibility(View.VISIBLE);
            mProgressBar.setProgress(10);
        }
    }

    public void fabClick(View v) {
        if (!isImageUpdating) {
            isImageUpdating = true;
            mPost = db.getRandomPost();

            final ImageDownloader imageDownloader = new ImageDownloader(this) {
                @Override
                protected void onProgressUpdate(Integer... values) {
                    mProgressBarUnder.setProgress(values[0]);
                }

                @Override
                protected void onPostExecute(String path) {
                    mPost.setUriToImage(path);
                    showPost();
                    mProgressBarUnder.setVisibility(View.GONE);
                    mProgressBarUnder.setProgress(0);
                    isImageUpdating = false;
                }
            };

            imageDownloader.execute(new String[]{mPost.getPreviewPicURL(), MainActivity.IMAGE_NAME});
            mProgressBarUnder.setVisibility(View.VISIBLE);
            mProgressBarUnder.setProgress(10);
        }
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
        if (!isImageUpdating) {
            isImageUpdating = true;
            Post cached = mPost;
            mPost = db.getClosestTime(Post.createCurrentTimePost());
            if (cached.equals(mPost)) {
                Snackbar.make(mFAB, "Это самый свежий социолог!", Snackbar.LENGTH_LONG).show();
                mSwipeRefresh.setRefreshing(false);
                isImageUpdating = false;
            } else {
                final ImageDownloader imageDownloader = new ImageDownloader(this) {
                    @Override
                    protected void onProgressUpdate(Integer... values) {
                    }

                    @Override
                    protected void onPostExecute(String path) {
                        mPost.setUriToImage(path);
                        showPost();
                        mSwipeRefresh.setRefreshing(false);
                        isImageUpdating = false;
                    }
                };

                imageDownloader.execute(new String[]{mPost.getPreviewPicURL(), MainActivity.IMAGE_NAME});

            }

        }
        mSwipeRefresh.setRefreshing(false);
    }
}
