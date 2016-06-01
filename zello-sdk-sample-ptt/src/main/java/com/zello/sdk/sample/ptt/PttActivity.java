package com.zello.sdk.sample.ptt;

import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.TextView;

import com.zello.sdk.*;
import android.widget.Button;
import android.widget.ToggleButton;

public class PttActivity extends Activity implements com.zello.sdk.Events {

    private TextView statusTextView;
    private ToggleButton connectChannelButton;
    private Button pttButton;
    private NoToggleButton speakerButton;
    private NoToggleButton earpieceButton;
    private NoToggleButton bluetoothButton;
    private View audioModeView;
    private TextView messageStateTextView;
    private TextView selectedContactTextView;

    private com.zello.sdk.AppState appState = new com.zello.sdk.AppState();
    private com.zello.sdk.Audio audio;
    private Contact selectedContact = new Contact();
    private MessageIn messageIn = new MessageIn();
    private MessageOut messageOut = new MessageOut();

    //region Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_ptt);

        statusTextView = (TextView)findViewById(R.id.statusTextView);
        connectChannelButton = (ToggleButton)findViewById(R.id.connectChannelButton);
        pttButton = (SquareButton)findViewById(R.id.pushToTalkButton);
        speakerButton = (NoToggleButton)findViewById(R.id.audio_speaker);
        earpieceButton = (NoToggleButton)findViewById(R.id.audio_earpiece);
        bluetoothButton = (NoToggleButton)findViewById(R.id.audio_bluetooth);
        audioModeView = (View)findViewById(R.id.audio_mode);
        messageStateTextView = (TextView)findViewById(R.id.messageStateTextView);
        selectedContactTextView = (TextView)findViewById(R.id.selectedContactTextView);

        // Constrain PTT button size
        pttButton.setMaxHeight(getResources().getDimensionPixelSize(R.dimen.talk_button_size));

        Zello.initialize("com.pttsdk", this, this);
        audio = Zello.getAudio();

        connectChannelButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                onConnectChannelButtonPressed();
            }
        });

        // PTT button push/release handler
        pttButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                int action = event.getAction();

                if (action == MotionEvent.ACTION_DOWN) {
                    Zello.beginMessage();
                } else if (action == MotionEvent.ACTION_UP || action == MotionEvent.ACTION_CANCEL) {
                    Zello.endMessage();
                }

                return false;
            }
        });

        // Audio modes
        speakerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audio.setMode(com.zello.sdk.AudioMode.SPEAKER);
                }
            }
        });

        earpieceButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audio.setMode(com.zello.sdk.AudioMode.EARPIECE);
                }
            }
        });

        bluetoothButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (audio != null) {
                    audio.setMode(com.zello.sdk.AudioMode.BLUETOOTH);
                }
            }
        });

        updateAudioMode();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Zello.unsubscribeFromEvents(this);
        Zello.uninitialize();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Zello.leavePowerSavingMode();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Zello.enterPowerSavingMode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        super.onCreateOptionsMenu(menu);

        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        Zello.getAppState(appState);

        if (appState.isSignedIn()) {
            getMenuInflater().inflate(R.menu.menu, menu);
            showMenuItem(menu, R.id.menu_mute_contact, true);

            Zello.getSelectedContact(selectedContact);
            String itemTitle = selectedContact.getMuted() ? "Unmute Contact" : "Mute Contact";
            menu.getItem(0).setTitle(itemTitle);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.menu_mute_contact) {
            Zello.getSelectedContact(selectedContact);

            Zello.muteContact(selectedContact, !selectedContact.getMuted());
        }

        return true;
    }

    //endregion

    //region Zello SDK Events

    @Override
    public void onAppStateChanged() {
        updateUI();
    }

    @Override
    public void onMessageStateChanged() {
        Zello.getMessageIn(messageIn);
        Zello.getMessageOut(messageOut);

        boolean incoming = messageIn.isActive(); // Is incoming message active?
        boolean outgoing = messageOut.isActive(); // Is outgoing message active?

        if (incoming) {
            String author = messageIn.getAuthor().getDisplayName(); // Is message from channel?

            if (author != null && author.length() > 0) {
                messageStateTextView.setText("Recieving message from " + messageIn.getFrom().getDisplayName() + " \\ " + author); // Show channel and author names
            } else {
                messageStateTextView.setText("Recieving message from " + messageIn.getFrom().getDisplayName()); // Show sender name
            }
        } else if (outgoing) {
            if (messageOut.isConnecting()) {
                messageStateTextView.setText("Connecting to " + selectedContact.getDisplayName());
            } else {
                messageStateTextView.setText("Outgoing Message to " + selectedContact.getDisplayName());
            }
        } else {
            messageStateTextView.setText("");
        }
    }

    @Override
    public void onSelectedContactChanged() {
        updateUI();
    }

    @Override
    public void onAudioStateChanged() {
        updateAudioMode();
    }

    @Override
    public void onContactsChanged() {

    }

    @Override
    public void onLastContactsTabChanged(Tab tab) {

    }

    //endregion

    private void updateUI() {
        Zello.getAppState(appState);

        if (appState.isSignedIn()) {
            Zello.getSelectedContact(selectedContact);

            if (selectedContact != null && selectedContact.getDisplayName() != null) {
                if (selectedContact.getStatus() == ContactStatus.AVAILABLE) {
                    updateUIForAvailableContact();
                } else {
                    updateUIForUnavailableContact();
                }
            } else {
                updateUIForNoContact();
            }
        } else if (appState.isSigningIn()) {
            updateUIForSigningIn();
        } else {
            updateUIForDisconnected();
        }
    }

    private void updateUIForAvailableContact() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.INVISIBLE);

        pttButton.setVisibility(View.VISIBLE);
        audioModeView.setVisibility(View.VISIBLE);
        selectedContactTextView.setVisibility(View.VISIBLE);
        selectedContactTextView.setText("Selected Contact: " + selectedContact.getDisplayName());

        updateConnectChannelButton();
        updateAudioMode();
    }

    private void updateUIForUnavailableContact() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);

        pttButton.setVisibility(View.INVISIBLE);
        audioModeView.setVisibility(View.INVISIBLE);
        speakerButton.setVisibility(View.INVISIBLE);
        bluetoothButton.setVisibility(View.INVISIBLE);
        earpieceButton.setVisibility(View.INVISIBLE);
        connectChannelButton.setVisibility(View.INVISIBLE);
        selectedContactTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.unavailable_selected_contact);
    }

    private void updateUIForNoContact() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);

        pttButton.setVisibility(View.INVISIBLE);
        audioModeView.setVisibility(View.INVISIBLE);
        speakerButton.setVisibility(View.INVISIBLE);
        bluetoothButton.setVisibility(View.INVISIBLE);
        earpieceButton.setVisibility(View.INVISIBLE);
        connectChannelButton.setVisibility(View.INVISIBLE);
        selectedContactTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.no_selected_contact);
    }

    private void updateUIForSigningIn() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);

        pttButton.setVisibility(View.INVISIBLE);
        audioModeView.setVisibility(View.INVISIBLE);
        speakerButton.setVisibility(View.INVISIBLE);
        bluetoothButton.setVisibility(View.INVISIBLE);
        earpieceButton.setVisibility(View.INVISIBLE);
        connectChannelButton.setVisibility(View.INVISIBLE);
        selectedContactTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.sign_in_status_signing_in);
    }

    private void updateUIForDisconnected() {
        Helper.invalidateOptionsMenu(this);

        statusTextView.setVisibility(View.VISIBLE);

        pttButton.setVisibility(View.INVISIBLE);
        audioModeView.setVisibility(View.INVISIBLE);
        connectChannelButton.setVisibility(View.INVISIBLE);
        selectedContactTextView.setVisibility(View.INVISIBLE);

        statusTextView.setText(R.string.sign_in_status_offline);
    }

    private void showMenuItem(Menu menu, int itemId, boolean show) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(show);
        }
    }

    private void updateAudioMode() {
        boolean speaker = false, earpiece = false, bluetooth = false;
        com.zello.sdk.AudioMode mode = com.zello.sdk.AudioMode.SPEAKER;

        // Can't set new mode while the mode is being changed
        boolean currentlyChangingMode = false;
        if (audio != null) {
            speaker = audio.isModeAvailable(com.zello.sdk.AudioMode.SPEAKER);
            earpiece = audio.isModeAvailable(com.zello.sdk.AudioMode.EARPIECE);
            bluetooth = audio.isModeAvailable(com.zello.sdk.AudioMode.BLUETOOTH);
            mode = audio.getMode();
            currentlyChangingMode = audio.isModeChanging();
        }

        // If none of the modes is available, the client app is old and needs to be updated
        if (bluetooth || earpiece || speaker) {
            speakerButton.setVisibility(speaker ? View.VISIBLE : View.GONE);
            if (speaker) {
                speakerButton.setChecked(mode == com.zello.sdk.AudioMode.SPEAKER);
                speakerButton.setEnabled(!currentlyChangingMode && (earpiece || bluetooth));
            }

            earpieceButton.setVisibility(earpiece ? View.VISIBLE : View.GONE);
            if (earpiece) {
                earpieceButton.setChecked(mode == com.zello.sdk.AudioMode.EARPIECE);
                earpieceButton.setEnabled(!currentlyChangingMode && (speaker || bluetooth));
            }

            bluetoothButton.setVisibility(bluetooth ? View.VISIBLE : View.GONE);
            if (bluetooth) {
                bluetoothButton.setChecked(mode == com.zello.sdk.AudioMode.BLUETOOTH);
                bluetoothButton.setEnabled(!currentlyChangingMode && (speaker || earpiece));
            }
        }

        audioModeView.setVisibility(speaker || earpiece || bluetooth ? View.VISIBLE : View.GONE);
    }

    private void onConnectChannelButtonPressed() {
        com.zello.sdk.ContactType type = selectedContact.getType();
        if (type == com.zello.sdk.ContactType.CHANNEL || type == com.zello.sdk.ContactType.GROUP) {
            com.zello.sdk.ContactStatus status = selectedContact.getStatus();
            if (status == com.zello.sdk.ContactStatus.OFFLINE) {
                Zello.connectChannel(selectedContact.getName());
            } else if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
                Zello.disconnectChannel(selectedContact.getName());
            }
        }
    }

    private void updateConnectChannelButton() {
        boolean showConnect = false, connected = false, canConnect = false;

        com.zello.sdk.ContactType type = selectedContact.getType();
        com.zello.sdk.ContactStatus status = selectedContact.getStatus();
        switch (type) {
            case USER:
            case GATEWAY:
            case CHANNEL:
            case GROUP: {
                showConnect = !selectedContact.getNoDisconnect();
                if (status == com.zello.sdk.ContactStatus.AVAILABLE) {
                    canConnect = true;
                    connected = true;
                } else if (status == com.zello.sdk.ContactStatus.OFFLINE) {
                    canConnect = true;
                } else if (status == com.zello.sdk.ContactStatus.CONNECTING) {
                    connected = true;
                }
                break;
            }
        }

        connectChannelButton.setEnabled(canConnect);
        connectChannelButton.setChecked(connected);
        connectChannelButton.setVisibility(showConnect ? View.VISIBLE : View.GONE);
    }

}
