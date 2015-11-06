package com.hse.dalexiv.vksignintest.acitivity;

import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;

import com.google.gson.Gson;
import com.hse.dalexiv.vksignintest.Downloader.VKDownloadManager;
import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.comms.IShow;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.hse.dalexiv.vksignintest.model.Post;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Arrays;

public class MainActivity extends AppCompatActivity {
    private TextView myText;
    private View coordinatorView;
    private DBHelper db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // Going to fullscreen

        // Do other stuff
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        myText = (TextView) findViewById(R.id.myText);
        coordinatorView = findViewById(R.id.mysnack);


        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            VKDownloadManager downloader = new VKDownloadManager(new IShow() {
                @Override
                public void show(String text, boolean isLong) {
                    showException(text, isLong);
                }
            });

            myText.setText(downloader.testRequest());
            downloader.checkPermissions();
            Bundle res = downloader.downloadAllTimesAndLinks();

            Gson gson = new Gson();
            Post[] mPosts = gson.fromJson(res.getString("data"), Post[].class);
            Arrays.sort(mPosts);
            if (mPosts != null)
            {
                db = new DBHelper(this, null, null, 5);
                for (Post post : mPosts)
                    db.insert(post);

                DateTime curTime = new DateTime();
                DateTime timeToFind = new DateTime(2015, 1, 1,
                        curTime.getHourOfDay(), curTime.getMinuteOfHour());

                Post target = db.getClosestTime(new Post(timeToFind));

                myText.setText(myText.getText() + target.toString());
                ArrayList<Post> fromDB = db.getEverything();

                for (Post post : fromDB)
                    myText.setText(myText.getText() + post.toString());

            }

        } else
            showException("Login was failed, sry", true);
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
            Snackbar.make(coordinatorView, exceptionText, Snackbar.LENGTH_LONG).show();
        else
            Snackbar.make(coordinatorView, exceptionText, Snackbar.LENGTH_SHORT).show();

    }

}
