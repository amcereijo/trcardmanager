package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.AsyncTask;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.TRCardManagerActivity;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.http.TRCardManagerHttpAction;

public class TRCardManagerLoginAction extends AsyncTask<Void, Void, Integer>{
	
	private ProgressDialog loadingDialog;
	private UserDao userDao;
	private TRCardManagerDbHelper trCardManagerDbHelper;
	private Activity activity;
	private int loginCode;
	
	public TRCardManagerLoginAction(UserDao user) {
		this.activity = TRCardManagerApplication.getActualActivity();;
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
		if(R.string.login_error_connection_message == loginCode || R.string.login_error_message == loginCode){
			updateViewErrorLogin(loginCode);
		}
	}

	@Override
	protected Integer doInBackground(Void... params) {
		doLogin();
		processLoginCode();
		return loginCode;
	}
	
	
	private void doLogin(){
		loginCode = -1;
		try{
			httpLogin();
			userInDb();
		}catch(TRCardManagerLoginException te){
			loginCode = R.string.login_error_message;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			loginCode = R.string.login_error_connection_message;
		} catch (IOException e) {
			e.printStackTrace();
			loginCode = R.string.login_error_connection_message;
		}
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
	
	private void processLoginCode(){
		if(!(R.string.login_error_connection_message == loginCode || R.string.login_error_message == loginCode)){
			this.publishProgress();
			loadUserData();
		}
	}
	
	private void loadUserData(){
		TRCardManagerApplication.setUser(userDao);
		getUserData();
		Intent settings = new Intent(activity, TRCardManagerActivity.class);
		activity.startActivityForResult(settings,TRCardManagerApplication.BACK_EXIT_APPLICATION);
	}
	
	
	
	private void getUserData(){
        if(!isDataReady(userDao)){
        	loadAndSaveUserData();
        }
	}
	
	private boolean isDataReady(UserDao user){
    	return (user!=null && user.getActualCard()!=null);
    }
    
    
    private void loadAndSaveUserData(){
    	try{
    		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
	        TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(activity);
	    	//http actions
			httpAction.getActualCard(userDao);
			httpAction.getActualCardBalanceAndMovements(userDao);
			//db actions
			dbHelper.addCard(userDao.getRowId(), userDao.getActualCard());
			dbHelper.updateCardBalance(userDao.getActualCard());
			dbHelper.findUserCards(userDao);
    	} catch (ClientProtocolException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (TRCardManagerDataException e) {
			e.printStackTrace();
		}
    }
	
	
	private void updateViewErrorLogin(int loginError){
		TextView errorTextView = (TextView)activity.findViewById(R.id.error_login_text_view);
		errorTextView.setText(loginError);
		 ((LinearLayout) activity.findViewById(R.id.error_login_layout)).setVisibility(View.VISIBLE);
	}
}
