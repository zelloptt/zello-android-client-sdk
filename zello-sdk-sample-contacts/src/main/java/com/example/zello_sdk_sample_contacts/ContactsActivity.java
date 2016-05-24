package com.example.zello_sdk_sample_contacts;

import android.app.Activity;
import android.os.Parcelable;
import android.os.Bundle;
import android.os.SystemClock;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zello.sdk.Tab;

public class ContactsActivity extends Activity implements com.zello.sdk.Events {

    private ListView contactsListView;
    private TextView statusTextView;
    private TextView selectedContactTextView;

    private com.zello.sdk.Sdk zelloSDK = new com.zello.sdk.Sdk();
    private com.zello.sdk.AppState appState = new com.zello.sdk.AppState();

    //region Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);

        contactsListView = (ListView)findViewById(R.id.contactsListView);
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        selectedContactTextView = (TextView)findViewById(R.id.selectedContactTextView);

        zelloSDK.onCreate("com.pttsdk", this, this);

        // Contact list pick handler
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter adapter = (ListAdapter)contactsListView.getAdapter();
                if (adapter != null) {
                    com.zello.sdk.Contact contact = (com.zello.sdk.Contact)adapter.getItem(position);
                    if (contact != null) {
                        zelloSDK.setSelectedContact(contact);
                    }
                }
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

    private void updateContactList() {
        ListAdapter adapter = (ListAdapter)contactsListView.getAdapter();
        boolean newAdapter = false;
        if (adapter == null) {
            newAdapter = true;
            adapter = new ListAdapter();
        }
        adapter.setContacts(zelloSDK.getContacts());
        Parcelable state = contactsListView.onSaveInstanceState();
        if (newAdapter) {
            contactsListView.setAdapter(adapter);
        } else {
            adapter.notifyDataSetChanged();
        }
        if (state != null) {
            contactsListView.onRestoreInstanceState(state);
        }
        contactsListView.setFocusable(adapter.getCount() > 0);
    }

    //region Zello SDK Events

    @Override
    public void onMessageStateChanged() {

    }

    @Override
    public void onSelectedContactChanged() {
        com.zello.sdk.Contact selectedContact = new com.zello.sdk.Contact();
        zelloSDK.getSelectedContact(selectedContact);

        String name = selectedContact.getDisplayName();
        if (name != null) {
            selectedContactTextView.setText("Selected Contact: " + selectedContact.getDisplayName());
        }
    }

    @Override
    public void onAudioStateChanged() {

    }

    @Override
    public void onContactsChanged() {
        updateContactList();
    }

    @Override
    public void onLastContactsTabChanged(Tab tab) {

    }

    @Override
    public void onAppStateChanged() {
        zelloSDK.getAppState(appState);

        if (appState.isLocked()) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(R.string.locked);
            contactsListView.setVisibility(View.INVISIBLE);
            selectedContactTextView.setVisibility(View.INVISIBLE);
        } else if (appState.isSignedIn()) {
            statusTextView.setVisibility(View.INVISIBLE);
            contactsListView.setVisibility(View.VISIBLE);
            selectedContactTextView.setVisibility(View.VISIBLE);

            updateContactList();
        } else if (appState.isSigningIn()) {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(R.string.sign_in_status_signing_in);
            contactsListView.setVisibility(View.INVISIBLE);
            selectedContactTextView.setVisibility(View.INVISIBLE);
        } else {
            statusTextView.setVisibility(View.VISIBLE);
            statusTextView.setText(R.string.sign_in_status_offline);
            contactsListView.setVisibility(View.INVISIBLE);
            selectedContactTextView.setVisibility(View.INVISIBLE);
        }
    }

    //endregion

}
