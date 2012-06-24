package com.trcardmanager;

import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.about.TRCardManagerAboutActivity;
import com.trcardmanager.action.MovementListAction;
import com.trcardmanager.adapter.MovementsListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.myaccount.TRCardManagerMyAccountActivity;
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
        initActivity();
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.principal_menu, menu);
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	switch(item.getItemId()){
    		case R.id.principal_menu_settings:
    			Intent settingsUpdate = new Intent(this, TRCardManagerMyAccountActivity.class);
    			startActivityForResult(settingsUpdate, TRCardManagerApplication.MY_ACCOUNT_CLOSED);
    			break;
    		case R.id.principal_menu_about:
    			Intent settingsAbout = new Intent(getApplicationContext(), TRCardManagerAboutActivity.class);
    			startActivity(settingsAbout);
    			break;
    	}
    	return true;
    }
    
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
    	 super.onActivityResult(requestCode, resultCode, data);
    	 if(requestCode == TRCardManagerApplication.MY_ACCOUNT_CLOSED){
    		if(resultCode == TRCardManagerApplication.CARD_UPDATED){
    			initActivity();
    		}else if (resultCode == TRCardManagerApplication.SESSION_EXPIRED_APPLICATION){
    			finishWithSessionExpired();
    		}else if (resultCode == TRCardManagerApplication.PASSWORD_UPDATED){
    			Toast.makeText(getApplicationContext(), R.string.update_password_changed, Toast.LENGTH_LONG).show();
    		}
    		
    	}
    	
    }

	private void finishWithSessionExpired() {
		this.setResult(TRCardManagerApplication.SESSION_EXPIRED_APPLICATION);
		this.finish();
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
    
    private void initActivity(){
    	UserDao user = TRCardManagerApplication.getUser();
		//view actions
		addCardsToView(user);		
		TRCardManagerApplication.setActualActivity(this);
		
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
    	super.onRestart();
    	if(isDataStillReady()){
    		TRCardManagerApplication.setActualActivity(this);
    	}else{
    		finishWithSessionExpired();
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