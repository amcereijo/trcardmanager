package com.trcardmanager.dao;

/**
 * 
 * @author angelcereijo
 *
 */
public class LocationDao {

	double longitude;
	double latitude;
	
	public LocationDao() {
	}
	
	public LocationDao(double longitude, double latitude) {
		setLongitude(longitude);
		setLatitude(latitude);
	}
	
	public double getLongitude() {
		return longitude;
	}
	public void setLongitude(double longitude) {
		this.longitude = longitude;
	}
	public double getLatitude() {
		return latitude;
	}
	public void setLatitude(double latitude) {
		this.latitude = latitude;
	}
	
	
}
