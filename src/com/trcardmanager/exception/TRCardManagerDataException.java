package com.trcardmanager.exception;

public class TRCardManagerDataException extends Exception {

	private static final long serialVersionUID = 1018396017533142740L;
	
	
	public TRCardManagerDataException() {
	}
	
	public TRCardManagerDataException(Throwable e) {
		super(e);
	}
	
	public TRCardManagerDataException(String message){
		super(message);
	}
}
