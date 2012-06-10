package com.trcardmanager.dao;

/**
 * 
 * @author angelcereijo
 *
 */
public class MovementDao{
	private String operationId;
	private String date;
	private String hour;
	private String amount;
	private String trade;
	private String operationType;
	private String state;
	
	public void setOperationId(String operationId) {
		this.operationId = operationId;
	}
	public String getOperationId() {
		return operationId;
	}
	
	public String getDate() {
		return date;
	}
	public void setDate(String date) {
		this.date = date;
	}
	
	public String getHour() {
		return hour;
	}
	public void setHour(String hour) {
		this.hour = hour;
	}
	
	public String getAmount() {
		return amount;
	}
	public void setAmount(String amount) {
		this.amount = amount;
	}
	
	public String getTrade() {
		return trade;
	}
	public void setTrade(String trade) {
		this.trade = trade;
	}
	
	public String getOperationType() {
		return operationType;
	}
	public void setOperationType(String operationType) {
		this.operationType = operationType;
	}
	
	public String getState() {
		return state;
	}
	public void setState(String state) {
		this.state = state;
	}
	
}
