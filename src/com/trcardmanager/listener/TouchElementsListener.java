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
	private String downColor = DOWN_COLOR;
	
	public TouchElementsListener(){
		
	}
	
	/**
	 * 
	 * @param downColor Supported formats are: #RRGGBB #AARRGGBB 'red', 'blue', 'green', 'black', 'white', 'gray', 'cyan', 'magenta', 'yellow', 'lightgray', 'darkgray'
	 */
	public TouchElementsListener(String downColor){
		this.downColor = downColor;
	}
	
	
	public boolean onTouch(View v, MotionEvent event) {
		touchElement = (T)v;
		if(event.getAction() == MotionEvent.ACTION_DOWN){
			originalState = touchElement.getBackground();
			touchElement.setBackgroundColor(Color.parseColor(downColor));
		}else if(event.getAction() == MotionEvent.ACTION_UP){
			touchElement.setBackgroundDrawable(originalState);
		}
		return false;
	}

}
