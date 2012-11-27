package com.trcardmanager.restaurant;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.OverlayItem;
import com.trcardmanager.dao.RestaurantDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantOverlayItemDao extends OverlayItem {
	
	private RestaurantDao restaurantDao;
	
	public RestaurantOverlayItemDao(GeoPoint point, String title, String snippet) {
		super(point, title, snippet);
		// TODO Auto-generated constructor stub
	}

	public RestaurantOverlayItemDao(GeoPoint point, String title, String snippet,
			RestaurantDao restaurantDao) {
		this(point, title, snippet);
		setRestaurantDao(restaurantDao);
	}
	
	public void setRestaurantDao(RestaurantDao restaurantDao) {
		this.restaurantDao = restaurantDao;
	}
	public RestaurantDao getRestaurantDao() {
		return restaurantDao;
	}
	
}
