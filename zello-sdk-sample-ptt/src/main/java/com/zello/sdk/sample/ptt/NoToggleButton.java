package com.zello.sdk.sample.ptt;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.ToggleButton;

public class NoToggleButton extends ToggleButton {

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
