package com.trcardmanager.settings;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.CheckBox;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;


/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerSettingsActivity extends Activity {

	final private static String TAG = TRCardManagerSettingsActivity.class.getName();
	
	private UserDao userDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.application_settings_layout);
		setTitle(R.string.settings_title);
		setSettingsValues();
	}

	private void setSettingsValues() {
		userDao = TRCardManagerApplication.getUser();
		CheckBox autoLoginCheck = (CheckBox)findViewById(R.id.settings_check_autologin);
		autoLoginCheck.setChecked(userDao.isAutologin());
		CheckBox askExitCheck = (CheckBox)findViewById(R.id.settings_check_ask_exit);
		askExitCheck.setChecked(userDao.isConfirmationClose());
	}
	
	/**
	 * 
	 * @param v
	 */
	public void onClickAutoupdate(View v) {
		CheckBox autoLoginCheck = (CheckBox)v;
		userDao.setAutologin(autoLoginCheck.isChecked());
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		dbHelper.updateUserAutoLogin(userDao);
	}
	
	/**
	 * 
	 * @param v
	 */
	public void onClickAskExit(View v){
		CheckBox askExitCheck = (CheckBox)v;
		userDao.setConfirmationClose(askExitCheck.isChecked());
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		dbHelper.updateUserConfirmationClose(userDao);
	}
}
