package com.trcardmanager.listener;

import java.util.List;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.view.View;
import android.view.View.OnClickListener;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;
/**
 * 
 * @author angelcereijo
 *
 */
public class GNavClickListener implements OnClickListener{
	
	private static final String GOOGLE_NAV_APP_URL = "google.navigation:q=New+York+NY";
	
	private static final String URI_TO_OPEN_GOOGLE_NAV_ = "google.navigation:mode=%s&q=%s";
	private static final String GOOGLE_NAV_MODE_WALK = "walking";
	private static final String GOOGLE_NAV_MODE_DRIVE = "driving";
	
	private RestaurantDao restaurantDao;
	private String mode = GOOGLE_NAV_MODE_WALK; 
	
	/**
	 * 
	 * @param restaurantDao
	 */
	public GNavClickListener(RestaurantDao restaurantDao){
		this.restaurantDao = restaurantDao;
	}
	
	
	public void onClick(View v) {
		Context ctx = TRCardManagerApplication.getActualActivity();
		AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
		alert.setMessage(String.format(ctx.getString(R.string.gnavigation_dialog_open_message),
				restaurantDao.getRetaurantName()));
		alert.setPositiveButton(R.string.gnavigation_dialog_open_walking,
			new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					mode = GOOGLE_NAV_MODE_WALK;
					openGNavigation();
				}
			});
		alert.setNegativeButton(R.string.gnavigation_dialog_open_driving,
				new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				mode = GOOGLE_NAV_MODE_DRIVE;
				openGNavigation();
			}
		});
		alert.show();
	}
	
	
	private void openGNavigation(){
		String urlGNavigation = String.format(URI_TO_OPEN_GOOGLE_NAV_,mode,
				restaurantDao.getRestaurantDisplayDirection());
		Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( urlGNavigation ) );
		TRCardManagerApplication.getActualActivity().startActivity(intent);
	}
	
	/**
	 * Check if GMaps app is installed
	 * @return
	 */
	public static boolean isGNavigationInstalled(){
		 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( GOOGLE_NAV_APP_URL ) );
		 List<ResolveInfo> list = TRCardManagerApplication.getActualActivity()
		 	.getPackageManager().queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY);
		 return (list.size()>0);  
	}
}
