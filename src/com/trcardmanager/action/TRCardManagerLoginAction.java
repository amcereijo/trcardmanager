package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.TRCardManagerActivity;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.http.TRCardManagerHttpAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerLoginAction extends AsyncTask<Void, Void, Integer>{
	
	private final static String TAG = TRCardManagerLoginAction.class.getName();
	
	private final static int LOGIN_CODE_OK = -1;
	
	private ProgressDialog loadingDialog;
	private UserDao userDao;
	private TRCardManagerDbHelper trCardManagerDbHelper;
	private Activity activity;
	private int loginCode;
	
	public TRCardManagerLoginAction(UserDao user) {
		this.activity = TRCardManagerApplication.getActualActivity();
		this.userDao = user;
		this.trCardManagerDbHelper = new TRCardManagerDbHelper(activity);
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {		
		loadingDialog.setTitle(activity.getText(R.string.loadcards_load_dialog_title));
		loadingDialog.setMessage(activity.getText(R.string.loadcards_load_dialog_text));
	}
	
	@Override
	protected void onPreExecute() {
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.loggin_load_dialog_title), 
				activity.getText(R.string.loggin_load_dialog_text));
	}
	
	@Override
	protected void onPostExecute(Integer result) {
		loadingDialog.cancel();
		if(loginCode != LOGIN_CODE_OK){
			updateViewErrorLogin();
		}
	}

	@Override
	protected Integer doInBackground(Void... params) {
		loginCode = LOGIN_CODE_OK;
		try{
			httpLogin();
			userInDb();
			
			publishProgress();
			
			loadUserData();
		}catch(TRCardManagerLoginException te){
			Log.e(TAG, te.getMessage(),te);
			loginCode = R.string.login_error_message;
		} catch (ClientProtocolException e) {
			Log.e(TAG, e.getMessage(),e);
			loginCode = R.string.login_error_connection_message;
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(),e);
			loginCode = R.string.login_error_connection_message;
		}catch(TRCardManagerSessionException se){
			Log.e(TAG, se.getMessage(),se);
			loginCode = R.string.login_error_message;
		}catch(TRCardManagerDataException e){
			Log.e(TAG, e.getMessage(),e);
			loginCode = R.string.login_error_message;
		}catch(Exception e){
			Log.e(TAG, e.getMessage(),e);
			loginCode = R.string.login_error_connection_error;
		}
		return loginCode;
	}
	
	
	
	private void httpLogin() throws TRCardManagerLoginException, ClientProtocolException, IOException{
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		httpAction.getCookieLogin(userDao);
	}
	
	
	private void userInDb(){
		boolean rememberme = userDao.isRememberme();
		trCardManagerDbHelper.findUser(userDao);
		if(userDao.getRowId()!=-1 && rememberme != userDao.isRememberme()){
			userDao.setRememberme(rememberme);
			trCardManagerDbHelper.updateUserRemeberMe(userDao);
		}else if(userDao.getRowId()==-1){
			trCardManagerDbHelper.createUser(userDao);
		}
	}
	
	
	private void loadUserData() throws ClientProtocolException, IOException, 
			TRCardManagerDataException, TRCardManagerSessionException{
		TRCardManagerApplication.setUser(userDao);
		getUserData();
		if(loginCode == LOGIN_CODE_OK){
			Intent settings = new Intent(activity, TRCardManagerActivity.class);
			activity.startActivityForResult(settings,TRCardManagerApplication.BACK_EXIT_APPLICATION);
		}
	}
	
	
	
	private void getUserData() throws ClientProtocolException, IOException, 
			TRCardManagerDataException, TRCardManagerSessionException{
        if(!isDataReady(userDao)){
        	new UserDataAction().loadAndSaveUserData(userDao);
        }else{
        	loginCode = R.string.login_error_message;
        }
	}
	
	private boolean isDataReady(UserDao user){
    	return (user!=null && user.getActualCard()!=null);
    }
    
	
	private void updateViewErrorLogin(){
		TextView errorTextView = (TextView)activity.findViewById(R.id.error_login_text_view);
		errorTextView.setText(loginCode);
		errorTextView.setVisibility(View.VISIBLE);
	}
}
