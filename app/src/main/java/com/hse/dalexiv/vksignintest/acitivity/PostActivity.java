package com.hse.dalexiv.vksignintest.acitivity;

import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.ShareActionProvider;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.app.AppConstants;

import com.hse.dalexiv.vksignintest.comms.ICallback;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.downloader.ImageDownloader;
import com.hse.dalexiv.vksignintest.downloader.LoadImage;
import com.hse.dalexiv.vksignintest.model.Post;

import java.lang.ref.WeakReference;

public class PostActivity extends AppCompatActivity implements SwipeRefreshLayout.OnRefreshListener {
    private volatile boolean isImageUpdating;
    private ProgressBar mProgressBar;
    private ProgressBar mProgressBarUnder;

    private ShareActionProvider mShareActionProvider;
    private CoordinatorLayout mCoordinatorLayout;

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

        initializeViews();

        Gson gson = new Gson();
        String json = getIntent().getStringExtra("post");
        mPost = gson.fromJson(json, Post.class);

        db = new DBHelper(this, null, null, 3);

        showPost(new ICallback() {
            @Override
            public void callback(View v) {
                onRefresh();
            }
        });

        isImageUpdating = false;
    }

    private void initializeViews() {
        mProgressBar = (ProgressBar) findViewById(R.id.progBar);
        mImageView = (ImageView) findViewById(R.id.imgView);
        mTextView = (TextView) findViewById(R.id.txtView);
        mLikesTextView = (TextView) findViewById(R.id.likesText);
        mCommsTextView = (TextView) findViewById(R.id.commsText);
        mProgressBarUnder = (ProgressBar) findViewById(R.id.progBarUnder);
        mFabbyScrollView = (MyFabbyScrollView) findViewById(R.id.scroll);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.coordinator);
        mFAB = (FloatingActionButton) findViewById(R.id.fab);

        mFabbyScrollView.setFab(mFAB);

        mSwipeRefresh = (SwipeRefreshLayout) findViewById(R.id.swipeRefresh);
        mSwipeRefresh.setOnRefreshListener(this);
        mSwipeRefresh.setColorSchemeColors(R.color.primary, R.color.primaryDark, R.color.colorAccent);

        mProgressBar.setIndeterminate(true);
        mProgressBar.setVisibility(View.GONE);
        mProgressBarUnder.setIndeterminate(true);
        mProgressBarUnder.setVisibility(View.GONE);
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
                    if (uri == null)
                        Snackbar.make(mCoordinatorLayout, R.string.no_connection2, Snackbar.LENGTH_LONG).setAction("Ещё раз!", new View.OnClickListener() {
                            @Override
                            public void onClick(View v) {
                                imageClick(v);
                            }
                        }).show();
                    else {
                        Intent toGallery = new Intent(Intent.ACTION_VIEW);
                        toGallery.setDataAndType(Uri.parse("file://" + Environment.getExternalStorageDirectory() + "/" + uri),
                                "image/*");
                        startActivity(toGallery);
                    }

                    mProgressBar.setVisibility(View.GONE);
                    mProgressBar.setProgress(0);
                    mProgressBar.setIndeterminate(false);
                    isImageUpdating = false;

                }

                @Override
                protected void onProgressUpdate(Integer... values) {
                    if (mProgressBar.isIndeterminate())
                        mProgressBar.setIndeterminate(false);
                    mProgressBar.setProgress(values[0]);
                }
            };
            downloadFull.execute(new String[]{mPost.getFullPicURL(), AppConstants.IMAGE_NAME_FULL});
            mProgressBar.setVisibility(View.VISIBLE);
            mFabbyScrollView.scrollTo(0, mFabbyScrollView.getBottom());
        }
    }

    public void fabClick(View v) {
        if (!isImageUpdating) {
            isImageUpdating = true;
            mPost = db.getRandomPost();

            final ImageDownloader imageDownloader = new ImageDownloader(this) {
                @Override
                protected void onProgressUpdate(Integer... values) {
                    if (mProgressBarUnder.isIndeterminate())
                        mProgressBarUnder.setIndeterminate(false);
                    mProgressBarUnder.setProgress(values[0]);
                }

                @Override
                protected void onPostExecute(String path) {
                    mPost.setUriToImage(path);
                    showPost(new ICallback() {
                        @Override
                        public void callback(View v) {
                            fabClick(v);
                        }
                    });
                    mProgressBarUnder.setVisibility(View.GONE);
                    mProgressBarUnder.setProgress(0);
                    isImageUpdating = false;
                    mProgressBarUnder.setIndeterminate(true);


                    SharedPreferences pref = getPreferences(MODE_PRIVATE);
                    if (pref.getBoolean("my_first_time", true)) {
                        // we are first time
                        Snackbar.make(mCoordinatorLayout, R.string.firstTimeMes, Snackbar.LENGTH_LONG).show();

                        pref.edit().putBoolean("my_first_time", false).apply();
                    }
                }
            };

            imageDownloader.execute(new String[]{mPost.getPreviewPicURL(), AppConstants.IMAGE_NAME});
            mProgressBarUnder.setVisibility(View.VISIBLE);
            mFabbyScrollView.scrollTo(0, mFabbyScrollView.getBottom());
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_post, menu);
        MenuItem item = menu.findItem(R.id.menu_item_share);
        mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(item);
        setShareActionProvider(createSharingIntent());
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        return super.onOptionsItemSelected(item);
    }

    public void showPost(final ICallback callback) {
        if (mPost.getUriToImage() == null)
            Snackbar.make(mCoordinatorLayout, R.string.no_connection, Snackbar.LENGTH_LONG).setAction("Ещё раз!", new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    callback.callback(v);
                }
            }).show();
        else {
            currentPic
                    = new WeakReference<>(LoadImage.getBitmapFromFile(mPost.getUriToImage()));
            mImageView.setImageBitmap(currentPic.get());

            mTextView.setText(mPost.getText());
            mLikesTextView.setText(Integer.toString(mPost.getLikes()));
            mCommsTextView.setText(Integer.toString(mPost.getComments()));
            setShareActionProvider(createSharingIntent());
        }
    }

    @Override
    public void onRefresh() {
        if (!isImageUpdating) {
            isImageUpdating = true;
            Post cached = mPost;
            mPost = db.getClosestTime(Post.createCurrentTimePost());
            if (cached.equals(mPost) && cached.getUriToImage() != null) {
                Snackbar.make(mCoordinatorLayout, R.string.newest, Snackbar.LENGTH_LONG).show();
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
                        showPost(new ICallback() {
                            @Override
                            public void callback(View v) {
                                onRefresh();
                            }
                        });
                        mSwipeRefresh.setRefreshing(false);
                        isImageUpdating = false;
                    }
                };

                imageDownloader.execute(new String[]{mPost.getPreviewPicURL(), AppConstants.IMAGE_NAME});

            }

        }
        mSwipeRefresh.setRefreshing(false);
    }

    private void setShareActionProvider(Intent shareIntent) {
        if (mShareActionProvider != null)
            mShareActionProvider.setShareIntent(shareIntent);
    }

    private Intent createSharingIntent() {
        final Intent share = new Intent(Intent.ACTION_SEND);
        String type = "image/*";
        String mediaPath = "file://" + Environment.getExternalStorageDirectory() + "/" + mPost.getUriToImage();
        share.setType(type);
        share.putExtra(Intent.EXTRA_STREAM, Uri.parse(mediaPath))
                .putExtra(Intent.EXTRA_TEXT, mPost.getText() + getString(R.string.tweetMes));
        return share;
    }

    public void goToVKGroup(MenuItem item) {
        Intent toBrowser = new Intent(Intent.ACTION_VIEW);
        toBrowser.setData(Uri.parse(AppConstants.GROUP_URL));
        startActivity(toBrowser);
    }
}
