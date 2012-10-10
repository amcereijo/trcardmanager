package com.trcardmanager.location;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerLocationAction implements LocationListener {
	
	final private static String TAG = TRCardManagerLocationAction.class.getName();
	
	private LocationManager locationManager;
	private boolean gpsActive;
	private Location actualLocation;

	public TRCardManagerLocationAction(){
		Activity activity = TRCardManagerApplication.getActualActivity();
		locationManager = (LocationManager)activity.getSystemService(Context.LOCATION_SERVICE);
		locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 0, 0, this);
		locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
		gpsActive = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
	}

	
	public DirectionDao getLocationFromAddress(String address) throws IOException{
		Geocoder geoCoder = new Geocoder(TRCardManagerApplication.getActualActivity());
		DirectionDao phisicalDirecction = null;
		List<Address> addresses = geoCoder.getFromLocationName(address, 5);
		phisicalDirecction = getAddressDirection(addresses.get(0));
		return phisicalDirecction;
	}
	
	
	
	public DirectionDao getActualLocation(ProgressDialog processDialog) throws InterruptedException{
		do{
			actualLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			if(!gpsActive || ( gpsActive && actualLocation==null)){
				actualLocation = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			}
		}while(actualLocation==null);
		
		DirectionDao phisicalDirecction = locateAddressFromLocation();
		
		return phisicalDirecction;
	}

	private DirectionDao locateAddressFromLocation() {
		DirectionDao phisicalDirecction = new DirectionDao();
    	Geocoder gc = new Geocoder(TRCardManagerApplication.getActualActivity().getApplicationContext(),
    			Locale.getDefault());
        try {
			List<Address> addresses = gc.getFromLocation(actualLocation.getLatitude(), actualLocation.getLongitude(), 5);
			phisicalDirecction = getAddressDirection(addresses.get(0));
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
			gpsActive = true;
		}

	}

	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.i("","");
	}
    
	public boolean isGpsActive(){
		return gpsActive;
	}
}
