package com.trcardmanager.application;

import android.app.Activity;
import android.app.Application;
import android.text.GetChars;
import android.util.Log;

import com.trcardmanager.dao.UserDao;

public class TRCardManagerApplication extends Application {
	
	public static int BACK_EXIT_APPLICATION = 99;
	
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
		Log.i(TRCardManagerApplication.class.toString(), "---- SETED LOADING INFO TO " + loadingInfo);
		TRCardManagerApplication.loadingInfo = loadingInfo;
	}
	public static boolean isLoadingInfo() {
		return loadingInfo;
	}
	
}
