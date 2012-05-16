package com.trcardmanager.application;

import android.app.Activity;
import android.app.Application;

import com.trcardmanager.dao.UserDao;

public class TRCardManagerApplication extends Application {
	
	public static int BACK_EXIT_APPLICATION = 99;
	
	private static UserDao user;
	//private static Context context;
	private static Activity actualActivity;
	
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
	
	
//	public static Context getContext(){
//		return context;
//	}
//	public static void setContext(Context context) {
//		TRCardManagerApplication.context = context;
//	}
	
}
