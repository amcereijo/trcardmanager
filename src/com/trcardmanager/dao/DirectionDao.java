package com.trcardmanager.dao;

/**
 * 
 * @author angelcereijo
 *
 */
public class DirectionDao {

	private LocationDao location;
	private String street = "";
	private String postalCode = "";
	private String country = "";
	private String area = "";	//In spain "Comunidad autonoma"
	private String subArea = ""; //In spain "provincia"
	private String locality = "";
	
	public LocationDao getLocation() {
		return location;
	}
	public void setLocation(LocationDao location) {
		this.location = location;
	}
	public String getStreet() {
		return street;
	}
	public void setStreet(String street) {
		this.street = street;
	}
	public String getPostalCode() {
		return postalCode;
	}
	public void setPostalCode(String postalCode) {
		this.postalCode = postalCode;
	}
	public String getCountry() {
		return country;
	}
	public void setCountry(String country) {
		this.country = country;
	}
	public String getArea() {
		return area;
	}
	public void setArea(String area) {
		this.area = area;
	}
	public String getSubArea() {
		return subArea;
	}
	public void setSubArea(String subArea) {
		this.subArea = subArea;
	}
	public String getLocality() {
		return locality;
	}
	public void setLocality(String locality) {
		this.locality = locality;
	}
	
	
}
