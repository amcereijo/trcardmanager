package com.trcardmanager.action;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.AsyncTask;
import android.provider.Settings;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * Async action to get user location
 * @author angelcereijo
 *
 */
public class GeoDirectionAction extends AsyncTask<Void, Void, Void> {
	
	final private static String TAG = GeoDirectionAction.class.getName();
	
	private ProgressDialog loadingDialog;
	private Activity activity;
	private TRCardManagerLocationAction locationAction;
	private DirectionDao userDirection;
	private RestaurantSearchDao restaurantSearchDao;
	
	
	public GeoDirectionAction(){
		activity = TRCardManagerApplication.getActualActivity();
	}
	
	
	public GeoDirectionAction(RestaurantSearchDao restaurantSearchDao,TRCardManagerLocationAction locationAction){
		this();
		this.restaurantSearchDao = restaurantSearchDao;
		this.locationAction = locationAction;
	}
	
	
	private void loadingMessage(){
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.restaurants_dialog_location_title), 
				activity.getText(R.string.restaurants_dialog_location_message));
	}

	
	private void buildAlertMessageNoGps() {
        final AlertDialog.Builder alert = new AlertDialog.Builder(activity);
		alert.setTitle(R.string.restaurants_no_gps_title);
		alert.setMessage(R.string.restaurants_no_gps_message);
		alert.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						activity.startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
								TRCardManagerApplication.GPS_ACTIVATED);
					}
				});
		alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				getPhisicalDirecction();
			}
		});
		activity.runOnUiThread(new Runnable() {
		    public void run() {
		    	alert.show();
		    }
		});
		//alert.show();
    }
	
	
	@Override
	protected Void doInBackground(Void... params) {
		if(!locationAction.isGpsActive()){
    		buildAlertMessageNoGps();
    	}else{
    		getPhisicalDirecction();
    	}
		return null;
	}
	
	
	private void getPhisicalDirecction(){
		loadingMessage();
		userDirection = locationAction.getActualLocation(loadingDialog);
//		UserDao user = TRCardManagerApplication.getUser();
//		user.setUserLocation(userDirection);
		restaurantSearchDao.setDirectionDao(userDirection);
		loadingDialog.cancel();
    }
	    
	 

}
