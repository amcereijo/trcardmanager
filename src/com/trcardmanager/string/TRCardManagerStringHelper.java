package com.trcardmanager.string;

import com.trcardmanager.exception.TRCardManagerDataException;

public class TRCardManagerStringHelper {
	
	private String content;
	
	public TRCardManagerStringHelper(String string) {
		content = string;
	}
	
	public String getStringBetwen(String startString, String endString) throws TRCardManagerDataException{
		int startPosition = content.indexOf(startString);
		startPosition += startString.length();
        int endPosition = content.indexOf(endString, startPosition);
        String foundString = content.substring(startPosition,endPosition);
		return foundString;
	}
}
