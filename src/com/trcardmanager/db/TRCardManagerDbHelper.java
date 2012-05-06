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

public class TRCardManagerDbHelper extends SQLiteOpenHelper {
	//boolean = integers 0 (false) and 1 (true).
	private static final int VALUE_OF_TRUE = 1;
	private static final int VALUE_OF_FALSE = 0;
	//the rowid is like a id of user
	
	private static final int DATABASE_VERSION = 5;
	private static final String DATABASE_NAME = "trcardmanager";
	private static final String USER_TABLE_NAME = "table_users";
	private static final String CARD_TABLE_NAME = "table_cards";
	
	private static final String CREATE_TABLE_USER = "create table "
		+USER_TABLE_NAME+" (email text, password text, rememberme integer default "+VALUE_OF_FALSE+" ," +
			"primary key (email))";
	private static final String CREATE_TABLE_CARD = "create table "+
		CARD_TABLE_NAME+" (userid integer, cardnumber text, balance text, " +
				"primary key(cardnumber))";
	
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
		db.execSQL("DROP TABLE IF EXISTS "+USER_TABLE_NAME);
		db.execSQL("DROP TABLE IF EXISTS "+CARD_TABLE_NAME);
		db.execSQL(CREATE_TABLE_USER);
		db.execSQL(CREATE_TABLE_CARD);
	}
	
	
	public UserDao findRemeberedUser(){
		UserDao user = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(USER_TABLE_NAME, new String[]{"rowid","email","password","rememberme"},
				"rememberme=?",new String[]{""+VALUE_OF_TRUE}, null, null, null);
		if(cursor.moveToFirst()){
			user = new UserDao(cursor.getString(1), cursor.getString(2),
					getBooleanValueOfInt(cursor.getInt(3)));
			user.setRowId(cursor.getLong(0));
		}
		cursor.close();
		db.close();
		return user;
	}
	
	public void findUser(UserDao user){
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(USER_TABLE_NAME, new String[]{"rowid","rememberme"}, "email=? and password=?",
				new String[]{user.getEmail(),user.getPassword()}, null, null, null);
		if(cursor.moveToFirst()){
			user.setRowId(cursor.getLong(0));
			user.setRememberme(getBooleanValueOfInt(cursor.getInt(1)));
		}
		cursor.close();
		db.close();
	}
	
	public void createUser(UserDao user){
		if(user.isRememberme()){
			clearRemeberMeUsers();
		}
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("email", user.getEmail());
		values.put("password",user.getPassword());
		values.put("rememberme",getIntValueOfBoolean(user.isRememberme()));
		long rowId = db.insert(USER_TABLE_NAME, null, values);
		user.setRowId(rowId);
		db.close();
	}
	
	public void updateUserRemeberMe(UserDao user){
		if(user.isRememberme()){
			clearRemeberMeUsers();
		}
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("rememberme",getIntValueOfBoolean(user.isRememberme()));
		String where = "rowid=?";
		db.update(USER_TABLE_NAME, values, where, new String[]{String.valueOf(user.getRowId())});
		db.close();
	}
	
	public void findUserCards(UserDao user){
		String whereClausule = "";
		String[] whereParams = null;
		CardDao actualCard = user.getActualCard();
		if(actualCard==null){
			whereClausule = "userid=?";
			whereParams = new String[]{String.valueOf(user.getRowId())};
		}else{
			whereClausule = "userid=? and cardnumber!=?";
			whereParams = new String[]{String.valueOf(user.getRowId()),
					actualCard.getCardNumber()};
		}
		
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(CARD_TABLE_NAME, new String[]{"rowid","cardnumber","balance"}, whereClausule, 
				whereParams, null, null, null);
		if(cursor.moveToFirst()){
			user.setCards(new ArrayList<CardDao>()); 
			do{
				CardDao card = new CardDao(cursor.getLong(0),
						cursor.getString(1),cursor.getString(2));
				user.getCards().add(card);
			}while(cursor.moveToNext());
		}
		cursor.close();
		db.close();
	}
	
	
	
	public void addCard(long userId, CardDao card){
		CardDao cardFound = findCard(card.getCardNumber());
		if(cardFound==null){
			SQLiteDatabase db = getWritableDatabase();
			ContentValues values = new ContentValues();
			values.put("cardnumber", card.getCardNumber());
			values.put("userid",userId);
			long rowid = db.insert(CARD_TABLE_NAME, null, values);
			card.setId(rowid);
			db.close();
		}else{
			card.setId(cardFound.getId());
		}
		
	}
	
	public void updateCardBalance(CardDao card){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("balance", card.getBalance());
		db.update(CARD_TABLE_NAME, values, "rowid=?", new String[]{String.valueOf(card.getId())});
		db.close();
	}
	
	public void deleteCard(long cardId){
		SQLiteDatabase db = getWritableDatabase();
		db.delete(CARD_TABLE_NAME, "rowid=?", new String[]{String.valueOf(cardId)});
		db.close();
	}
	
	
	private CardDao findCard(String cardNumber){
		CardDao card = null;
		SQLiteDatabase db = getReadableDatabase();
		Cursor cursor = db.query(CARD_TABLE_NAME, new String[]{"rowid"}, "cardnumber=?", 
				new String[]{cardNumber}, null, null, null);
		if(cursor.moveToFirst()){
			card = new CardDao(cursor.getLong(0),null,null);
		}
		cursor.close();
		db.close();
		return card;
	}
	
	private void clearRemeberMeUsers(){
		SQLiteDatabase db = getWritableDatabase();
		ContentValues values = new ContentValues();
		values.put("rememberme",VALUE_OF_FALSE);
		db.update(USER_TABLE_NAME, values, null, null);
		db.close();
	}
	
	private int getIntValueOfBoolean(boolean booleanValue){
		return booleanValue?VALUE_OF_TRUE:VALUE_OF_FALSE;
	}
	
	private boolean getBooleanValueOfInt(int intValue){
		return VALUE_OF_TRUE == intValue;
	}

}
