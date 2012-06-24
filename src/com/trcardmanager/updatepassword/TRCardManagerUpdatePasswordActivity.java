package com.trcardmanager.updatepassword;

import org.jsoup.helper.StringUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.UpdatePasswordAction;
import com.trcardmanager.application.TRCardManagerApplication;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerUpdatePasswordActivity extends Activity {

	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.update_password_title);
		setContentView(R.layout.update_password_layout);
		TRCardManagerApplication.setActualActivity(this);
	}
	
	
	public void onClickUpdatePasswordButton(View v){
		String newPass = getPassWordTyped(R.id.update_password_text);
		String newPassConfirm = getPassWordTyped(R.id.update_password_text_confirm);
		if(StringUtil.isBlank(newPass)){
			//error pass
			showErrorUpdatePassword(R.string.update_password_error_pass);
			setFocusOnEditTextResouce(R.id.update_password_text);
		}else if(StringUtil.isBlank(newPassConfirm)){
			//error confirm pass
			showErrorUpdatePassword(R.string.update_password_error_pass_confirm);
			setFocusOnEditTextResouce(R.id.update_password_text_confirm);
		}else if(!newPass.equals(newPassConfirm)){
			//error equal pass
			showErrorUpdatePassword(R.string.update_password_error_equal_passs);
			setFocusOnEditTextResouce(R.id.update_password_text_confirm);
		}else {
			updateUserPassword(newPass);
		}
			
	}
	
	private void updateUserPassword(String newPass){
		new UpdatePasswordAction().execute(newPass);
	}

	
	@Override
	protected void onResume() {
		super.onResume();
		TRCardManagerApplication.setActualActivity(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		String newPass = getPassWordTyped(R.id.update_password_text);
		String newPassConfirm = getPassWordTyped(R.id.update_password_text_confirm);
		setContentView(R.layout.update_password_layout);
		setPassWordSaved(R.id.update_password_text, newPass);
		setPassWordSaved(R.id.update_password_text_confirm, newPassConfirm);
		TRCardManagerApplication.setActualActivity(this);
		super.onConfigurationChanged(newConfig);
		TRCardManagerApplication.setActualActivity(this);
	}
	
	private String getPassWordTyped(int resource){
		EditText editText = (EditText)findViewById(resource);
		return editText.getText().toString();
	}
	
	private void setPassWordSaved(int resource, String pass){
		EditText editText = (EditText)findViewById(resource);
		editText.setText(pass);
	}
	
	private void showErrorUpdatePassword(int messageResource){
		Toast.makeText(getApplicationContext(), messageResource, Toast.LENGTH_LONG).show();
	}
	
	private void setFocusOnEditTextResouce(int editTextResource){
		((EditText)findViewById(editTextResource)).requestFocus();
	}
	
}
