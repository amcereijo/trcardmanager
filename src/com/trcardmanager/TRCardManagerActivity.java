package com.trcardmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.action.MovementListAction;
import com.trcardmanager.adapter.MovementsListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.views.TRCardManagerListView;
import com.trcardmanager.views.TRCardManagerListView.OnRefreshListenerBottomLoad;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerActivity extends Activity {

	final private static String TAG = TRCardManagerActivity.class.getName();
	
	
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
        
    
    private void addMovementsToView(List<MovementDao> movements){
    	List<MovementDao> movementsCopy = new ArrayList<MovementDao>();
    	movementsCopy.addAll(movements);
    	final TRCardManagerListView linearMovements = (TRCardManagerListView)findViewById(R.id.layout_listview_movements);
    	final ArrayAdapter<MovementDao> adapter = new MovementsListViewAdapter(this, R.id.layout_listview_movements, movementsCopy);
    	final TextView balanceView = (TextView)findViewById(R.id.card_balance);
    	linearMovements.setOnRefreshListener(new OnRefreshListenerBottomLoad() {
            public void onRefresh() {
            	new MovementListAction(linearMovements,adapter,balanceView).execute();
            }
        });
		linearMovements.setAdapter(adapter);
    }
    
    
    @Override
    protected void onRestart() {
    	Log.d(TAG, "On restart application");
    	super.onRestart();
    	if(isDataStillReady()){
    		TRCardManagerApplication.setActualActivity(this);
    	}else{
    		this.setResult(TRCardManagerApplication.SESSION_EXPIRED_APPLICATION);
        	this.finish();
    	}
    }
    
    
    private boolean isDataStillReady(){
    	UserDao user = TRCardManagerApplication.getUser();
    	boolean stillReady = (user != null);	
    	return stillReady;
    }
    

    
    private void addCardsToView(UserDao user){
    	RelativeLayout cardsLayout = (RelativeLayout)findViewById(R.id.layout_card_information);
    	
    	CardDao actualCard = user.getActualCard();
	    TextView cardNumber = (TextView)cardsLayout.findViewById(R.id.card_number);
	    cardNumber.setText(actualCard.getCardNumber());
	    TextView cardBalance = (TextView)cardsLayout.findViewById(R.id.card_balance);
    	cardBalance.setText(actualCard.getBalance());
    	
    	
	    addMovementsToView(actualCard.getMovementsData().getMovements());
		
    }
 
   
}