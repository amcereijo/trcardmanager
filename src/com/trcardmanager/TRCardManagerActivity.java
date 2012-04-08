package com.trcardmanager;

import java.io.IOException;
import java.util.Currency;
import java.util.List;
import java.util.Locale;

import org.apache.http.client.ClientProtocolException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.http.TRCardManagerHttpAction;

public class TRCardManagerActivity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        //get http card
        
        TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
        TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
        UserDao user = TRCardManagerApplication.getUser();
        try {
        	//http actions
			httpAction.getActualCard(user);
			httpAction.getActualCardBalance(user);
			//db actions
			dbHelper.addCard(user.getRowId(), user.getActualCard());
			dbHelper.findUserCards(user);
			//view actions
			addCardsToView(user);
		} catch (ClientProtocolException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (TRCardManagerDataException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        
        //get db cards
        //update db cards
        
    }
    
    
    public void clickAddCard(View v) {
		//show view to add cardNumber
    	//insertCardNumber
    	
    	 AlertDialog.Builder alert = new AlertDialog.Builder(this);  

         alert.setTitle("Add new card");  
         alert.setMessage("Type de card number");  

         // Set an EditText view to get user input   
         final EditText inputName = new EditText(this);  
         alert.setView(inputName);  
         alert.setCancelable(true);
         alert.setPositiveButton("Add Card", new DialogInterface.OnClickListener() {  
         public void onClick(DialogInterface dialog, int whichButton) {  
             String cardNumber = inputName.getText().toString();
             addNewCardAdded(cardNumber);
           }  
         }); 

         alert.show();
	}
    
    private void addNewCardAdded(String cardNumber){
    	TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
    	CardDao cardDao = new CardDao(cardNumber);
    	dbHelper.addCard(TRCardManagerApplication.getUser().getRowId(), cardDao);
    	
    	LinearLayout cardsLayout = (LinearLayout)findViewById(R.id.cards_layout);
		cardsLayout.addView(createCardLayout(cardDao,false));
    }
    
    private void addCardsToView(UserDao user){
    	LinearLayout cardsLayout = (LinearLayout)findViewById(R.id.cards_layout);
    	cardsLayout.removeAllViews();
    	CardDao actualCard = user.getActualCard();
	    LinearLayout layActualCard = createCardLayout(actualCard,true);
		

		cardsLayout.addView(layActualCard);
		
    	List<CardDao> cards = user.getCards();
    	for(CardDao card:cards){
    		LinearLayout lay = createCardLayout(card,false);
	        cardsLayout.addView(lay);
    	}
    }
    
    public void removeListCard(final long cardId){
    	new AlertDialog.Builder(this)
        .setTitle("Delete entry")
        .setMessage("Are you sure you want to delete this entry?")
        .setPositiveButton("Yes", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
            	TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
            	dbHelper.deleteCard(cardId);
            	UserDao user = TRCardManagerApplication.getUser();
            	updateInMemoryCardList(user, cardId);
            	addCardsToView(user);
            }
         })
        .setNegativeButton("No", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialog, int which) { 
               dialog.cancel();
            }
         })
         .show();
    }
    
    private void updateInMemoryCardList(UserDao user,long cardId){
    	List<CardDao> cards = user.getCards();
    	for(CardDao actualCard:cards){
    		if(actualCard.getId() == cardId){
    			cards.remove(actualCard);
    			break;
    		}
    	}
    }
    
    private LinearLayout createCardLayout(CardDao card, boolean actualCard){
    	LinearLayout cardLayout = createCardLinearLayout();
    	
 		RelativeLayout cardDataLayout = createNumberCardLayout(card,actualCard);
 		cardLayout.addView(cardDataLayout);
 		
 		LinearLayout cardDataBalanceLayout = createCardDataLinearLayout();
 		cardDataBalanceLayout.addView(createCardDataTextView(
 				getApplicationContext().getString(R.string.cards_list_card_balance),true)); 		
 			TextView textActualCardBalance = createCardDataTextView(
 					card.getBalance()+"€", false);
 			cardDataBalanceLayout.addView(textActualCardBalance);
 		
 		cardLayout.addView(cardDataBalanceLayout);
 		
    	
    	return cardLayout;
    }
    
    
    private RelativeLayout createNumberCardLayout(CardDao card, boolean actualCard){
    	RelativeLayout cardDataLayout = new RelativeLayout(getApplicationContext());
    	cardDataLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    	cardDataLayout.setBackgroundColor(Color.rgb(224, 230, 248));
        
        TextView titleCardNumber = createCardDataTextView(
 				getApplicationContext().getString(R.string.cards_list_card_title),true);
        RelativeLayout.LayoutParams relLayoutParms = 
        	new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        relLayoutParms.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        titleCardNumber.setLayoutParams(relLayoutParms);
        titleCardNumber.setId(101010);
 		cardDataLayout.addView(titleCardNumber);
 		
 		TextView textActualCard = createCardDataTextView(card.getCardNumber(),false);
 			RelativeLayout.LayoutParams relLayoutParmsCardNumber = 
 				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 			relLayoutParmsCardNumber.addRule(RelativeLayout.RIGHT_OF,titleCardNumber.getId());
 		textActualCard.setLayoutParams(relLayoutParmsCardNumber);
 		cardDataLayout.addView(textActualCard);
 		
 		TextView textActualOrRemoveCardStart = new TextView(getApplicationContext());
 		textActualOrRemoveCardStart.setGravity(Gravity.CENTER_VERTICAL);
 		if(actualCard){
 			textActualOrRemoveCardStart.setBackgroundResource(R.drawable.active);
 		}else{
 			textActualOrRemoveCardStart.setBackgroundResource(R.drawable.list_remove);
 			textActualOrRemoveCardStart.setOnClickListener(new OnClickListener() {
				public void onClick(View v) {
					long cardId = v.getId();
					removeListCard(cardId);
				}
			});
 		}
 		textActualOrRemoveCardStart.setId((int)card.getId());
 		RelativeLayout.LayoutParams relLayoutParmsIcon = 
				new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 			relLayoutParmsIcon.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		textActualOrRemoveCardStart.setLayoutParams(relLayoutParmsIcon);
 		cardDataLayout.addView(textActualOrRemoveCardStart);
 		
 		return cardDataLayout;
    }
    
    private TextView createCardDataTextView(String text,boolean bold){
		TextView textView = new TextView(getApplicationContext());
		textView.setText(text);
		textView.setTextColor(Color.rgb(52,90,204));
		if(bold){
			textView.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));
		}else{
			textView.setTypeface(Typeface.create(Typeface.SERIF,Typeface.NORMAL));	
		}
		textView.setGravity(Gravity.CENTER_VERTICAL);
		textView.setHeight(50);
		return textView;
    }
    
    private LinearLayout createCardLinearLayout(){
    	LinearLayout lay = new LinearLayout(getApplicationContext());
    	MarginLayoutParams params = new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT);
		params.setMargins(0, 0, 0, 20);
        lay.setLayoutParams(params);
        //lay.setBackgroundColor(Color.rgb(224, 230, 248));
        lay.setBackgroundResource(R.drawable.card_item_border);
        lay.setOrientation(1);
        return lay;
    }
    
    private LinearLayout createCardDataLinearLayout(){
    	LinearLayout lay = new LinearLayout(getApplicationContext());
        lay.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
        lay.setOrientation(0);
        lay.setBackgroundColor(Color.rgb(224, 230, 248));
        return lay;
    }
    
    
    
    private void addCardIfNotExist(UserDao user,CardDao card){
    	List<CardDao> cards = user.getCards();
    	String newCardNumber = card.getCardNumber();
    	boolean match = false;
    	for(CardDao actualCard:cards){
    		if(actualCard.getCardNumber().equals(newCardNumber)){
    			match = true;
    		}
    	}
    	if(!match){
    		cards.add(card);
    	}else{
    		
    	}
    }
    
   
    
}