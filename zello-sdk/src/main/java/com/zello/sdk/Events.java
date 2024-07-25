package com.zello.sdk;

import android.app.Activity;
import android.content.Context;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

/**
 * The <code>Events</code> interface enables monitoring of Zello SDK state and property changes
 */
@SuppressWarnings("unused")
public interface Events {

	/**
	 * <p>
	 * Called when the selected contact changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * To retrieve the newly selected contact, call the {@link Zello#getSelectedContact(Contact)} method.
	 * </p>
	 *
	 * @see Zello#getSelectedContact(Contact)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
	 * @see Zello#setSelectedContact(Contact)
	 * @see Zello#setSelectedUserOrGateway(String)
	 * @see Zello#setSelectedChannelOrGroup(String)
	 */
	void onSelectedContactChanged();

	/**
	 * <p>
	 * Called when the state of either the {@link MessageOut} or {@link MessageIn} changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * To retrieve the current message state, call the {@link Zello#getMessageIn(MessageIn)} and {@link Zello#getMessageOut(MessageOut)} methods.
	 * </p>
	 *
	 * @see Zello#getMessageIn(MessageIn)
	 * @see Zello#getMessageOut(MessageOut)
	 * @see Zello#beginMessage()
	 * @see Zello#endMessage()
	 */
	void onMessageStateChanged();

	/**
	 * <p>
	 * Called when the {@link AppState} changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * To retrieve the current <code>AppState</code>, call the {@link Zello#getAppState(AppState)} method.
	 * </p>
	 *
	 * @see Zello#getAppState(AppState)
	 */
	void onAppStateChanged();

	/**
	 * <p>
	 * Called when the last {@link Contacts} {@link Tab} changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * </p>
	 *
	 * @param tab The tab that changed.
	 * @see Zello#selectContact(String, Tab[], Tab, Theme)
	 * @see Zello#selectContact(String, Tab[], Tab, Theme, Activity)
	 */
	void onLastContactsTabChanged(@NonNull Tab tab);

	/**
	 * <p>
	 * Called when the {@link Contacts} for the user changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * To retrieve the current <code>Contacts</code> snapshot, call the {@link Zello#getContacts()} method.
	 * </p>
	 * <p>
	 * When dealing with large contact lists (1000+ contacts), the best approach is to run both <code>getContacts()</code>
	 * and any contact processing in a background thread and then post the result to UI thread for display.
	 * </p>
	 *
	 * @see Zello#getContacts()
	 */
	void onContactsChanged();

	/**
	 * <p>
	 * Called when the the state of the {@link Audio} changes.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * To retrieve the current <code>Audio</code>, call the {@link Zello#getAudio()} method.
	 * </p>
	 *
	 * @see Zello#getAudio()
	 */
	void onAudioStateChanged();

	/**
	 * <p>
	 * Called when an invocation of the {@link Zello#beginMessage()} method fails because the microphone permission hasn't been granted.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * </p>
	 * <p>
	 * The normal use case for handling this error would be to call {@link Zello#showMicrophonePermissionDialog()}.
	 * However, it is the responsibility of the app using the SDK to determine if they should handle this error or not.
	 * For example, if there are multiple apps using the SDK, they will all receive this callback. The {@link Zello#showMicrophonePermissionDialog()}
	 * method should likely only be on one of these apps (the one in the foreground).
	 * </p>
	 *
	 * @see Zello#showMicrophonePermissionDialog(Activity)
	 * @see Zello#showMicrophonePermissionDialog()
	 * @see Zello#beginMessage()
	 */
	void onMicrophonePermissionNotGranted();

	/**
	 * <p>
	 * Called when a Bluetooth accessory connection state changes.
	 * </p>
	 *
	 * @param type        Type of the device.
	 * @param state       New state of the device.
	 * @param name        Name of the device.
	 * @param description A description of the event that can be shown to the user.
	 * @see BluetoothAccessoryType
	 * @see BluetoothAccessoryState
	 */
	void onBluetoothAccessoryStateChanged(
			@NonNull BluetoothAccessoryType type, @NonNull BluetoothAccessoryState state,
			@Nullable String name, @Nullable String description);

	/**
	 * <p>
	 * Called when an attempt to start the Zello foreground service fails.
	 * </p>
	 * <p>
	 * This method is invoked on the UI thread.
	 * </p>
	 * <p>
	 * It is the responsibility of the app using the SDK to determine if and how to handle this error.
	 * The most likely cause is that the app connecting to Zello was not displaying any UI at the
	 * time when the foreground service was started. In this case, the implementing app may call
	 * {@link Zello#unconfigure()} followed by {@link Zello#configure(Context)}
	 * (or one of its overloads) once its UI is in the foreground to attempt to start the foreground
	 * service again.
	 * </p>
	 * <p>
	 * In Android 14 and above, requirements for foreground services have become stricter. For
	 * the most up-to-date information, please refer to the Android documentation.
	 * </p>
	 *
	 * @param t The exception that caused the failure if available.
	 */
	void onForegroundServiceStartFailed(@Nullable Throwable t);

}
