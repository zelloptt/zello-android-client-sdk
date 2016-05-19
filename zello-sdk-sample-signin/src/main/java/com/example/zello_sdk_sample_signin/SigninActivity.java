package com.example.zello_sdk_sample_signin;

import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import android.app.Activity;
import android.widget.Button;
import android.widget.TextView;

import com.zello.sdk.AppState;
import com.zello.sdk.Tab;

public class SigninActivity extends Activity implements com.zello.sdk.Events {

    //region Lifecycle Methods

    private EditText networkEdit;
    private EditText usernameEdit;
    private EditText passwordEdit;
    private Button loginButton;
    private TextView textView;
    private CheckBox perishableCheckBox;

    private com.zello.sdk.Sdk zelloSDK = new com.zello.sdk.Sdk();
    private com.zello.sdk.AppState appState = new com.zello.sdk.AppState();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_signin);

        networkEdit = (EditText)findViewById(R.id.networkEdit);
        usernameEdit = (EditText)findViewById(R.id.usernameEdit);
        passwordEdit = (EditText)findViewById(R.id.passwordEdit);
        loginButton = (Button)findViewById(R.id.loginButton);
        textView = (TextView)findViewById(R.id.textView);
        perishableCheckBox = (CheckBox)findViewById(R.id.checkBox);

        zelloSDK.onCreate("com.pttsdk", this, this);

        loginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String network = networkEdit.getText().toString();
                String username = usernameEdit.getText().toString();
                String password = passwordEdit.getText().toString();
                boolean perishable = perishableCheckBox.isChecked();

                zelloSDK.signIn(network, username, password, perishable);
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        zelloSDK.onDestroy();
    }

    @Override
    protected void onResume() {
        super.onResume();

        zelloSDK.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();

        zelloSDK.onPause();
    }

    //endregion

    //region Zello SDK Events

    @Override
    public void onMessageStateChanged() {

    }

    @Override
    public void onSelectedContactChanged() {

    }

    @Override
    public void onAudioStateChanged() {

    }

    @Override
    public void onContactsChanged() {

    }

    @Override
    public void onLastContactsTabChanged(Tab tab) {

    }

    @Override
    public void onAppStateChanged() {
        zelloSDK.getAppState(appState);

        if (appState.isSignedIn()) {
            textView.setText("YOU ARE SIGNED IN.");
        } else if (appState.isSigningIn()) {
            textView.setText("SIGNING IN...");
        } else {
            textView.setText("SIGN IN.");
        }
    }

    //endregion

}
