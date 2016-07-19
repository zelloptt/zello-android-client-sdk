#Zello SDK migration guide#

This SDK release introduces several breaking changes so users migrating from legacy version of the SDK will need to update their code. 

##Replace com.zello.sdk.Sdk class with com.zello.sdk.Zello singleton##

Instead of creating an instance of `Sdk` class and initializing it using `Sdk.onCreate` method, you use `Zello.getInstance()` to access the instance, and then `Zello.configure()` to initialize it. 

Old version
```java
// Zello activty
public class TalkActivity extends Activity implememts com.zello.sdk.Events {
  private com.zello.sdk.Sdk _sdk = new com.zello.sdk.Sdk();
  
  @Override
  protected void onCreate(Bundel savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ...
    _sdk.onCreate("net.loudtalks", this, this);
  }

}
```

New version (activity)
```java
public class TalkActivity extends Activity implememts com.zello.sdk.Events {
  private com.zello.sdk.Zello _sdk; // Changed Sdk -> Zello
  
  @Override
  protected void onCreate(Bundel savedInstanceState) {
    super.onCreate(savedInstanceState);
    // ...
    _sdk = Zello.getInstance();
    _sdk.subscribeToEvents(this); // Explicitly subscribe for events
  }

}
```

New version (application)
```java
// Initialization is done once in Application subclass
public class App extends Application {

  @Override
  public void onCreate() {
    super.onCreate();
    // ...
    Zello.getInstance().configure("net.loudtalks", this);
  }

}

```

Most of the interface remains unchanged, the exceptions are listed in the next section.

##Update selected method names with new versions##

Old method name | New method name
----------------|----------------
Sdk.onCreate    | Zello.configure
Sdk.onDestroy   | Zello.unconfigure
Sdk.onPause     | Zello.enterPowerSavingMode
Sdk.onResume    | Zello.leavePowerSavingMode
Sdk.cancel      | Zello.cancelSignIn

##Review updated SDK documentation##

* [Getting started](https://github.com/zelloptt/zello-android-client-sdk/blob/master/README.md)
* [Zello SDK reference](http://zelloptt.github.io/zello-android-client-sdk/zello-sdk-documentation/)

