package com.trcardmanager.db;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteDatabase.CursorFactory;
import android.database.sqlite.SQLiteOpenHelper;

import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerDbHelper extends SQLiteOpenHelper {
	
	//boolean = integers 0 (false) and 1 (true).
	private static final int VALUE_OF_TRUE = 1;
	private static final int VALUE_OF_FALSE = 0;
	//the rowid is like a id of user
	
	private static final int DATABASE_VERSION = 9;
	private static final String DATABASE_NAME = "trcardmanager";
	private static final String USER_TABLE_NAME = "table_users";
	private static final String CARD_TABLE_NAME = "table_cards";
	
	private static final String FIELD_BALANCE = "balance";
	private static final String FIELD_CARDNUMBER = "cardnumber";
	private static final String FIELD_USERID = "userid";
	private static final String FIELD_AUTOLOGIN = "autologin";
	private static final String FIELD_REMEMBERME = "rememberme";
	private static final String FIELD_PASSWORD = "password";
	private static final String FIELD_EMAIL = "email";
	private static final String FIELD_ROWID = "rowid";
	private static final String FIELD_CONFIRMATION_CLOSE = "confirmationclose";
	
	
	private static final String CREATE_TABLE_USER = "create table "
		+USER_TABLE_NAME+" ("+FIELD_EMAIL+" text, "+FIELD_PASSWORD+" text, " +
		FIELD_REMEMBERME+" integer default "+VALUE_OF_FALSE+" ," +
		FIELD_AUTOLOGIN+" integer default "+VALUE_OF_TRUE+","+
		FIELD_CONFIRMATION_CLOSE+" integer default "+VALUE_OF_TRUE+
		", primary key ("+FIELD_EMAIL+"))";
	private static final String CREATE_TABLE_CARD = "create table "+
		CARD_TABLE_NAME+" ("+FIELD_USERID+" integer, "+FIELD_CARDNUMBER+" text, "+FIELD_BALANCE+" text, " +
				"primary key("+FIELD_CARDNUMBER+"))";
	
	/**
	 * 
	 * @param context
	 */
	public TRCardManagerDbHelper(Context context){
		this(context, DATABASE_NAME, null, DATABASE_VERSION);
	}
	
	private TRCardManagerDbHelper(Context context, String name,
			CursorFactory factory, int version) {
		super(context, name, factory, version);
	}

	@Override
	public void onCreate(SQLiteDatabase db) {
		db.execSQL(CREATE_TABLE_USER);
		db.execSQL(CREATE_TABLE_CARD);
	}
	
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
		//code in this method will be changed with each new version
		String updateTableVersion_9 = "alter table "+USER_TABLE_NAME+" add column "+
			FIELD_CONFIRMATION_CLOSE+" integer default "+VALUE_OF_TRUE;
		db.execSQL(updateTableVersion_9);
	}
	
	/**
	 * 
	 * @return
	 */
	public UserDao findRemeberedUser(){
		UserDao user = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try{
			db = getReadableDatabase();
			String[] fields = new String[]{FIELD_ROWID,FIELD_EMAIL,FIELD_PASSWORD,
					FIELD_REMEMBERME,FIELD_AUTOLOGIN,FIELD_CONFIRMATION_CLOSE};
			String clausule = FIELD_REMEMBERME+"=?";
			String[] clausuleValues = new String[]{""+VALUE_OF_TRUE};
			cursor = db.query(USER_TABLE_NAME, fields,clausule,clausuleValues,
				null, null, null);
			if(cursor.moveToFirst()){
				user = new UserDao(cursor.getString(1), cursor.getString(2),
						getBooleanValueOfInt(cursor.getInt(3)),
						getBooleanValueOfInt(cursor.getInt(4)),
						getBooleanValueOfInt(cursor.getInt(5)));
				user.setRowId(cursor.getLong(0));
			}
		}finally{
			if(cursor!=null){cursor.close();}
			if(db!=null){db.close();}
		}
		return user;
	}
	
	/**
	 * 
	 * @param user
	 */
	public void findUser(UserDao user){
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try{
			db = getReadableDatabase();
			String[] fields = new String[]{FIELD_ROWID,FIELD_REMEMBERME,FIELD_AUTOLOGIN, FIELD_CONFIRMATION_CLOSE};
			String clausule = FIELD_EMAIL+"=? and "+FIELD_PASSWORD+"=?";
			String[] clausuleValues = new String[]{user.getEmail(),user.getPassword()};
			cursor = db.query(USER_TABLE_NAME, fields, clausule,clausuleValues, null, null, null);
			if(cursor.moveToFirst()){
				user.setRowId(cursor.getLong(0));
				user.setRememberme(getBooleanValueOfInt(cursor.getInt(1)));
				user.setAutologin(getBooleanValueOfInt(cursor.getInt(2)));
				user.setConfirmationClose(getBooleanValueOfInt(cursor.getInt(3)));
			}
		}finally{
			if(cursor!=null){cursor.close();}
			if(db!=null){db.close();}
		}
	}
	
	/**
	 * 
	 * @param user
	 */
	public void createUser(UserDao user){
		if(user.isRememberme()){
			clearRemeberMeUsers();
		}
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(FIELD_EMAIL, user.getEmail());
			values.put(FIELD_PASSWORD,user.getPassword());
			values.put(FIELD_REMEMBERME,getIntValueOfBoolean(user.isRememberme()));
			long rowId = db.insert(USER_TABLE_NAME, null, values);
			user.setRowId(rowId);
		}finally{
			if(db!=null){db.close();}
		}
	}
	
	/**
	 * 
	 * @param user
	 */
	public void updateUserRemeberMe(UserDao user){
		if(user.isRememberme()){
			clearRemeberMeUsers();
		}
		ContentValues values = new ContentValues();
		values.put(FIELD_REMEMBERME,getIntValueOfBoolean(user.isRememberme()));
		String clausule = FIELD_ROWID+"=?";
		String[] clausuleValues = new String[]{String.valueOf(user.getRowId())};
		updateUserTable(values, clausule, clausuleValues);
	}
	
	/**
	 * 
	 * @param user
	 */
	public void updateUserAutoLogin(UserDao user){
		ContentValues values = new ContentValues();
		values.put(FIELD_AUTOLOGIN,getIntValueOfBoolean(user.isAutologin()));
		String clausule = FIELD_ROWID+"=?";
		String[] clausuleValues = new String[]{String.valueOf(user.getRowId())};
		updateUserTable(values, clausule, clausuleValues);
	}
	
	/**
	 * 
	 * @param user
	 */
	public void updateUserConfirmationClose(UserDao user){
		ContentValues values = new ContentValues();
		values.put(FIELD_CONFIRMATION_CLOSE,getIntValueOfBoolean(user.isConfirmationClose()));
		String clausule = FIELD_ROWID+"=?";
		String[] clausuleValues = new String[]{String.valueOf(user.getRowId())};
		updateUserTable(values, clausule, clausuleValues);
	}
	
	/**
	 * 
	 * @param user
	 * @param newPassword
	 */
	public void updateUserPassword(UserDao user, String newPassword){
		ContentValues values = new ContentValues();
		values.put(FIELD_PASSWORD,newPassword);
		String clausule = FIELD_ROWID+"=?";
		String[] clausuleValues = new String[]{String.valueOf(user.getRowId())};
		updateUserTable(values, clausule, clausuleValues);
	}
	
	
	private void updateUserTable(ContentValues values, String clausule,
			String[] clausuleValues){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			db.update(USER_TABLE_NAME, values, clausule, clausuleValues);
		}finally{
			if(db!=null){db.close();}
		}
	}
	
	
	/**
	 * 
	 * @param user
	 */
	public void findUserCards(UserDao user){
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try{
			db = getReadableDatabase();
			String[] fields = new String[]{FIELD_ROWID,FIELD_CARDNUMBER,FIELD_BALANCE};
			
			CardDao actualCard = user.getActualCard();
			
			String clausule = FIELD_USERID+"=?";
			String[] clausuleValues = new String[]{String.valueOf(user.getRowId())};
		
			if(actualCard!=null){
				clausule += " and "+FIELD_CARDNUMBER+"!=?";
				clausuleValues = new String[]{String.valueOf(user.getRowId()),
						actualCard.getCardNumber()};
			}
			cursor = db.query(CARD_TABLE_NAME, fields, clausule, clausuleValues, null, null, null);
			if(cursor.moveToFirst()){
				user.setCards(new ArrayList<CardDao>()); 
				do{
					CardDao card = new CardDao(cursor.getLong(0), cursor.getString(1),cursor.getString(2));
					user.getCards().add(card);
				}while(cursor.moveToNext());
			}
		}finally{
			if(cursor!=null){cursor.close();}
			if(db!=null){db.close();}
		}
	}
	
	
	/**
	 * 
	 * @param userId
	 * @param card
	 */
	public void addCard(long userId, CardDao card){
		CardDao cardFound = findCard(card.getCardNumber());
		if(cardFound==null){
			SQLiteDatabase db = null;
			try{
				db = getWritableDatabase();
				ContentValues values = new ContentValues();
				values.put(FIELD_CARDNUMBER, card.getCardNumber());
				values.put(FIELD_USERID,userId);
				long rowid = db.insert(CARD_TABLE_NAME, null, values);
				card.setId(rowid);
			}finally{
				if(db!=null){db.close();}
			}
		}else{
			card.setId(cardFound.getId());
		}
		
	}
	
	/**
	 * 
	 * @param card
	 */
	public void updateCardBalance(CardDao card){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put(FIELD_BALANCE, card.getBalance());
			String clausule = FIELD_ROWID+"=?";
			String[] clausuleValues = new String[]{String.valueOf(card.getId())};
			db.update(CARD_TABLE_NAME, values, clausule, clausuleValues);
		}finally{
			if(db!=null){db.close();}
		}
	}
	
	/**
	 * 
	 * @param cardId
	 */
	public void deleteCard(long cardId){
		SQLiteDatabase db = null;
		try{
			db = getWritableDatabase();
			String clausule = FIELD_ROWID+"=?";
			String[] clausuleValues = new String[]{String.valueOf(cardId)};
			db.delete(CARD_TABLE_NAME, clausule, clausuleValues);
		}finally{
			if(db!=null){db.close();}
		}
	}
	
	
	private CardDao findCard(String cardNumber){
		CardDao card = null;
		SQLiteDatabase db = null;
		Cursor cursor = null;
		try{
			db = getReadableDatabase();
			String[] fields = new String[]{FIELD_ROWID};
			String clausule = "cardnumber=?";
			String[] clausuleValues = new String[]{cardNumber};
			cursor = db.query(CARD_TABLE_NAME, fields, clausule,clausuleValues , null, null, null);
			if(cursor.moveToFirst()){
				card = new CardDao(cursor.getLong(0),null,null);
			}
		}finally{
			if(cursor!=null){cursor.close();}
			if(db!=null){db.close();}
		}
		return card;
	}
	
	private void clearRemeberMeUsers(){
		ContentValues values = new ContentValues();
		values.put(FIELD_REMEMBERME,VALUE_OF_FALSE);
		updateUserTable(values, null, null);
	}
	
	private int getIntValueOfBoolean(boolean booleanValue){
		return booleanValue?VALUE_OF_TRUE:VALUE_OF_FALSE;
	}
	
	private boolean getBooleanValueOfInt(int intValue){
		return VALUE_OF_TRUE == intValue;
	}

}
