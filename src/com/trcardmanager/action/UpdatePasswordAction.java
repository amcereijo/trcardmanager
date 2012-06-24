package com.trcardmanager.action;

import java.io.IOException;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.CheckBox;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.exception.TRCardManagerUpdatePasswordException;
import com.trcardmanager.http.TRCardManagerHttpAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class UpdatePasswordAction extends AsyncTask<String, Void, Integer> {
	
	private final static String TAG = UpdatePasswordAction.class.getName();
	
	private static final int OK_RESULT_CODE = -1;
	
	private ProgressDialog loadingDialog;
	private Activity activity;
	private UserDao userDao;
	private int resultCode = OK_RESULT_CODE;
	
	
	public UpdatePasswordAction() {
		this.activity = TRCardManagerApplication.getActualActivity();
		this.userDao = TRCardManagerApplication.getUser();
	}
	
	@Override
	protected Integer doInBackground(String... params) {
		updatePassword(params[0]);
		return resultCode;
	}
	
	private void updatePassword(String newPassword){
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		try {
			httpAction.changePassword(userDao, newPassword);
			CheckBox checkSave = (CheckBox)activity.findViewById(R.id.update_password_savedb);
			//¿save in db?
			if(checkSave.isChecked() && userDao.isRememberme()){
				publishProgress();
				TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(activity.getApplicationContext());
				dbHelper.updateUserPassword(userDao, newPassword);
			}

			//TODO return a code to principal activity to repaint screen
			finalizeWithState(TRCardManagerApplication.PASSWORD_UPDATED);
		} catch (ClientProtocolException e) {
			Log.e(TAG,"Error updating password:"+e.getMessage(),e);
			resultCode = R.string.update_password_error;
		} catch (IOException e) {
			Log.e(TAG,"Error updating password:"+e.getMessage(),e);
			resultCode = R.string.update_password_error;
		} catch (TRCardManagerDataException e) {
			Log.e(TAG,"Error updating password:"+e.getMessage(),e);
			resultCode = R.string.update_password_error;
		} catch (TRCardManagerSessionException e) {
			Log.e(TAG,"Error updating card: "+e.getMessage(),e);
			finalizeWithState(TRCardManagerApplication.SESSION_EXPIRED_APPLICATION);
		} catch (TRCardManagerUpdatePasswordException e) {
			Log.e(TAG,"Error updating password:"+e.getMessage(),e);
			resultCode = e.getResourceIdError();
		}catch(Exception e){
			Log.e(TAG,"Error updating password:"+e.getMessage(),e);
			resultCode = R.string.update_password_error;
		}
	}
	
	private void finalizeWithState(int endState){
		Activity act = TRCardManagerApplication.getActualActivity();
		act.getParent().setResult(endState);
		act.getParent().finish();
	}
	
	private void showErrorDialog(int messageCode){
		AlertDialog.Builder builder = new AlertDialog.Builder(activity);
		builder.setMessage(messageCode)
		       .setCancelable(false)
		       .setPositiveButton(R.string.update_password_dialog_error_button,
		    		   new DialogInterface.OnClickListener() {
		           			public void onClick(DialogInterface dialog, int id) {
		           				dialog.cancel();
		           			}
		       });
		AlertDialog alert = builder.create();
		alert.show();
	}
	
	
	@Override
	protected void onPreExecute() {
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.update_password_dialog_title), 
		activity.getText(R.string.update_password_dialog_update_http));
	}

	@Override
	protected void onPostExecute(Integer result) {
		loadingDialog.cancel();
		if(result!=OK_RESULT_CODE){
			showErrorDialog(result);
		}
		
	}
	
	@Override
	protected void onProgressUpdate(Void... values) {		
		loadingDialog.setMessage(activity.getText(R.string.update_password_dialog_update_db));
	}
	
	

}
