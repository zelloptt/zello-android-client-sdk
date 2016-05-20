package com.example.zello_sdk_sample_ptt;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Button;

public class SquareButton extends Button {

	private int _maxWidth = Integer.MAX_VALUE;
	private int _maxHeight = Integer.MAX_VALUE;

	public SquareButton(Context context) {
		super(context);
	}

	public SquareButton(Context context, AttributeSet attrs) {
		super(context, attrs);
	}

	public SquareButton(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
	}

	@Override
	public void setMaxWidth(int maxWidth) {
		super.setMaxWidth(maxWidth);
		if (_maxWidth != maxWidth) {
			_maxWidth = maxWidth;
			requestLayout();
		}
	}

	@Override
	public void setMaxHeight(int maxHeight) {
		super.setMaxHeight(maxHeight);
		if (_maxHeight != maxHeight) {
			_maxHeight = maxHeight;
			requestLayout();
		}
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = Math.min(getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec), _maxWidth);
		int measuredHeight = Math.min(getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec), _maxHeight);
		int min = Math.min(measuredHeight, measuredWidth);
		setMeasuredDimension(min, min);
	}

}
