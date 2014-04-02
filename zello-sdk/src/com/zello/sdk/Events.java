package com.zello.sdk;

public interface Events {

	public void onSelectedContactChanged();

	public void onMessageStateChanged();

	public void onAppStateChanged();

	public void onLastContactsTabChanged(Tab tab);

	public void onContactsChanged();

}
