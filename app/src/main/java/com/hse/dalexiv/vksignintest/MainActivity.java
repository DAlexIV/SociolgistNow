package com.hse.dalexiv.vksignintest;

import android.app.Activity;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;

import com.vk.sdk.api.VKApi;
import com.vk.sdk.api.VKApiConst;
import com.vk.sdk.api.VKError;
import com.vk.sdk.api.VKParameters;
import com.vk.sdk.api.VKRequest;
import com.vk.sdk.api.VKResponse;
import com.vk.sdk.api.httpClient.VKJsonOperation;

import org.json.JSONException;
import org.json.JSONObject;

public class MainActivity extends Activity {
    private TextView myText;
    private View coordinatorView;
    private final String TEST_ID = "50323156";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main_activity);

        myText = (TextView) findViewById(R.id.myText);
        coordinatorView = findViewById(R.id.mysnack);

        String response = getIntent().getExtras().getString("result");
        if (response.equals("OK")) {
            VKRequest request = VKApi.users().get(VKParameters.from(VKApiConst.USER_IDS, TEST_ID));
            request.executeSyncWithListener(new VKRequest.VKRequestListener() {
                @Override
                public void onComplete(VKResponse response) {
                    super.onComplete(response);
                    myText.setText(response.json.toString());
                }
            });

            checkPermissions();
        }
        else
            Snackbar.make(coordinatorView, "Login failed", Snackbar.LENGTH_LONG)
                    .show();
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

    private void checkPermissions() {
        final VKRequest checkPermission = VKApi.groups().isMember(VKParameters.from(VKApiConst.GROUP_ID, "92209938"));
        checkPermission.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                myText.setText(myText.getText());

                JSONObject jsonResp = response.json;
                try {
                    if (jsonResp.get("response").toString().equals("0"))
                        Snackbar.make(coordinatorView, "You aren't member of this group, sry", Snackbar.LENGTH_LONG)
                                .show();
                    else if (jsonResp.get("response").toString().equals("1")) {
                        Snackbar.make(coordinatorView, "Everything is okay, stepping next", Snackbar.LENGTH_SHORT)
                                .show();
                        downloadAllPosts();
                    }
                    else
                        throw new Exception("WRONG RESPONSE");
                } catch
                        (Exception e) {
                    Snackbar.make(coordinatorView, e.getMessage(), Snackbar.LENGTH_LONG)
                            .show();
                }

            }

            @Override
            public void onError(VKError error) {
                super.onError(error);

            }
        });
    }
    private void downloadAllPosts()
    {
        VKRequest main_req = VKApi.wall()
                .get(VKParameters.from(VKApiConst.OWNER_ID, "-92209938", VKApiConst.COUNT, "1"));
        main_req.executeWithListener(new VKRequest.VKRequestListener() {
            @Override
            public void onError(VKError error) {
                super.onError(error);
                Snackbar.make(coordinatorView, error.errorMessage, Snackbar.LENGTH_LONG);
            }

            @Override
            public void onComplete(VKResponse response) {
                super.onComplete(response);
                myText.setText(response.json.toString());
            }
        });
    }

}
