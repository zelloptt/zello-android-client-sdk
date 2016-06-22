# Zello Android client SDK

## Overview

Zello Android client SDK allows you integrate [Zello for Work](https://zellowork.com/) push-to-talk into your own application. The SDK uses cross-process communication to let your app connect to Zello app installed on the device and remotely control it. Supported features include:

* Send voice messages
* Get notiications about incoming voice messages
* Get the list of contacts and their status
* Configure and switch user accounts
* Connect and disconnect channels
* Mute and unmute users or channels
* Set availability status
* Set custom text status
* Control auto-run and other Zello app options

## Installation

### Sign up for Zello for Work account

Go to http://zellowork.com/ and click __Start your network__ button. If you already have a network, click __Sign In__. A free Zello for Work account supports up to five users and has no time limit.

### Get Zello for Work app

Before you can use the SDK install Zello for Work app on your phone. You can do it from __Get app__ section of the web console or by navigating to `http://<network name>.zellowork.com/app` on your phone. 

__NB__: Zello app downloaded from Google Play is not supported by the SDK.

### Install Android Studio and configure your project

[Download Android Studio](https://developer.android.com/studio/index.html) and install it. Open your exisitng project or create a new one. The minimum API level supported by the SDK is 4 (Donut).

Place [zello-sdk.jar](https://github.com/zelloptt/zello-android-client-sdk/blob/master/zello-sdk.jar?raw=true) file into `libs` folder of your project, then right-click the file in Android Studio and select “Add as Library…”.

## Using the SDK

### Configuring the SDK

The first thing you need to do in your app to start using Zello SDK is to configure it. In the most cases you'd want to do it in your `Application.onCreate()` method:

```java
public class App extends Application {

   @Override
   public void onCreate() {
       super.onCreate();

       Zello.getInstance().configure("net.loudtalks", this);
   }

}
```
