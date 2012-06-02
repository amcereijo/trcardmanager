package com.trcardmanager.action;

import java.io.IOException;

import android.os.AsyncTask;
import android.util.Log;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.http.TRCardManagerHttpAction;

/**
 * Action to get more card movements
 * @author angelcereijo
 *
 */
public class TRCardManagerLoadPaginationAction extends AsyncTask<Void, Void, Void> {
	
	private UserDao user;
	
	public TRCardManagerLoadPaginationAction(UserDao user) {
		this.user = user;
	}
	
	@Override
	protected void onPreExecute() {
		Log.i(this.getClass().toString(), "----Started to find more movements....");
		TRCardManagerApplication.setLoadingInfo(true);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		try {
			httpAction.getMoreMovements(user);
		} catch (IOException e) {
			Log.e(this.getClass().toString(), e.getMessage(),e);
		}
		return null;
	}
	
	@Override
	protected void onPostExecute(Void result) {
		TRCardManagerApplication.setLoadingInfo(false);
		Log.i(this.getClass().toString(), "---- Finished to find more movements....");
	}

}
