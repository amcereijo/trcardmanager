package com.trcardmanager.dao;

public class CardDao {
	
	private long id;
	private String cardNumber; 
	private String balance = "0,00";
	
	public CardDao(String cardNumber) {
		setCardNumber(cardNumber);
	}
	
	
	public CardDao(long id, String cardNumber,String balance) {
		setCardNumber(cardNumber);
		setId(id);
		setBalance(balance);
	}
	
	public void setId(long id) {
		this.id = id;
	}
	public long getId() {
		return id;
	}
	
	public void setCardNumber(String cardNumber) {
		this.cardNumber = cardNumber;
	}
	public String getCardNumber() {
		return cardNumber;
	}
	
	public void setBalance(String balance) {
		this.balance = balance==null?"":balance;
	}
	public String getBalance() {
		return balance;
	}

}
