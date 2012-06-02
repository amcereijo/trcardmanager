package com.trcardmanager.login;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.HeaderViewListAdapter;
import android.widget.TextView;

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
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.login);    
		 
		TRCardManagerApplication.setActualActivity(this);
		
		fillRemeberedUser();
	}
	
	@Override
    protected void onRestart() {
		super.onRestart();
    	TRCardManagerApplication.setActualActivity(this);
    }
	
	private void fillRemeberedUser(){
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		UserDao user = dbHelper.findRemeberedUser();
		if(user!=null){
			TextView emailTextView = (TextView)findViewById(R.id.email);
			emailTextView.setText(user.getEmail());
			TextView passTextView = (TextView)findViewById(R.id.password);
			passTextView.setText(user.getPassword());
			CheckBox checkRememberme = (CheckBox)findViewById(R.id.rememberme);
			checkRememberme.setChecked(true);
		}
	}
	
	
	public void doLogin(View v) {
		new TRCardManagerLoginAction(getUserData()).execute();
	}
	
	
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == TRCardManagerApplication.BACK_EXIT_APPLICATION){
	        if(resultCode == RESULT_CANCELED){
	            finish();
	        }
	    }
	}
		
	private UserDao getUserData(){
		String email = ((EditText)findViewById(R.id.email)).getText().toString();
		String password = ((EditText)findViewById(R.id.password)).getText().toString();
		boolean rememberme = ((CheckBox)findViewById(R.id.rememberme)).isChecked();
		UserDao user = new UserDao(email, password, rememberme);
		return user;
	}
}
