package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.http.TRCardManagerHttpAction;

public class TRCardManagerLoadAndSaveDataAction {

	public UserDao getUserData(){
		UserDao user = TRCardManagerApplication.getUser();
        if(!isDataReady(user)){
        	loadAndSaveUserData(user);
        }
        return user;
	}
	
	private boolean isDataReady(UserDao user){
    	return (user!=null && user.getActualCard()!=null);
    }
    
    
    private void loadAndSaveUserData(UserDao user){
    	try{
    		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
	        TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(TRCardManagerApplication.getContext());
	    	//http actions
			httpAction.getActualCard(user);
			httpAction.getActualCardBalance(user);
			//db actions
			dbHelper.addCard(user.getRowId(), user.getActualCard());
			dbHelper.updateCardBalance(user.getActualCard());
			dbHelper.findUserCards(user);
    	} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TRCardManagerDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
}
