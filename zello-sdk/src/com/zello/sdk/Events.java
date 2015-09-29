package com.zello.sdk;

public interface Events {

	void onSelectedContactChanged();

	void onMessageStateChanged();

	void onAppStateChanged();

	void onLastContactsTabChanged(Tab tab);

	void onContactsChanged();

}
