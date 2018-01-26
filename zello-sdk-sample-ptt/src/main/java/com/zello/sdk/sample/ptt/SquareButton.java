package com.zello.sdk.sample.ptt;

import android.content.Context;
import android.support.v7.widget.AppCompatButton;
import android.util.AttributeSet;

public class SquareButton extends AppCompatButton {

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
	public void setMaxWidth(final int maxWidth) {
		super.setMaxWidth(maxWidth);
		post(new Runnable() {
			@Override
			public void run() {
				_maxWidth = maxWidth;
				update();
			}
		});
	}

	@Override
	public void setMaxHeight(final int maxHeight) {
		super.setMaxHeight(maxHeight);
		post(new Runnable() {
			@Override
			public void run() {
				_maxHeight = maxHeight;
				update();
			}
		});
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		int measuredWidth = Math.min(getDefaultSize(Integer.MAX_VALUE, widthMeasureSpec), _maxWidth);
		int measuredHeight = Math.min(getDefaultSize(Integer.MAX_VALUE, heightMeasureSpec), _maxHeight);
		int min = Math.min(measuredHeight, measuredWidth);
		setMeasuredDimension(min, min);
		invalidate();
	}

	private void update() {
		requestLayout();
		invalidate();
		setText(getText());
	}

}
