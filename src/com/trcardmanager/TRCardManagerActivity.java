package com.trcardmanager;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.TextView;

import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;
import com.trcardmanager.http.TRCardManagerHttpAction;

public class TRCardManagerActivity extends Activity {
   
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        setContentView(R.layout.main);
        
		UserDao user = TRCardManagerApplication.getUser();
		//view actions
		addCardsToView(user);		
		TRCardManagerApplication.setActualActivity(this);
    }
    
    
    
    
    private void addMovementsToView(List<MovementDao> movements,LinearLayout viewActualCard ){
    	
    	LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT);
    	lp.setMargins(14, 3, 14, 3);
    	
    	LayoutInflater inflater = LayoutInflater.from(this);
    	LinearLayout linearScrollMovements = (LinearLayout) inflater.inflate(R.layout.list_movements, null,false);
    	ScrollView  scrollMovements =  (ScrollView) linearScrollMovements.getChildAt(2);
    	LinearLayout linearMovements = (LinearLayout) scrollMovements.getChildAt(0);
    	
    	boolean isOdd = false;
    	for(MovementDao movement:movements){
    		LinearLayout viewMovement = null;
    		if(!isOdd){
		    		viewMovement = (LinearLayout) inflater.inflate(R.layout.card_movement, null,false);
		    		viewMovement.setLayoutParams(lp);
    		}else{
		    		viewMovement = (LinearLayout)inflater.inflate(R.layout.card_movement_odd, null,false);
		    		viewMovement.setLayoutParams(lp);
    		}
    		
    		RelativeLayout realtiveMovementLayout = (RelativeLayout)inflater.inflate(
    				R.layout.movement_relative_layout, null,false);
    		
    		((TextView)realtiveMovementLayout.findViewById(R.id.hour_movement)).setText(movement.getHour()+" -");
    		((TextView)realtiveMovementLayout.findViewById(R.id.date_movement)).setText(movement.getDate());
    		((TextView)realtiveMovementLayout.findViewById(R.id.operation_movement)).setText(movement.getOperationType());
    		((TextView)realtiveMovementLayout.findViewById(R.id.balance_movement)).setText(movement.getAmount());
    		((TextView)realtiveMovementLayout.findViewById(R.id.state_movement)).setText(movement.getState());
    		((TextView)realtiveMovementLayout.findViewById(R.id.trade_movement)).setText(movement.getTrade());
    		
    		viewMovement.addView(realtiveMovementLayout);
    		
    		linearMovements.addView(viewMovement);
    		isOdd = !isOdd;
    	}
    	
    	viewActualCard.addView(linearScrollMovements);
    }
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	TRCardManagerApplication.setActualActivity(this);
    }
    
    
    public void clickMovementsText(View v){
    	ScrollView scrollMovements = (ScrollView)findViewById(R.id.scrollMovements);
    	int visibility = scrollMovements.getVisibility();
    	if(visibility == LinearLayout.VISIBLE){
    		visibility = LinearLayout.GONE;
    	}else{
    		visibility = LinearLayout.VISIBLE;
    	}
    	scrollMovements.setVisibility(visibility);
    	scrollMovements.getParent().requestLayout();
    	//scrollMovements.getParent().getParent().requestLayout();
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
	    addMovementsToView(actualCard.getMovements(),layActualCard);
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
 				getApplicationContext().getString(R.string.cards_list_card_balance),true,false)); 		
 			TextView textActualCardBalance = createCardDataTextView(
 					card.getBalance()+"€", false,true);
 			cardDataBalanceLayout.addView(textActualCardBalance);
 		
 		cardLayout.addView(cardDataBalanceLayout);
 		
    	
    	return cardLayout;
    }
    
    
    private RelativeLayout createNumberCardLayout(CardDao card, boolean actualCard){
    	RelativeLayout cardDataLayout = new RelativeLayout(getApplicationContext());
    	cardDataLayout.setLayoutParams(new RelativeLayout.LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
    	cardDataLayout.setBackgroundColor(Color.rgb(224, 230, 248));
        
        TextView titleCardNumber = createCardDataTextView(
 				getApplicationContext().getString(R.string.cards_list_card_title),true,false);
        RelativeLayout.LayoutParams relLayoutParms = 
        	new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
        relLayoutParms.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        titleCardNumber.setLayoutParams(relLayoutParms);
        titleCardNumber.setId(101010);
 		cardDataLayout.addView(titleCardNumber);
 		
 		TextView textActualCard = createCardDataTextView(card.getCardNumber(),false,false);
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
 		
 		
 		if(!actualCard){
 			TextView textNoActive = new TextView(getApplicationContext());
 			textNoActive.setGravity(Gravity.CENTER_VERTICAL);

 			textNoActive.setBackgroundResource(R.drawable.no_active);
 			textNoActive.setOnClickListener(new OnClickListener() {
 					public void onClick(View v) {
 						//active card
 						long cardId = v.getId()/1000;
 						changeActiveCard(cardId);
 					}
 				});
 			textNoActive.setId((int)card.getId()*1000);
 	 		RelativeLayout.LayoutParams relLayoutParmsNoActive = 
 					new RelativeLayout.LayoutParams(LayoutParams.WRAP_CONTENT,LayoutParams.WRAP_CONTENT);
 	 		relLayoutParmsNoActive.addRule(RelativeLayout.LEFT_OF,textActualOrRemoveCardStart.getId());
 	 		textNoActive.setLayoutParams(relLayoutParmsNoActive);
 	 		cardDataLayout.addView(textNoActive);
 		}
 		
 		return cardDataLayout;
    }
    
    private void changeActiveCard(long cardId){
    	UserDao user = TRCardManagerApplication.getUser();
    	CardDao newActiveCard = findCardById(user.getCards(),cardId);
    	//http active card
    	TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
    	try {
			httpAction.activateCard(user, newActiveCard);
			//refresh in memory list
			user.getCards().remove(newActiveCard);
			CardDao tempCard = user.getActualCard().getCopy();
			user.setActualCard(newActiveCard);
			user.getCards().add(tempCard);
			
			httpAction.getActualCardBalanceAndMovements(user);
			//update balance card
			TRCardManagerDbHelper dbHelper = new TRCardManagerDbHelper(getApplicationContext());
			dbHelper.updateCardBalance(newActiveCard);
	    	//show
			addCardsToView(user);
		} catch (IOException e) {
			e.printStackTrace();
			showErrorDialog(R.string.activate_card_error);
		} catch (TRCardManagerUpdateCardException e) {
			e.printStackTrace();
			showErrorDialog(R.string.activate_card_error);
		} catch (TRCardManagerDataException e) {
			e.printStackTrace();
			//TODO change message
			showErrorDialog(R.string.activate_card_error);
		}
    	
    }
    
    private void showErrorDialog(int messageCode){
    	AlertDialog.Builder builder = new AlertDialog.Builder(this);
    	builder.setMessage(messageCode)
    	       .setCancelable(false)
    	       .setPositiveButton("OK", new DialogInterface.OnClickListener() {
    	           public void onClick(DialogInterface dialog, int id) {
    	                dialog.cancel();
    	           }
    	       });
    	AlertDialog alert = builder.create();
    	alert.show();
    }
    
    private TextView createCardDataTextView(String text,boolean bold,boolean extraSize){
		TextView textView = new TextView(getApplicationContext());
		textView.setText(text+" ");
		textView.setTextColor(Color.parseColor("#18376C"));
//		textView.setTextColor(Color.rgb(52,90,204));
		if(bold){
			textView.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));
		}else{
			textView.setTypeface(Typeface.create(Typeface.SERIF,Typeface.NORMAL));	
		}
		textView.setGravity(Gravity.CENTER_VERTICAL);
		if(extraSize){
			textView.setTextSize(textView.getTextSize()+2L);
		}
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
    
    
    
    private CardDao findCardById(List<CardDao> cards, long id){
    	for(CardDao card:cards){
    		if(card.getId() == id){
    			return card;
    		}
    	}
    	return null;
    }
    
   
}