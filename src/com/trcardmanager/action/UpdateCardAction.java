package com.trcardmanager.action;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;
import com.trcardmanager.http.TRCardManagerHttpAction;


/**
 * 
 * @author angelcereijo
 *
 */
public class UpdateCardAction extends AsyncTask<String, Void, Integer> {

	private final static String TAG = UpdateCardAction.class.getName();
	
	private ProgressDialog loadingDialog;
	private Activity activity;
	private UserDao userDao;
	private int resultCode = -1;
	
	public UpdateCardAction() {
		this.activity = TRCardManagerApplication.getActualActivity();
		this.userDao = TRCardManagerApplication.getUser();
	}
	
	@Override
	protected Integer doInBackground(String...cardsNumber) {
		updateCard(cardsNumber[0]);
		return resultCode;
	}
	
	
	private void updateCard(String cardNumber){
		CardDao cardDao = addNewCardInDb(cardNumber);
    	try {
    		if(cardDao != null){
    			//http active card
	    		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
				httpAction.activateCard(userDao, cardDao);
				
				publishProgress();
				
				//load data for new active card
				new UserDataAction().loadAndSaveUserData(userDao);
				
				//TODO return a code to principal activity to repaint screen
				finalizeWithState(TRCardManagerApplication.CARD_UPDATED);
    		}
		} catch (IOException e) {
			Log.e(TAG,"Error updating card: "+e.getMessage(),e);
			resultCode = R.string.activate_card_error;
		} catch (TRCardManagerUpdateCardException e) {
			Log.e(TAG,"Error updating card: "+e.getMessage(),e);
			resultCode = e.getResourceIdError();
		} catch (TRCardManagerDataException e) {
			Log.e(TAG,"Error updating card: "+e.getMessage(),e);
			resultCode = R.string.activate_card_error;
		}catch(TRCardManagerSessionException e){
			Log.e(TAG,"Error updating card: "+e.getMessage(),e);
			finalizeWithState(TRCardManagerApplication.SESSION_EXPIRED_APPLICATION);
		}
	}
	
	private void finalizeWithState(int endState){
		Activity act = TRCardManagerApplication.getActualActivity();
		act.getParent().setResult(endState);
		act.getParent().finish();
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {		
		loadingDialog.setTitle(activity.getText(R.string.loadcards_load_dialog_title));
		loadingDialog.setMessage(activity.getText(R.string.loadcards_load_dialog_text));
	}
	
	private void showErrorDialog(int messageCode){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(messageCode)
		       .setCancelable(false)
		       .setPositiveButton(R.string.update_card_dialog_error_button,
		    		   new DialogInterface.OnClickListener() {
		           			public void onClick(DialogInterface dialog, int id) {
		           				dialog.cancel();
		           			}
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	private CardDao addNewCardInDb(String cardNumber){
		CardDao cardDao = null;
		TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(activity.getApplicationContext());
    	cardDao = new CardDao(cardNumber);
    	dbHelper.addCard(userDao.getRowId(), cardDao);
    	return cardDao;
	}
	
	
	@Override
	protected void onPreExecute() {
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.update_card_loading_message_title), 
				activity.getText(R.string.update_card_loading_message_message));
	}

	@Override
	protected void onPostExecute(Integer result) {
		loadingDialog.cancel();
		if(result!=-1){
			showErrorDialog(result);
		}
		
	}
}
