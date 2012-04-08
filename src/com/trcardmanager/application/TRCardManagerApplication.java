package com.trcardmanager.application;

import com.trcardmanager.dao.UserDao;

import android.app.Application;
import android.content.Context;

public class TRCardManagerApplication extends Application {

	private static UserDao user;
	private static Context context;
	
	public static void setUser(UserDao user) {
		TRCardManagerApplication.user = user;
	}
	public static UserDao getUser() {
		return user;
	}
	
	public static Context getContext(){
		return context;
	}
	public static void setContext(Context context) {
		TRCardManagerApplication.context = context;
	}
	
}
