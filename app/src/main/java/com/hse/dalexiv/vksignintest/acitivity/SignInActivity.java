package com.hse.dalexiv.vksignintest.acitivity;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.hse.dalexiv.vksignintest.R;
import com.hse.dalexiv.vksignintest.db.DBHelper;
import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;

public class SignInActivity extends Activity {
    DBHelper db;

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                finish();
                startActivity(new Intent(SignInActivity.this, MainActivity.class).putExtra("result", "OK"));
            }

            @Override
            public void onError(VKError vkError) {
                startActivity(new Intent(SignInActivity.this, MainActivity.class).putExtra("result", "NOPE"));
                finish();
            }
        })) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.signin_activity);
        db = new DBHelper(this, null, null, 3);
        if (db.checkIfEmpty())
            VKSdk.login(this);
        else {
            startActivity(new Intent(SignInActivity.this, MainActivity.class).putExtra("result", "OK"));
            finish();
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
}
