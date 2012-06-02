package com.trcardmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup.MarginLayoutParams;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.LinearLayout.LayoutParams;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.adapter.MovementsListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.MovementsDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.db.TRCardManagerDbHelper;
import com.trcardmanager.exception.TRCardManagerDataException;
import com.trcardmanager.exception.TRCardManagerUpdateCardException;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.views.TRCardManagerListViewBottomLoad;
import com.trcardmanager.views.TRCardManagerListViewBottomLoad.OnRefreshListenerBottomLoad;
import com.trcardmanager.views.TRCardManagerListViewBottomLoad.ScrollDirection;

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
    
    
    @Override
    public void onBackPressed() {
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.exit_dialog_question_title);
		alert.setMessage(R.string.exit_dialog_question_message);
		alert.setPositiveButton(android.R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						closeApplication();
					}
				});
		alert.setNegativeButton(android.R.string.no, null);
		alert.show();
    }
    
    private void closeApplication(){
    	this.finish();
    }
    
    ArrayAdapter<MovementDao> adapter;
    
    private void addMovementsToView(List<MovementDao> movements,LinearLayout viewActualCard ){
    	LayoutInflater inflater = LayoutInflater.from(this);
    	LinearLayout linearScrollMovements = (LinearLayout) inflater.inflate(R.layout.list_movements, null,false);
    	
    	List<MovementDao> movementsCopy = new ArrayList<MovementDao>();
    	movementsCopy.addAll(movements);
    	//linearMovements = (TRCardManagerListViewBottomLoad)linearScrollMovements.getChildAt(2);
    	linearMovements = (TRCardManagerListViewBottomLoad)linearScrollMovements.getChildAt(1);
    	
    	
    	//ListView.LayoutParams lp = new ListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
    	//		LinearLayout.LayoutParams.WRAP_CONTENT);
    	//LinearLayout convertView = new LinearLayout(getApplicationContext());
		//convertView.setLayoutParams(lp);
    	
    	
    	linearMovements.setOnRefreshListener(new OnRefreshListenerBottomLoad() {
			
            public void onRefresh() {
                // Do work to refresh the list here.
                new GetDataTask().execute(linearMovements.getScrollDirection());
            }
        });

    	adapter = new MovementsListViewAdapter(this, R.id.listViewMovements, movementsCopy);
		linearMovements.setAdapter(adapter);
		

		
    	viewActualCard.addView(linearScrollMovements);
    }
    
    TRCardManagerListViewBottomLoad linearMovements;
    List<MovementDao> myListItems;
    
    private class GetDataTask extends AsyncTask<ScrollDirection, Void, Void> {

        @Override
        protected Void doInBackground(ScrollDirection...scrollDirection) {
            
        	if(scrollDirection[0] == ScrollDirection.UP){
        		Log.i("ANGEL","Scroll up");
        		 try {
					Thread.sleep(2000);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
        	}else{
        		Log.i("ANGEL","Scroll down");
	        	// Simulates a background job.
	        	UserDao user = TRCardManagerApplication.getUser();
				MovementsDao movementsData = user.getActualCard().getMovementsData();
				
				
				TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
				try {
					List<MovementDao> lista = httpAction.getNextMovements(user);
					//Reset the array that holds the new items
			    	myListItems = new ArrayList<MovementDao>();
					
					//
					int pageActual = movementsData.getActualPage();
					int nextPage = pageActual+1;
					Log.i("ANGEL", "--------------------- Add from "+pageActual+ " to "+nextPage);
					int i=0;
			    	for (i = 0; i < lista.size(); i++) {		
			    		MovementDao m = lista.get(i);
			        	myListItems.add(m);          	
					}
			    	Log.i("ANGEL", "--------------------- Added : "+i);
			    	if(i==0){
			    		//time to show message
			    		 Thread.sleep(2000);
			    	}
			    	
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getMessage(),e);
				} catch(InterruptedException ine){
					Log.e(this.getClass().toString(), ine.getMessage(),ine);
				}
        	}

			//Done! now continue on the UI thread
	        runOnUiThread(returnRes);
			return null;
        }

        @Override
        protected void onPostExecute(Void result) {
            //mListItems.addFirst("Added after refresh...");

            // Call onRefreshComplete when the list has been refreshed.
        	linearMovements.onRefreshComplete();

            super.onPostExecute(result);
        }
        
        private Runnable returnRes = new Runnable() {
            
            public void run() {
            	
    			//Loop thru the new items and add them to the adapter
    			if(myListItems != null && myListItems.size() > 0){
                    for(int i=0;i<myListItems.size();i++)
                    	adapter.add(myListItems.get(i));
                }
            	
    						
    			//Tell to the adapter that changes have been made, this will cause the list to refresh
                adapter.notifyDataSetChanged();

            }
        };
    }
    
    
    
    
    @Override
    protected void onRestart() {
    	super.onRestart();
    	TRCardManagerApplication.setActualActivity(this);
    }
    
    
    public void clickMovementsText(View v){
    	ListView listMovements = (ListView)findViewById(R.id.listViewMovements);
    	int visibility = (listMovements.getVisibility() == ListView.VISIBLE)?ListView.GONE:ListView.VISIBLE;
    	listMovements.setVisibility(visibility);
    	listMovements.getParent().requestLayout();
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
    	RelativeLayout cardsLayout = (RelativeLayout)findViewById(R.id.cards_layout);
    	cardsLayout.removeAllViews();
    	
    	CardDao actualCard = user.getActualCard();
	    LinearLayout layActualCard = createCardLayout(actualCard,true);
	    addMovementsToView(actualCard.getMovementsData().getMovements(),layActualCard);
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