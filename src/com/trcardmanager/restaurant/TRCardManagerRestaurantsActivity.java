package com.trcardmanager.restaurant;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsActivity extends Activity {
	
	final private static String TAG = TRCardManagerRestaurantsActivity.class.getName();
	
	private TRCardManagerLocationAction locationAction;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurants);
		setTitle(R.string.restaurants_title);
		TRCardManagerApplication.setActualActivity(this);
		locationAction = new TRCardManagerLocationAction();
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == 1024){
    		getPhisicalDirecction();
		}
	}
	
	
	public void showSearch(View v){
		showSearchSelectLayout(false);
		showSearchLayout(true);
	}
	
	public void findInLocation(View v){
		showSearchSelectLayout(false);
		findPlaces();
		showMinimizedSearchLayout(true);
	}
	
	public void search(View v){
		showSearchLayout(false);
		//TODO search
		showMinimizedSearchLayout(true);
	}
	
	public void showMoreSearch(View v){
		showMinimizedSearchLayout(false);
		showSearchLayout(true);
	}
	
	
	@Override
	public void onBackPressed() {
		LinearLayout selectSearchLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(selectSearchLayout.getVisibility() == LinearLayout.GONE){
			showSearchLayout(false);
			showMinimizedSearchLayout(false);
			showSearchSelectLayout(true);
		}else{
			super.onBackPressed();
		}
	}
	
	
	private void showSearchLayout(boolean show){
		RelativeLayout searchLayout = (RelativeLayout)findViewById(R.id.restaurants_search_layout);
		if(show){
			searchLayout.setVisibility(RelativeLayout.VISIBLE);
		}else{
			searchLayout.setVisibility(RelativeLayout.GONE);
		}
	}
	
	private void showMinimizedSearchLayout(boolean show){
		RelativeLayout searchLayout = (RelativeLayout)findViewById(R.id.restaurants_search_minimized_layout);
		if(show){
			searchLayout.setVisibility(RelativeLayout.VISIBLE);
		}else{
			searchLayout.setVisibility(RelativeLayout.GONE);
		}
	}
	
	private void showSearchSelectLayout(boolean show){
		LinearLayout searchSelectLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(show){
			searchSelectLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			searchSelectLayout.setVisibility(LinearLayout.GONE);
		}
	}
	
	private void findPlaces(){
    	if(!locationAction.isGpsActive()){
    		buildAlertMessageNoGps();
    	}else{
    		getPhisicalDirecction();
    	}
    }
    
	 private DirectionDao getPhisicalDirecction(){
    	DirectionDao phisicalDirecction = new DirectionDao();
    	Location location = locationAction.getActualLocation();
    	Log.i(TAG, "location: "+location);
    	Geocoder gc = new Geocoder(getApplicationContext(), Locale.getDefault());
        try {
			List<Address> addresses = gc.getFromLocation(location.getLatitude(), location.getLongitude(), 5);
			phisicalDirecction = getAddressDirection(addresses.get(0));
			TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
			httpAction.getRestaurants(phisicalDirecction);
			
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(),e);
		}
        return phisicalDirecction;
    }
	    
	 private DirectionDao getAddressDirection(Address address){
    	DirectionDao directionDao = new DirectionDao();
    		directionDao.setCountry(address.getCountryName());
    		directionDao.setPostalCode(address.getPostalCode());
    		directionDao.setArea(address.getAdminArea());
    		directionDao.setSubArea(address.getSubAdminArea());
    		directionDao.setLocality(address.getLocality());
    		directionDao.setStreet(address.getThoroughfare());
    		LocationDao locationDao = new LocationDao(address.getLongitude(), address.getLatitude());
    		directionDao.setLocation(locationDao);
    	return directionDao;
    }
	
    private void buildAlertMessageNoGps() {
        AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle("GPS");
		alert.setMessage("Yout GPS seems to be disabled, do you want to enable it?");
		alert.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),1024);
					}
				});
		alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int whichButton) {
				getPhisicalDirecction();
			}
		});
		alert.show();
    }
}
