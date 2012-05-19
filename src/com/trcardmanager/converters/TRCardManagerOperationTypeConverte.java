package com.trcardmanager.converters;

import java.util.HashMap;
import java.util.Map;

public class TRCardManagerOperationTypeConverte {
	
	private final static String PLUS_SYMBOL = "+";
	private final static String MINUS_SYMBOL = "-";
	
	
	private final static Map<String, String> OPERATIONS_TYPES_MAPPER = new HashMap<String, String>();
	static{
		OPERATIONS_TYPES_MAPPER.put("0095-CARGA CUENTA",PLUS_SYMBOL);
		OPERATIONS_TYPES_MAPPER.put("0051-VENTA",MINUS_SYMBOL);
	}
	
	public static String getSymbolForOperation(String opertionDescription){
		String symbol = OPERATIONS_TYPES_MAPPER.get(opertionDescription);
		if(symbol == null){
			symbol = MINUS_SYMBOL;
		}
		return symbol;
	}
}
