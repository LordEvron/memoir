package com.devapp.memoir;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.util.DisplayMetrics;
import android.view.Display;
import android.view.WindowManager;
import android.widget.ViewFlipper;

public class AViewFlipper extends ViewFlipper {

	Paint paint = new Paint();
	int mWidth = 0, mHeight = 0;

	public AViewFlipper(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected void dispatchDraw(Canvas canvas)
	{
		super.dispatchDraw(canvas);
		int width = getWidth();
		
		float margin = 2;
		float radius = 10;
		float cx = width / 2 - ((radius + margin) * 2 * getChildCount() / 2);
		float cy = getHeight()- 15;
		
		canvas.save();
		for (int i = 0; i < getChildCount(); i++)
		{
		if (i == getDisplayedChild())
		{
		paint.setColor(Color.CYAN);
		canvas.drawCircle(cx, cy, radius, paint);
		
		} else
		{
		paint.setColor(Color.GRAY);
		canvas.drawCircle(cx, cy, radius, paint);
		}
		cx += 2 * (radius + margin);
		}
		canvas.restore();
	}
}