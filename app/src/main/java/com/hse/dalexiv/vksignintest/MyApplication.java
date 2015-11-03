package com.hse.dalexiv.vksignintest;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by dalexiv on 02.11.15.
 */
public class MyApplication extends Application{
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
