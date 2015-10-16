package com.hse.dalexiv.vksignintest;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.vk.sdk.VKAccessToken;
import com.vk.sdk.VKCallback;
import com.vk.sdk.VKSdk;
import com.vk.sdk.api.VKError;
import com.vk.sdk.util.VKUtil;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String[] fingerprints = VKUtil.getCertificateFingerprint(this, this.getPackageName());
        for(int i = 0; i<fingerprints.length;i++)
            Log.i("myApp", "Fingerprint:" + fingerprints[i]);
        VKUtil.getCertificateFingerprint(this, this.getPackageName());
        VKSdk.login(this);

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
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!VKSdk.onActivityResult(requestCode, resultCode, data, new VKCallback<VKAccessToken>() {
            @Override
            public void onResult(VKAccessToken vkAccessToken) {
                Intent startRes = new Intent(MainActivity.this, RegisterResult.class);
                startRes.putExtra("mes", "VSE ZAEBIS");
                startActivity(startRes);
            }

            @Override
            public void onError(VKError vkError) {
                Intent startRes = new Intent(MainActivity.this, RegisterResult.class);
                startRes.putExtra("mes", "VSE HUINA");
                startActivity(startRes);
            }
        }))
        {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

}
