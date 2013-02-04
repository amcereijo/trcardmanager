package com.trcardmanager.listener;

import java.util.List;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.RestaurantDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class WazeClickListener implements OnClickListener{
	
	private static final String WAZE_APP_URL = "waze://?q=Hawaii";
	private static final String URI_TO_OPEN_WAZE_APP = "waze://?ll=%s,%s&navigate=yes";
	
	private RestaurantDao restaurantDao;
	
	/**
	 * 
	 * @param restaurantDao
	 */
	public WazeClickListener(RestaurantDao restaurantDao){
		this.restaurantDao = restaurantDao;
	}
	public void onClick(View v) {
		LocationDao location = restaurantDao.getLocation();
		String urlwaze = String.format(URI_TO_OPEN_WAZE_APP,location.getLatitude(),location.getLongitude());
		Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( urlwaze ) );
		TRCardManagerApplication.getActualActivity().startActivity(intent);
		
	}
	
	/**
	 * Check if Waze app is installed
	 * @return
	 */
	public static boolean isWazeInstalled(){
		 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( WAZE_APP_URL ) );
		 List<ResolveInfo> list = TRCardManagerApplication.getActualActivity()
		 	.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		 return (list.size()>0);  
	}
}