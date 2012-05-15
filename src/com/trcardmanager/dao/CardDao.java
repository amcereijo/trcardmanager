package com.trcardmanager.dao;

import java.util.List;

public class CardDao {
	
	private long id;
	private String cardNumber; 
	private String balance = "0,00";
	
	private List<MovementDao> movements;
	
	public CardDao(String cardNumber) {
		setCardNumber(cardNumber);
	}
	
	
	public CardDao(long id, String cardNumber,String balance) {
		setCardNumber(cardNumber);
		setId(id);
		setBalance(balance);
	}
	
	public void setMovements(List<MovementDao> movements) {
		this.movements = movements;
	}
	public List<MovementDao> getMovements() {
		return movements;
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
	
	public CardDao getCopy() {
		return new CardDao(getId(), getCardNumber(), getBalance());
	}

}
