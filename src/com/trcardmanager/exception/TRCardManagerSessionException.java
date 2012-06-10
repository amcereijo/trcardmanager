package com.trcardmanager.exception;

/**
 * Exception to not logged or session expired
 * @author angelcereijo
 *
 */
public class TRCardManagerSessionException extends Exception {

	private static final long serialVersionUID = 4654971455664506646L;

	public TRCardManagerSessionException() {
	}
	
	public TRCardManagerSessionException(Throwable t){
		super(t);
	}
	
	public TRCardManagerSessionException(String message){
		super(message);		
	}

}
