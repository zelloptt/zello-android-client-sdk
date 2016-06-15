package com.zello.sdk.sample;

import android.app.Application;

import com.zello.sdk.Zello;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

        //Zello.getInstance().configure("net.loudtalks", this); // Use to connect to apk from zellowork.com
        Zello.getInstance().configure("com.pttsdk", this); // Use with generic apk
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Zello.getInstance().unconfigure();
    }

}
