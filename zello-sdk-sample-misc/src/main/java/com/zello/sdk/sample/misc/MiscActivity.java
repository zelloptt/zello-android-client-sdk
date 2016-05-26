package com.zello.sdk.sample.misc;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.Resources;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.zello.sdk.Tab;

public class MiscActivity extends Activity implements com.zello.sdk.Events {

    private com.zello.sdk.Sdk zelloSDK = new com.zello.sdk.Sdk();
    private com.zello.sdk.AppState appState = new com.zello.sdk.AppState();

    private Button lockButton;
    private TextView statusTextView;
    private EditText externalIdEditText;
    private TextView externalIdTextView;

    //region Lifecycle methods
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_misc);

        lockButton = (Button)findViewById(R.id.lockButton);
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        externalIdEditText = (EditText)findViewById(R.id.externalIdEditText);
        externalIdTextView = (TextView)findViewById(R.id.externalIdTextView);

        lockButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                zelloSDK.getAppState(appState);

                if (appState.isLocked()) {
                    zelloSDK.unlock();
                } else {
                    zelloSDK.lock("Zello SDK Sample Misc", getPackageName());
                }
            }
        });

        externalIdEditText.setOnEditorActionListener(new EditText.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    zelloSDK.setExternalId(externalIdEditText.getText().toString());

                    hideKeyboard();

                    return true;
                }

                return false;
            }
        });

        zelloSDK.onCreate("com.pttsdk", this, this);
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

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        zelloSDK.getAppState(appState);
        if (appState.isAvailable() && !appState.isInitializing() && !appState.isLocked()) {
            getMenuInflater().inflate(R.menu.menu, menu);

            boolean available = false, solo = false, busy = false;
            if (!appState.isConfiguring() && appState.isSignedIn() && !appState.isSigningIn() && !appState.isSigningOut()) {
                com.zello.sdk.Status status = appState.getStatus();
                available = status == com.zello.sdk.Status.AVAILABLE;
                solo = status == com.zello.sdk.Status.SOLO;
                busy = status == com.zello.sdk.Status.BUSY;
            }

            showMenuItem(menu, R.id.menu_available, available);
            showMenuItem(menu, R.id.menu_solo, solo);
            showMenuItem(menu, R.id.menu_busy, busy);

            if (appState.isSignedIn()) {
                showMenuItem(menu, R.id.menu_enable_auto_run, !appState.isAutoRunEnabled());
                showMenuItem(menu, R.id.menu_disable_auto_run, appState.isAutoRunEnabled());
                showMenuItem(menu, R.id.menu_enable_auto_connect_channels, !appState.isChannelAutoConnectEnabled());
                showMenuItem(menu, R.id.menu_disable_auto_connect_channels, appState.isChannelAutoConnectEnabled());
            }

            showMenuItem(menu, R.id.menu_open_app, true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_set_status:
            case R.id.menu_available:
            case R.id.menu_solo:
            case R.id.menu_busy: {
                chooseStatus();
                break;
            }
            case R.id.menu_enable_auto_run: {
                zelloSDK.setAutoRun(true);
                break;
            }
            case R.id.menu_disable_auto_run: {
                zelloSDK.setAutoRun(false);
                break;
            }
            case R.id.menu_enable_auto_connect_channels: {
                zelloSDK.setAutoConnectChannels(true);
                break;
            }
            case R.id.menu_disable_auto_connect_channels: {
                zelloSDK.setAutoConnectChannels(false);
                break;
            }
            case R.id.menu_open_app: {
                zelloSDK.openMainScreen();
                break;
            }
        }

        return true;
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
            Helper.invalidateOptionsMenu(this);

            updateUIForSignedIn();
        } else if (appState.isSigningIn()) {
            updateUIForSigningIn();
        } else {
            updateUIForDisconnected();
        }
    }

    //endregion

    private void showMenuItem(Menu menu, int itemId, boolean show) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(show);
        }
    }

    private void chooseStatus() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Resources res = getResources();
        zelloSDK.getAppState(appState);

        com.zello.sdk.Status status = appState.getStatus();
        int selection = -1;
        switch (status) {
            case AVAILABLE: {
                selection = 0;
                break;
            }
            case SOLO: {
                selection = 1;
                break;
            }
            case BUSY: {
                selection = 2;
                break;
            }
        }
        if (appState.getStatusMessage() != null && !appState.getStatusMessage().equals("")) {
            selection = 3;
        }

        String[] items = new String[]{res.getString(R.string.menu_available), res.getString(R.string.menu_solo), res.getString(R.string.menu_busy), res.getString(R.string.menu_set_status_message)};
        builder.setSingleChoiceItems(items, selection, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which) {
                    case 0: {
                        zelloSDK.setStatus(com.zello.sdk.Status.AVAILABLE);
                        break;
                    }
                    case 1: {
                        zelloSDK.setStatus(com.zello.sdk.Status.SOLO);
                        break;
                    }
                    case 2: {
                        zelloSDK.setStatus(com.zello.sdk.Status.BUSY);
                        break;
                    }
                    case 3: {
                        createCustomStatusMessageDialog();
                        break;
                    }
                }
                dialog.dismiss();
            }
        });
        builder.setCancelable(true).setTitle(res.getString(R.string.menu_set_status));
        final AlertDialog dialog = builder.create();
        dialog.setCanceledOnTouchOutside(true);
        dialog.show();
    }

    private void updateUIForLocked() {
        Helper.invalidateOptionsMenu(this);

        externalIdEditText.setVisibility(View.INVISIBLE);
        externalIdTextView.setVisibility(View.INVISIBLE);

        statusTextView.setVisibility(View.VISIBLE);

        lockButton.setText(R.string.unlock);
        statusTextView.setText(R.string.status_locked);
    }

    private void updateUIForUnlocked() {
        Helper.invalidateOptionsMenu(this);

        externalIdEditText.setVisibility(View.VISIBLE);
        externalIdEditText.setText(appState.getExternalId());

        externalIdTextView.setVisibility(View.VISIBLE);

        statusTextView.setVisibility(View.INVISIBLE);

        lockButton.setText(R.string.lock);
    }

    private void updateUIForSignedIn() {
        if (appState.isLocked()) {
            updateUIForLocked();
        } else {
            updateUIForUnlocked();
        }

        lockButton.setVisibility(View.VISIBLE);
    }

    private void updateUIForSigningIn() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);
        lockButton.setVisibility(View.INVISIBLE);
        externalIdEditText.setVisibility(View.INVISIBLE);
        externalIdTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.sign_in_status_signing_in);
    }

    private void updateUIForDisconnected() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);
        lockButton.setVisibility(View.INVISIBLE);
        externalIdEditText.setVisibility(View.INVISIBLE);
        externalIdTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.sign_in_status_offline);
    }

    private void createCustomStatusMessageDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        Resources res = getResources();
        zelloSDK.getAppState(appState);
        String previousStatus = appState.getStatusMessage();
        final EditText statusEditText = new EditText(this);
        statusEditText.setText(previousStatus);
        builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                zelloSDK.setStatusMessage(statusEditText.getText().toString());
            }
        });
        builder.setNegativeButton("CANCEL", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(statusEditText);

        builder.show();
    }

    private void hideKeyboard() {
        View view = this.getCurrentFocus();
        if (view != null) {
            InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

}
