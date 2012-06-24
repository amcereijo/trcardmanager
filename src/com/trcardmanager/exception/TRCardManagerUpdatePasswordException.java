package com.trcardmanager.exception;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerUpdatePasswordException extends Exception {

	private static final long serialVersionUID = 2677584450431150926L;

	private String codeError = "0";
	private int resourceIdError;
	
	public TRCardManagerUpdatePasswordException() {
	}
	
	public TRCardManagerUpdatePasswordException(String codeError) {
		this.codeError = codeError;
	}
	
	@Override
	public String getMessage() {
		final int code = Integer.parseInt(this.codeError);
		String message = "";
		switch (code) {
			case 1: message = getStringFromResource(R.string.update_data_errors_user_not_exists);
				resourceIdError = R.string.update_data_errors_user_not_exists;
				break;
			case 2:message = getStringFromResource(R.string.update_data_errors_card_not_exist);
				resourceIdError = R.string.update_data_errors_card_not_exist;
				break;
			case 3:message = getStringFromResource(R.string.update_data_errors_card_associated);
				resourceIdError = R.string.update_data_errors_card_associated;
				break;
			case 4:message = getStringFromResource(R.string.update_data_errors_card_different_profile);
				resourceIdError = R.string.update_data_errors_card_different_profile;
				break;
			default: message = getStringFromResource(R.string.update_password_error);
				resourceIdError = R.string.update_password_error;
				break;
		}
		return message;
	}
	
	private String getStringFromResource(int idResource){
		return TRCardManagerApplication.getActualActivity().getText(idResource).toString();
	}
	
	public int getResourceIdError() {
		return resourceIdError;
	}
}
		