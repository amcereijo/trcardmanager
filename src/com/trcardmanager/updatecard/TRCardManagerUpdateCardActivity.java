package com.trcardmanager.updatecard;

import java.util.ArrayList;
import java.util.List;

import org.jsoup.helper.StringUtil;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.UpdateCardAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.listener.TouchElementsListener;

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
		Button buttonUpdate = (Button)findViewById(R.id.update_card_btn_update);
		buttonUpdate.setOnTouchListener(new TouchElementsListener<Button>());
		userDao = TRCardManagerApplication.getUser();
		fillOtherCardList();
		TRCardManagerApplication.setActualActivity(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TRCardManagerApplication.setActualActivity(this);
	}
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		String textCard = ((TextView) findViewById(R.id.update_card_cardnumber_text)).getText().toString();
		setContentView(R.layout.update_card_layout);
		((TextView) findViewById(R.id.update_card_cardnumber_text)).setText(textCard);
		fillOtherCardList();
		super.onConfigurationChanged(newConfig);
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
