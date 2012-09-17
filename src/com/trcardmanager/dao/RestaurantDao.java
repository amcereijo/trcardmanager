package com.trcardmanager.dao;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantDao extends DirectionDao {

	private String phoneNumber = "";
	private String foodType = "";
	private String retaurantName = "";
	
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
	
	
	
}
