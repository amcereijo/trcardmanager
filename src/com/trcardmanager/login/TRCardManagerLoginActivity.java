package com.trcardmanager.login;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.TRCardManagerLoginAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;

/**
 * Activity to login
 * @author angelcereijo
 *
 */
public class TRCardManagerLoginActivity extends Activity {
	
	private static final String TAG = TRCardManagerLoginActivity.class.getName(); 
	
	private UserDao user;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.login);    
		 
		TRCardManagerApplication.setActualActivity(this);
		findRemeberedUser();
		fillUserFields();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		findActualUser();
		setContentView(R.layout.login);    
		fillUserFields();
		super.onConfigurationChanged(newConfig);
	}
	
	@Override
    protected void onRestart() {
		super.onRestart();
    	TRCardManagerApplication.setActualActivity(this);
    }
	
	
	private void findRemeberedUser(){
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		user = dbHelper.findRemeberedUser();
	}
	
	
	private void fillUserFields(){
		if(user!=null){
			TextView emailTextView = (TextView)findViewById(R.id.login_email);
			emailTextView.setText(user.getEmail());
			TextView passTextView = (TextView)findViewById(R.id.login_password);
			passTextView.setText(user.getPassword());
			CheckBox checkRememberme = (CheckBox)findViewById(R.id.login_rememberme);
			checkRememberme.setChecked(user.isRememberme());
		}
	}
	
	private void findActualUser(){
		TextView emailTextView = (TextView)findViewById(R.id.login_email);
		TextView passTextView = (TextView)findViewById(R.id.login_password);
		CheckBox checkRememberme = (CheckBox)findViewById(R.id.login_rememberme);
		user = new UserDao(emailTextView.getText().toString(),
				passTextView.getText().toString(),
				checkRememberme.isChecked());
	}
	
	
	public void doLogin(View v) {
		new TRCardManagerLoginAction(getUserData()).execute();
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == TRCardManagerApplication.BACK_EXIT_APPLICATION){
	        if(resultCode == RESULT_CANCELED){
	            finish();
	        }else if(resultCode == TRCardManagerApplication.SESSION_EXPIRED_APPLICATION){
	        	Toast.makeText(getApplicationContext(), R.string.session_expired, Toast.LENGTH_LONG).show();
	        }
	    }
	}
		
	private UserDao getUserData(){
		String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
		String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
		boolean rememberme = ((CheckBox)findViewById(R.id.login_rememberme)).isChecked();
		UserDao user = new UserDao(email, password, rememberme);
		return user;
	}
}
