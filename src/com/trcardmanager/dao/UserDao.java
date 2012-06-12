package com.trcardmanager.dao;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * @author angelcereijo
 *
 */
public class UserDao {
	
	private long rowId = -1;
	private String email;
	private String password;
	private boolean rememberme;
	private String cookieValue;
	
	private List<CardDao> cards;
	private CardDao actualCard;
	
	public UserDao(String email, String password, boolean rememberme) {
		setEmail(email);
		setPassword(password);
		setRememberme(rememberme);
		cards = new ArrayList<CardDao>();
	}
	
	public void setActualCard(CardDao actualCard) {
		this.actualCard = actualCard;
	}
	public CardDao getActualCard() {
		return actualCard;
	}
	
	public void setCards(List<CardDao> cards) {
		this.cards = cards;
	}
	public List<CardDao> getCards() {
		return cards;
	}
	
	public void setRowId(long rowId) {
		this.rowId = rowId;
	}
	public long getRowId() {
		return rowId;
	}
	
	public void setCookieValue(String cookieValue) {
		this.cookieValue = cookieValue;
	}
	public String getCookieValue() {
		return cookieValue;
	}

	public String getEmail() {
		return email;
	}

	private void setEmail(String email) {
		this.email = email;
	}

	public String getPassword() {
		return password;
	}

	private void setPassword(String password) {
		this.password = password;
	}

	public boolean isRememberme() {
		return rememberme;
	}

	public void setRememberme(boolean rememberme) {
		this.rememberme = rememberme;
	}
	
	
}
