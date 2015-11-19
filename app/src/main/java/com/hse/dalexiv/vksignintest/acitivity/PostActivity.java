package com.hse.dalexiv.vksignintest.acitivity;

import android.graphics.Bitmap;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.downloader.LoadImage;
import com.hse.dalexiv.vksignintest.model.Post;

import org.w3c.dom.Text;

import java.lang.ref.WeakReference;

public class PostActivity extends AppCompatActivity {
    ImageView mImageView;
    TextView mTextView;
    WeakReference<Bitmap> currentPic;
    Post mPost;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_post);

        mImageView = (ImageView) findViewById(R.id.imgView);
        mTextView = (TextView) findViewById(R.id.txtView);

        Gson gson = new Gson();
        String json = getIntent().getStringExtra("post");
        mPost = gson.fromJson(json, Post.class);

        currentPic
                = new WeakReference<>(LoadImage.getBitmapFromFile(mPost.getUriToImage()));
        mImageView.setImageBitmap(currentPic.get());
        mTextView.setText(mPost.getText());
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
}
