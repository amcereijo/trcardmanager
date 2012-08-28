package com.trcardmanager.location;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;

import com.trcardmanager.application.TRCardManagerApplication;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerLocationAction implements LocationListener {
	
	private LocationManager locationManager;
	private boolean gpsActive;
	private Location actualLocation;
	
	public TRCardManagerLocationAction(){
		Activity activity = TRCardManagerApplication.getActualActivity();
		locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		checkGpsProvider();
		
	}
	
	private void checkGpsProvider(){
		gpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
		if(gpsActive){
			locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		}
	}
	
	public void gpsActivated(){
		gpsActive = true;
		actualLocation = null;
		getActualLocation();
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
	}
	
	public boolean isGpsActive(){
		return gpsActive;
	}
	
	public Location getActualLocation(){
		//Location location;
		if(actualLocation == null){
			if(gpsActive){
				actualLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			}else{
				actualLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}
		return actualLocation;
	}
	
	public void onLocationChanged(Location location) {
		this.actualLocation = location;
	}

	public void onProviderDisabled(String provider) {
		if(LocationManager.GPS_PROVIDER.equals(provider)){
			gpsActive = false;
		}

	}

	public void onProviderEnabled(String provider) {
		if(LocationManager.GPS_PROVIDER.equals(provider)){
			gpsActivated();
		}

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		// TODO Auto-generated method stub

	}
    
}
