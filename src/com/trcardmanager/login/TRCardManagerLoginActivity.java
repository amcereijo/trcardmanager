package com.trcardmanager.login;

import android.app.Activity;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.TRCardManagerLoginAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerRecoverPasswordException;
import com.trcardmanager.http.TRCardManagerHttpUserAction;
import com.trcardmanager.listener.TouchElementsListener;

/**
 * Activity to login
 * @author angelcereijo
 *
 */
public class TRCardManagerLoginActivity extends Activity {
	
	private static final String TAG = TRCardManagerLoginActivity.class.getName(); 
	
	private UserDao user;
	private boolean showLogin = Boolean.TRUE;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.login);    
		TRCardManagerApplication.setActualActivity(this);
		
		Button loginButton = (Button)findViewById(R.id.btn_login_enter);
		loginButton.setOnTouchListener(new TouchElementsListener<Button>());
		
		findRemeberedUser();
		prepareLoginView();
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		if(showLogin){
			findActualUser();
			setContentView(R.layout.login);    
			fillUserFields();
		}
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
	
	
	private void prepareLoginView(){
		if(user!=null){
			fillUserFields();
			if(user.isAutologin()){
				doLogin(null);
			}
		}
	}
	
	
	
	private void fillUserFields(){
		this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		TextView emailTextView = (TextView)findViewById(R.id.login_email);
		emailTextView.setText(user.getEmail());
		TextView passTextView = (TextView)findViewById(R.id.login_password);
		passTextView.setText(user.getPassword());
		CheckBox checkRememberme = (CheckBox)findViewById(R.id.login_rememberme);
		checkRememberme.setChecked(user.isRememberme());
	}
	
	private void findActualUser(){
		TextView emailTextView = (TextView)findViewById(R.id.login_email);
		TextView passTextView = (TextView)findViewById(R.id.login_password);
		CheckBox checkRememberme = (CheckBox)findViewById(R.id.login_rememberme);
		user = new UserDao(emailTextView.getText().toString(),
				passTextView.getText().toString(),
				checkRememberme.isChecked(),
				checkRememberme.isChecked());
	}
	
	/**
	 * 
	 * @param v
	 */
	public void doLogin(View v) {
		new TRCardManagerLoginAction(getUserData()).execute();
	}
	
	
	/**
	 * 
	 */
	public void onActivityResult(int requestCode, int resultCode, Intent data) {
	    super.onActivityResult(requestCode, resultCode, data);
	    if(requestCode == TRCardManagerApplication.BACK_EXIT_APPLICATION){
	        if(resultCode == RESULT_CANCELED){
	            finish();
	        }else if(resultCode == TRCardManagerApplication.SESSION_EXPIRED_APPLICATION){
	        	Toast.makeText(getApplicationContext(), R.string.session_expired, Toast.LENGTH_LONG).show();
	        }else if(resultCode == TRCardManagerApplication.SESSION_CLOSED){
	        	clearLoginForm();
	        	removeRememberMe();
	        }
	    }
	}

	
	/**
	 * 
	 * @param v
	 */
	public void showRecoverPassword(View v){
		this.setContentView(R.layout.recover_password_layout);
		showLogin = Boolean.FALSE;
		Button loginButton = (Button)findViewById(R.id.btn_recover_password_enter);
		loginButton.setOnTouchListener(new TouchElementsListener<Button>());
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void doRecoverPassword(View v){
		final EditText emailEditText = (EditText) findViewById(R.id.recover_password_email);
		if(emailEditText.getText().toString() == null || "".equals(emailEditText.getText().toString())){
			Toast.makeText(getApplicationContext(), R.string.recover_password_error_no_email, Toast.LENGTH_LONG).show();
		}else{
			new Thread(new RecoverPasswordRunnable(emailEditText.getText().toString())).start();
		}
		
	}
	
	@Override
	public void onBackPressed() {
		if(!showLogin){
			setContentView(R.layout.login);
			showLogin = Boolean.TRUE;
		}else{
			this.finish();
		}
	}
	
	private void clearLoginForm() {
		TextView emailTextView = (TextView)findViewById(R.id.login_email);
		TextView passTextView = (TextView)findViewById(R.id.login_password);
		CheckBox checkRememberme = (CheckBox)findViewById(R.id.login_rememberme);
		emailTextView.setText("");
		passTextView.setText("");
		checkRememberme.setChecked(Boolean.FALSE);
	}
	
	private void removeRememberMe(){
		user.setRememberme(Boolean.FALSE);
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
		dbHelper.updateUserRemeberMe(user);
	}
		
	private UserDao getUserData(){
		String email = ((EditText)findViewById(R.id.login_email)).getText().toString();
		String password = ((EditText)findViewById(R.id.login_password)).getText().toString();
		boolean rememberme = ((CheckBox)findViewById(R.id.login_rememberme)).isChecked();
		UserDao user = new UserDao(email, password, rememberme,rememberme);
		return user;
	}
	
	
	private class RecoverPasswordRunnable implements Runnable{
		private String email;
		
		/**
		 * 
		 * @param email
		 */
		public RecoverPasswordRunnable(String email){
			this.email = email;
		}
		
		public void run() {
			runOnUiThread(new PrepareErrorRecoverViewRunnable());
			try{
				new TRCardManagerHttpUserAction().callRecoverPassword(email);
				runOnUiThread(new LoginViewRunnable());
			}catch (TRCardManagerRecoverPasswordException e) {
				runOnUiThread(new ErrorRecoverPassWordRunnable(e));
			}
		}
	}
	
	private class PrepareErrorRecoverViewRunnable implements Runnable{
		public void run() {
			Button loginButton = (Button)findViewById(R.id.btn_recover_password_enter);
			loginButton.setText(R.string.recover_password_btn_clicked);
			loginButton.setSelected(Boolean.TRUE);
		}
	}
	
	private class LoginViewRunnable implements Runnable{
		public void run() {
			setContentView(R.layout.login);
			showLogin = Boolean.TRUE;
			Toast t =  Toast.makeText(getApplicationContext(), R.string.recover_password_ok, Toast.LENGTH_LONG);
			t.setGravity(Gravity.CENTER, 0, 0);
			t.show();
		}
	}
	
	private class ErrorRecoverPassWordRunnable implements Runnable{
		TRCardManagerRecoverPasswordException error;
		public ErrorRecoverPassWordRunnable(TRCardManagerRecoverPasswordException e){
			this.error = e;
		}
		public void run() {
			TextView errorTextView = (TextView)findViewById(R.id.recover_password_error);
			errorTextView.setText(error.getResourceIdError());
			Button loginButton = (Button)findViewById(R.id.btn_recover_password_enter);
			final EditText emailEditText = (EditText) findViewById(R.id.recover_password_email);
			loginButton.setText(R.string.recover_password_btn);
			loginButton.setSelected(Boolean.FALSE);
			emailEditText.requestFocus();
		}
	}
}
