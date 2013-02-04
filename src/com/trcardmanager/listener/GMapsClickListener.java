package com.trcardmanager.listener;

import android.content.Intent;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;

public class GMapsClickListener implements OnClickListener{
	
	private final static String URI_TO_OPEN_MAPS = "http://maps.google.com/maps?z=%d&q=%s";
	private final static int ZOOM_LEVEL = 18; 
	
	private RestaurantDao restaurantDao;
	
	/**
	 * 
	 * @param restaurantDao
	 */
	public GMapsClickListener(RestaurantDao restaurantDao){
		this.restaurantDao = restaurantDao;
	}
	
	public void onClick(View v) {
		String uri = String.format(URI_TO_OPEN_MAPS,
				ZOOM_LEVEL,restaurantDao.getRestaurantDisplayDirection());
		TRCardManagerApplication.getActualActivity().startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
	}
}
