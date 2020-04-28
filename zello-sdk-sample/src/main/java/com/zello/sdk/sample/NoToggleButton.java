package com.zello.sdk.sample;

import android.content.Context;
import android.util.AttributeSet;

import androidx.appcompat.widget.AppCompatToggleButton;

public class NoToggleButton extends AppCompatToggleButton {

	public NoToggleButton(Context context, AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
	}

	public NoToggleButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public NoToggleButton(Context context) {
		super(context);
	}

	@Override
	public void toggle() {
		// Do not change the checked state
	}

}
