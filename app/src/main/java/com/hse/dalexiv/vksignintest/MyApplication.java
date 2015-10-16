package com.hse.dalexiv.vksignintest;

import android.app.Application;

import com.vk.sdk.VKSdk;

/**
 * Created by dalex on 10/11/2015.
 */
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
        VKSdk.initialize(this);
    }
}
