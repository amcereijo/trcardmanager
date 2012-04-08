package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerLoginException;
import com.trcardmanager.http.TRCardManagerHttpAction;

public class TRCardManagerLoginAction {
	
	private UserDao userDao;
	private TRCardManagerDbHelper trCardManagerDbHelper;
	
	public TRCardManagerLoginAction(UserDao user) {
		userDao = user;
		trCardManagerDbHelper = new TRCardManagerDbHelper(TRCardManagerApplication.getContext());
	}
	
	public int doLogin(){
		int logCode = -1;
		try{
			httpLogin();
			userInDb();
		}catch(TRCardManagerLoginException te){
			logCode = R.string.login_error_message;
		} catch (ClientProtocolException e) {
			e.printStackTrace();
			logCode = R.string.login_error_connection_message;
		} catch (IOException e) {
			e.printStackTrace();
			logCode = R.string.login_error_connection_message;
		}
		return logCode;
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
	
}
