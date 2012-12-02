package com.trcardmanager.application;

import android.app.Activity;
import android.app.Application;

import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.UserDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerApplication extends Application {
	private final static String TAG =TRCardManagerApplication.class.toString();
	
	public static int BACK_EXIT_APPLICATION = 99;
	public static int SESSION_EXPIRED_APPLICATION = 101;
	public static int MY_ACCOUNT_CLOSED = 103;
	public static int CARD_UPDATED = 105;
	public static int PASSWORD_UPDATED = 107;
	public static int SESSION_CLOSED = 108;
	public static int GPS_ACTIVATED = 109;
	public static int SEARCH_RESTAURANTS_FINISHED = 110;
	public static int SEARCH_RESTAURANTS_MAP_TO_LIST = 111;
	public static int SEARCH_RESTAURANTS_MAP_TO_LIST_BACK_RESULT = -111;
	public static int SEARCH_RESTAURANTS_LIST_TO_MAP = 112;
	public static int SEARCH_RESTAURANTS_LIST_TO_MAP_BACK_RESULT = -112;
	
	private static UserDao user;
	private static Activity actualActivity;
	private static boolean loadingInfo = Boolean.FALSE;
	private static RestaurantSearchDao restaurantSearchDao;
	
	public static void setUser(UserDao user) {
		TRCardManagerApplication.user = user;
	}
	public static UserDao getUser() {
		return user;
	}
	
	public static void setActualActivity(Activity actualActivity) {
		TRCardManagerApplication.actualActivity = actualActivity;
	}
	public static Activity getActualActivity() {
		return actualActivity;
	}
	
	public static void setLoadingInfo(boolean loadingInfo) {
		TRCardManagerApplication.loadingInfo = loadingInfo;
	}
	public static boolean isLoadingInfo() {
		return loadingInfo;
	}
	
	public static void setRestaurantSearchDao(
			RestaurantSearchDao restaurantSearchDao) {
		TRCardManagerApplication.restaurantSearchDao = restaurantSearchDao;
	}
	public static RestaurantSearchDao getRestaurantSearchDao() {
		if(restaurantSearchDao == null){
			TRCardManagerApplication.restaurantSearchDao = new RestaurantSearchDao();
		}
		return restaurantSearchDao;
	}
	
}
