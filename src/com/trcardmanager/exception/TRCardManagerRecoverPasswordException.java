package com.trcardmanager.exception;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;

/**
 * Exceptions when call recover user password
 * @author angelcereijo
 *
 */
public class TRCardManagerRecoverPasswordException extends Exception {

	private static final long serialVersionUID = -3042268276531678761L;
	
	public final static int BAD_USER = 1;
	public final static int CONECTION_ERROR = 3;
	public final static int UNKNOWN_ERROR = 0;
	
	private int codeError = BAD_USER;
	private int resourceIdError;
	
	public TRCardManagerRecoverPasswordException(int error) {
		this.codeError = error;
		setResourceIdError();
	}
	
	private void setResourceIdError() {
		switch (codeError) {
		case BAD_USER:
			this.resourceIdError = R.string.recover_password_exception_user;
			break;
		case CONECTION_ERROR:
			this.resourceIdError = R.string.recover_password_exception_connection;
			break;
		default: //UNKNOW ERROR
			this.resourceIdError = R.string.recover_password_exception_other;
			break;
		}
	}
	
	
	@Override
	public String getMessage() {
		return getStringFromResource();
	}
	
	/**
	 * 
	 * @return
	 */
	public int getResourceIdError() {
		return resourceIdError;
	}
	
	/**
	 * 
	 * @return
	 */
	public String getStringFromResource(){
		return TRCardManagerApplication.getActualActivity().getText(resourceIdError).toString();
	}
	
}
