package com.trcardmanager.myaccount;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.GestureDetector;
import android.view.GestureDetector.OnGestureListener;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.UpdateCardAction;
import com.trcardmanager.action.UpdatePasswordAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerMyAccountActivity extends Activity implements OnTouchListener, OnGestureListener {

	enum Tabs{
		TAB_CARD,
		TAB_PASSWORD
	}
	
	private LayoutInflater inflater;
	private GestureDetector gestureScanner;
	private Tabs selectedTab;
	private UserDao userDao;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.my_account_tab_layout);
		
		gestureScanner = new GestureDetector(this,this);
		inflater = LayoutInflater.from(this);
		userDao = TRCardManagerApplication.getUser();
		
		TRCardManagerApplication.setActualActivity(this);
		((ScrollView)findViewById(R.id.tab_content_layout)).setOnTouchListener(this);
		
		showUpdateCard(null);
	}
	
	
	public void showUpdateCard(View v){
		View updateView = inflater.inflate(R.layout.update_card_layout, null);
		
		hideKeyboard((EditText)((ScrollView)findViewById(R.id.tab_content_layout))
				.findViewById(R.id.update_password_text));
		
		replaceTabView(updateView);
		
		selectedTab = Tabs.TAB_CARD;
		setSelectedTab();
		
		fillOtherCardList();
	}
	
	public void showUpdatePassword(View v){
		View updateView = inflater.inflate(R.layout.update_password_layout, null);
		
		hideKeyboard((EditText)((ScrollView)findViewById(R.id.tab_content_layout))
				.findViewById(R.id.update_card_cardnumber_text));
		
		replaceTabView(updateView);

		selectedTab = Tabs.TAB_PASSWORD;
		setSelectedTab();
	}
	
	
	private void replaceTabView(View updateView){
		((ScrollView)findViewById(R.id.tab_content_layout)).removeAllViews();
		((ScrollView)findViewById(R.id.tab_content_layout)).addView(updateView);
		updateView.setOnTouchListener(this);
	}
	
	
	private void setSelectedTab(){
		int notActiveColor = Color.parseColor("#A4A4A4");
		if(selectedTab == Tabs.TAB_CARD){
			(findViewById(R.id.tab_seleted_card_layout)).setVisibility(View.VISIBLE);
			(findViewById(R.id.tab_seleted_password_layout)).setVisibility(View.INVISIBLE);
			((TextView)findViewById(R.id.tab_card_title)).setTextColor(Color.WHITE);
			((TextView)findViewById(R.id.tab_password_title)).setTextColor(notActiveColor);
		}else{
			(findViewById(R.id.tab_seleted_password_layout)).setVisibility(View.VISIBLE);
			(findViewById(R.id.tab_seleted_card_layout)).setVisibility(View.INVISIBLE);
			((TextView)findViewById(R.id.tab_card_title)).setTextColor(notActiveColor);
			((TextView)findViewById(R.id.tab_password_title)).setTextColor(Color.WHITE);
		}
	}
	
	
	private void hideKeyboard(EditText editText) {
		if(editText!=null){
			InputMethodManager imm = (InputMethodManager)getSystemService(
				      Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(editText.getWindowToken(), 0);
		}
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}

	
	/* *** update card *** */
	private void fillOtherCardList(){
		List<CardDao> cards = userDao.getCards();
		if(!cards.isEmpty()){
			List<String> cardsNumbers = new ArrayList<String>();
			for(CardDao card:cards){
				cardsNumbers.add(card.getCardNumber());
			}
			ArrayAdapter<String> adapter = new ArrayAdapter<String>(this,
	                 android.R.layout.simple_dropdown_item_1line, cardsNumbers);
	        AutoCompleteTextView newCardtextView = (AutoCompleteTextView)
	                 findViewById(R.id.update_card_cardnumber_text);
	        newCardtextView.setAdapter(adapter);
		}
	}
	
	public void updateCard(View v){
		String cardNumber = ((EditText)findViewById(R.id.update_card_cardnumber_text)).getText().toString();
		
		if(!StringUtil.isBlank(cardNumber) && cardNumber.length()==16){
			new UpdateCardAction().execute(cardNumber);
		}else{
			Toast.makeText(getApplicationContext(), R.string.update_card_error_card_size, Toast.LENGTH_LONG).show();
		}
    	
    }
	
	/* ***** ****** */
	
	
	/* **** update password ***** */
	public void onClickUpdatePasswordButton(View v){
		String newPass = getPassWordTyped(R.id.update_password_text);
		String newPassConfirm = getPassWordTyped(R.id.update_password_text_confirm);
		if(StringUtil.isBlank(newPass)){
			//error pass
			showErrorUpdatePassword(R.string.update_password_error_pass);
			setFocusOnEditTextResouce(R.id.update_password_text);
		}else if(StringUtil.isBlank(newPassConfirm)){
			//error confirm pass
			showErrorUpdatePassword(R.string.update_password_error_pass_confirm);
			setFocusOnEditTextResouce(R.id.update_password_text_confirm);
		}else if(!newPass.equals(newPassConfirm)){
			//error equal pass
			showErrorUpdatePassword(R.string.update_password_error_equal_passs);
			setFocusOnEditTextResouce(R.id.update_password_text_confirm);
		}else {
			new UpdatePasswordAction().execute(newPass);
		}
			
	}
	
	
	private String getPassWordTyped(int resource){
		EditText editText = (EditText)findViewById(resource);
		return editText.getText().toString();
	}
	
	private void showErrorUpdatePassword(int messageResource){
		Toast.makeText(getApplicationContext(), messageResource, Toast.LENGTH_LONG).show();
	}
	
	private void setFocusOnEditTextResouce(int editTextResource){
		((EditText)findViewById(editTextResource)).requestFocus();
	}
	/* **** ***** ****** */
	
	
	/* ** Listener methods *** **** ** */
	public boolean onDown(MotionEvent e) {
		return false;
	}
	
	public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX,
			float velocityY) {
		if(e1.getX()>e2.getX() && selectedTab != Tabs.TAB_PASSWORD){ //form left to right
			showUpdatePassword(null);
		}else if(e2.getX()>e1.getX() && selectedTab != Tabs.TAB_CARD){ //form right to left
			showUpdateCard(null);
		}
		return true;
	}
	
	public void onLongPress(MotionEvent e) {	
	}
	
	public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX,
			float distanceY) {
		return false;
	}
	
	public void onShowPress(MotionEvent e) {
	}
	
	public boolean onSingleTapUp(MotionEvent e) {
		return false;
	}

	public boolean onTouch(View v, MotionEvent event) {
		return gestureScanner.onTouchEvent(event);
	}
	/* ******************************* */
	

}
