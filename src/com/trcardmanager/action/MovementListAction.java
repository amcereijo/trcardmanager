package com.trcardmanager.action;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.views.TRCardManagerListView;
import com.trcardmanager.views.TRCardManagerListView.ScrollDirection;

/**
 * 
 * @author angelcereijo
 *
 */
public class MovementListAction extends AsyncTask<Void, Void, Void>{
	
	private final static String TAG = MovementListAction.class.getName();
	
	private UserDao user = TRCardManagerApplication.getUser();
	
	private ArrayAdapter<MovementDao> adapter;
	private TRCardManagerListView linearMovements;
	private List<MovementDao> movements;
	private ScrollDirection scrollDirection;
	private TextView balanceView;
	
	private boolean sessionActive = true;
	
	public MovementListAction(TRCardManagerListView linearMovements,
			ArrayAdapter<MovementDao> adapter,TextView balanceView) {
		this.linearMovements = linearMovements;
		this.adapter = adapter;
		this.balanceView = balanceView;
	}

	@Override
	protected Void doInBackground(Void... params) {
		scrollDirection = linearMovements.getScrollDirection(); 
		if( scrollDirection == ScrollDirection.UP){
    		movements = updateMovements();
    	}else{
    		movements = findMoreMovements();
    	}
		if(sessionActive){
			TRCardManagerApplication.getActualActivity().runOnUiThread(new MovementsListRunnable());
		}
		return null;
	}
	
	
	@Override
	protected void onPostExecute(Void result) {
		if(sessionActive){
			linearMovements.onRefreshComplete();
		}
		super.onPostExecute(result);
	}
	
	
    private List<MovementDao> updateMovements() {
    	List<MovementDao> myListItems = new ArrayList<MovementDao>();
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
    	try {
    		myListItems = httpAction.updateLastMovementsAndBalance(user);
    		if(myListItems == null || myListItems.size()==0){
    			myListItems = new ArrayList<MovementDao>();
			}
		}catch(TRCardManagerSessionException se){
			cancelAndCloseActualActivity();
    	}catch(Exception e){
			Log.e(this.getClass().toString(), e.getMessage(),e);
			myListItems = new ArrayList<MovementDao>();
		}
    	return myListItems;
    }
    
    
	private List<MovementDao> findMoreMovements() {
		List<MovementDao> myListItems = new ArrayList<MovementDao>();
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		try {
			myListItems = httpAction.getNextMovements(user);
		} catch (IOException e) {
			Log.e(this.getClass().toString(), e.getMessage(),e);
			myListItems = new ArrayList<MovementDao>();
		}catch(TRCardManagerSessionException se){
			cancelAndCloseActualActivity();
    	}catch (Exception e){
			Log.e(this.getClass().toString(), e.getMessage(),e);
			myListItems = new ArrayList<MovementDao>();
		}
		return myListItems;
	}

    
    private void cancelAndCloseActualActivity(){
		Activity act = TRCardManagerApplication.getActualActivity();
		act.setResult(TRCardManagerApplication.SESSION_EXPIRED_APPLICATION);
    	act.finish();
    	sessionActive = false;
    }
	
    /**
     * Runnable class to do things in background
     * @author angelcereijo
     *
     */
    private class MovementsListRunnable implements Runnable{
		public void run() {
			if(scrollDirection == ScrollDirection.UP){
        		updateNewMovementsInfo();
        	}else{
        		updateMoreMovements();
        	}
            adapter.notifyDataSetChanged();
		}

		private void updateMoreMovements() {
			if(movements != null && movements.size() > 0){
			    for(int i=0;i<movements.size();i++)
			    	adapter.add(movements.get(i));
			}
		}

		private void updateNewMovementsInfo() {
			UserDao user = TRCardManagerApplication.getUser();
			balanceView.setText(user.getActualCard().getBalance());
			
			int totalElements = adapter.getCount();
			for(int i=0;i<totalElements;i++){
				movements.add((MovementDao)adapter.getItem(i));
			}
			adapter.clear();
			updateMoreMovements();
		}
    }
}

