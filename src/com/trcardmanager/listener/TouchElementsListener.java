package com.trcardmanager.listener;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;

/**
 * 
 * @author angelcereijo
 *
 * @param <T>
 */
public class TouchElementsListener<T extends View> implements OnTouchListener {
	
	private final static String DOWN_COLOR = "#E0E0F8";
	private T touchElement;
	private Drawable originalState;
	private int color = Color.parseColor(DOWN_COLOR);
	
	public TouchElementsListener(){
	}
	
	/**
	 * 
	 * @param downColor Supported formats are: #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
	 */
	public TouchElementsListener(String downColor){
		this.color = Color.parseColor(downColor);
	}
	
	
	/**
	 * 
	 * @param downColor
	 */
	public TouchElementsListener(int downColor){
		this.color = downColor;
		
	}
	
	
	@SuppressWarnings({ "unchecked", "deprecation" })
	public boolean onTouch(View v, MotionEvent event) {
		touchElement = (T)v;
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			originalState = touchElement.getBackground();
			touchElement.setBackgroundColor(color);
		}else{
			touchElement.setBackgroundDrawable(originalState);
		}
		return false;
	}

}
