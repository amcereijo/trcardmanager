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
		setSettingsValues();
	}

	private void setSettingsValues() {
		userDao = TRCardManagerApplication.getUser();
		CheckBox autoLoginCheck = (CheckBox)findViewById(R.id.settings_check_autologin);
		autoLoginCheck.setChecked(userDao.isAutologin());
		
	}
	
	public void onClickAutoupdate(View v) {
		CheckBox autoLoginCheck = (CheckBox)v;
		userDao.setAutologin(autoLoginCheck.isChecked());
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		dbHelper.updateUserAutoLogin(userDao);
	}
}
