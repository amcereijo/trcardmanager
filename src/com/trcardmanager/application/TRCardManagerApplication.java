package com.trcardmanager.application;

import android.app.Activity;
import android.app.Application;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.preference.PreferenceManager;

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
	public static int CARD_UPDATED = 103;
	
	private static UserDao user;
	private static Activity actualActivity;
	private static boolean loadingInfo = Boolean.FALSE;
	
	
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
	
}
