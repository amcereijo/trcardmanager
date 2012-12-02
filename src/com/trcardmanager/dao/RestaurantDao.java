package com.trcardmanager.dao;

import java.util.regex.Pattern;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantDao extends DirectionDao {
	
	private String phoneNumber = "";
	private String foodType = "";
	private String retaurantName = "";
	private String restaurantLink = "";
	
	private boolean completeDataLoaded = Boolean.FALSE;
	
	public String getPhoneNumber() {
		return phoneNumber;
	}
	public void setPhoneNumber(String phoneNumber) {
		this.phoneNumber = phoneNumber;
	}
	public String getFoodType() {
		return foodType;
	}
	public void setFoodType(String foodType) {
		this.foodType = foodType;
	}
	public String getRetaurantName() {
		return retaurantName;
	}
	public void setRetaurantName(String retaurantName) {
		this.retaurantName = retaurantName;
	}
	
	public void setRestaurantLink(String restaurantLink) {
		this.restaurantLink = restaurantLink;
	}
	public String getRestaurantLink() {
		return restaurantLink;
	}
	
	public void setCompleteDataLoaded(boolean completeDataLoaded) {
		this.completeDataLoaded = completeDataLoaded;
	}
	public boolean isCompleteDataLoaded() {
		return completeDataLoaded;
	}
	
	public String getRestaurantDisplayDirection(){
		String displayDirection = new StringBuffer()
			.append(getStreet())
			.append(", ")
			.append(getLocality())
			.append(", ")
			.append(getSubArea())
			.append(", ")
			.append(getPostalCode()).toString();
		displayDirection = displayDirection.replaceAll(Pattern.quote(", null"), "");
		displayDirection = displayDirection.replaceAll(Pattern.quote(", ,"), "");
		return displayDirection;
	}
	
	
}
