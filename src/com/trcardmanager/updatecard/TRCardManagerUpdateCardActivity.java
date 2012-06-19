package com.trcardmanager.updatecard;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.UpdateCardAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerUpdateCardActivity extends Activity {
	
	private static final String TAG = TRCardManagerUpdateCardActivity.class.getName();
	
	private UserDao userDao;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setTitle(R.string.update_card_title);
		setContentView(R.layout.update_card_layout);
		userDao = TRCardManagerApplication.getUser();
		fillActualCard();
		fillOtherCardList();
		TRCardManagerApplication.setActualActivity(this);
	}
	
	
	public void updateCard(View v){
		String cardNumber = ((EditText)findViewById(R.id.update_card_cardnumber_text)).getText().toString();
		
		if(!StringUtil.isBlank(cardNumber) && cardNumber.length()==16){
			new UpdateCardAction().execute(cardNumber);
		}else{
			Toast.makeText(getApplicationContext(), R.string.update_card_error_card_size, Toast.LENGTH_LONG).show();
		}
    	
    }

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	

	private void fillActualCard(){
		EditText actualCard = (EditText)findViewById(R.id.update_card_cardnumber_actual_text);
		actualCard.setText(userDao.getActualCard().getCardNumber());
	}
	
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

}
