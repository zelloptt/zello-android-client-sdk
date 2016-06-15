package com.zello.sdk.sample.contacts;

import android.app.Activity;
import android.os.Parcelable;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.zello.sdk.*;

public class ContactsActivity extends Activity implements com.zello.sdk.Events {

    private ListView contactsListView;
    private TextView statusTextView;
    private TextView selectedContactTextView;

    private com.zello.sdk.AppState appState = new com.zello.sdk.AppState();

    //region Lifecycle Methods

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_contacts);

        contactsListView = (ListView)findViewById(R.id.contactsListView);
        statusTextView = (TextView)findViewById(R.id.statusTextView);
        selectedContactTextView = (TextView)findViewById(R.id.selectedContactTextView);

        Zello.getInstance().configure("com.pttsdk", this, this);

        // Contact list pick handler
        contactsListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                ListAdapter adapter = (ListAdapter)contactsListView.getAdapter();
                if (adapter != null) {
                    com.zello.sdk.Contact contact = (com.zello.sdk.Contact)adapter.getItem(position);
                    if (contact != null) {
                        Zello.getInstance().setSelectedContact(contact);
                    }
                }
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Zello.getInstance().unsubscribeFromEvents(this);
        Zello.getInstance().unconfigure();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Zello.getInstance().leavePowerSavingMode();
    }

    @Override
    protected void onPause() {
        super.onPause();

        Zello.getInstance().enterPowerSavingMode();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        super.onPrepareOptionsMenu(menu);

        menu.clear();

        Zello.getInstance().getAppState(appState);
        if (appState.isAvailable() && !appState.isInitializing()) {
            getMenuInflater().inflate(R.menu.menu, menu);

            showMenuItem(menu, R.id.menu_select_contact, true);
        }

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.menu_select_contact: {
                // Activity title; optional
                String title = getResources().getString(R.string.select_contact_title);
                // Set of displayed tabs; required; any combination of RECENTS, USERS and CHANNELS
                com.zello.sdk.Tab[] tabs = new com.zello.sdk.Tab[]{com.zello.sdk.Tab.RECENTS, com.zello.sdk.Tab.USERS, com.zello.sdk.Tab.CHANNELS};
                // Initially active tab; optional; can be RECENTS, USERS or CHANNELS
                com.zello.sdk.Tab tab = Tab.RECENTS;
                // Visual theme; optional; can be DARK or LIGHT
                com.zello.sdk.Theme theme = com.zello.sdk.Theme.DARK;

                // Since Zello was initialized in the Activity, pass in this as Activity parameter
                Zello.getInstance().selectContact(title, tabs, tab, theme, this);
                break;
            }
        }

        return true;
    }

    //endregion

    private void updateContactList() {
        ListAdapter adapter = (ListAdapter)contactsListView.getAdapter();
        boolean newAdapter = false;
        if (adapter == null) {
            newAdapter = true;
            adapter = new ListAdapter();
        }
        adapter.setContacts(Zello.getInstance().getContacts());
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
        Zello.getInstance().getSelectedContact(selectedContact);

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
        Zello.getInstance().getAppState(appState);

        if (appState.isSignedIn()) {
            Helper.invalidateOptionsMenu(this);

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

    private void showMenuItem(Menu menu, int itemId, boolean show) {
        MenuItem item = menu.findItem(itemId);
        if (item != null) {
            item.setVisible(show);
        }
    }

}
