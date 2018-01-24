package com.zello.sdk.sample;

import android.app.Application;

import com.zello.sdk.Zello;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();

		// Use to connect to an app installed from an apk obtained from https://www.zellowork.com
		//Zello.getInstance().configure("net.loudtalks", this);

		// Use with an app installed from a generic PTT SDK apk obtained from https://github.com/zelloptt/zello-android-client-sdk/releases
		Zello.getInstance().configure("com.pttsdk", this);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();

        Zello.getInstance().unconfigure();
    }

}
