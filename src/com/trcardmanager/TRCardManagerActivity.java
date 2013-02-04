package com.trcardmanager;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.RelativeLayout.LayoutParams;
import android.widget.TextView;
import android.widget.Toast;

import com.google.ads.AdRequest;
import com.google.ads.AdSize;
import com.google.ads.AdView;
import com.trcardmanager.about.TRCardManagerAboutActivity;
import com.trcardmanager.action.MovementListAction;
import com.trcardmanager.adapter.MovementsListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.CardDao;
import com.trcardmanager.dao.MovementDao;
import com.trcardmanager.dao.UserDao;
import com.trcardmanager.exception.TRCardManagerSessionException;
import com.trcardmanager.http.TRCardManagerHttpCardAction;
import com.trcardmanager.myaccount.TRCardManagerMyAccountActivity;
import com.trcardmanager.restaurant.TRCardManagerRestaurantsActivity;
import com.trcardmanager.settings.TRCardManagerSettingsActivity;
import com.trcardmanager.views.TRCardManagerListView;
import com.trcardmanager.views.TRCardManagerListView.OnRefreshListenerBottomLoad;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerActivity extends Activity {

	private static final int POSITION_AD_VIEW_IN_PARENT = 1;
	private static final String TAG = TRCardManagerActivity.class.getName();
	
	private View popupView = null;
    private PopupWindow pw;
	
	
	/** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        try{
	        requestWindowFeature(Window.FEATURE_NO_TITLE);
        	
	        setContentView(R.layout.main);
	        AdView adView = (AdView)this.findViewById(R.id.adView);
	        adView.loadAd(new AdRequest());
	        prepareMenuOptions();
	        initActivity();
	        
        }catch(Exception e){
        	Log.e(TAG, e.getMessage(),e);
        	this.finish();
        }
    }
   
    @SuppressLint("NewApi")
	private void prepareMenuOptions(){    	
    	boolean fixedMenu = Boolean.TRUE;
    	try{
    		fixedMenu = ViewConfiguration.get(getApplicationContext()).hasPermanentMenuKey();
    	}catch(NoSuchMethodError se){//nothing to do
    	} 
    	if(!fixedMenu){
	        LayoutInflater inflater = (LayoutInflater) getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	        popupView = inflater.inflate(R.layout.menu_layout,null,false);
	        findViewById(R.id.menu_button_icon).setVisibility(View.VISIBLE);
	        ((RelativeLayout) findViewById(R.id.layout_card_number_balance)).setPadding(0, 0, 2, 0);
        }
    }
    
    
    @Override
    public void onConfigurationChanged(Configuration newConfig) {
    	super.onConfigurationChanged(newConfig);
    	removeAndAddAdView();
    }


	private void removeAndAddAdView() {
		AdView adView = (AdView)this.findViewById(R.id.adView);
    	LayoutParams lp = (RelativeLayout.LayoutParams)adView.getLayoutParams();
    	RelativeLayout rl = (RelativeLayout)this.findViewById(R.id.main_layout);
    	rl.removeView(adView);
    	adView = new AdView(this, AdSize.SMART_BANNER, getString(R.string.unidId));
    	AdRequest adReq = new AdRequest();
    	//Delete two lines to publish app
    	adReq.addTestDevice(AdRequest.TEST_EMULATOR);              
    	adReq.addTestDevice(getString(R.string.dev_device));
    	
    	adView.loadAd(adReq);
    	adView.setId(R.id.adView);	
    	rl.addView(adView,POSITION_AD_VIEW_IN_PARENT,lp);
	}
    
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.principal_menu, menu);
        return true;
    }
    
    
    public void showPoup(View v){
    	pw = new PopupWindow(getApplicationContext());
        pw.setTouchable(true);
        pw.setFocusable(true);
        pw.setOutsideTouchable(true);
        pw.setWidth(WindowManager.LayoutParams.WRAP_CONTENT);
        pw.setHeight(WindowManager.LayoutParams.WRAP_CONTENT);
        pw.setOutsideTouchable(false);
        pw.setContentView(popupView);
        pw.showAsDropDown((View)v.getParent(), ((View)v.getParent()).getWidth(), 0);
    }
    
    
    
    public void clickMenuOption(View v) {
    	executeSelectedOption(v.getId());
    	pw.dismiss();
    }


	protected void executeSelectedOption(int optionId) {
		switch(optionId){
    		case R.id.principal_menu_myaccount:
    			Intent myAccount = new Intent(this, TRCardManagerMyAccountActivity.class);
    			startActivityForResult(myAccount, TRCardManagerApplication.MY_ACCOUNT_CLOSED);
    			break;
    		case R.id.principal_menu_settings:
    			Intent settingsUpdate = new Intent(this, TRCardManagerSettingsActivity.class);
    			startActivity(settingsUpdate);
    			break;
    		case R.id.principal_menu_logout:
    			logoutAction();
    			break;	
    		case R.id.principal_menu_restaurant:
    			Intent restaturants = new Intent(this,TRCardManagerRestaurantsActivity.class);
    			startActivity(restaturants);
    			break;
    		case R.id.principal_menu_about:
    			Intent settingsAbout = new Intent(getApplicationContext(), TRCardManagerAboutActivity.class);
    			startActivity(settingsAbout);
    			break;
    	}
	}
    
    
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
    	executeSelectedOption(item.getItemId());
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
    
    
    
	private void logoutAction(){
		AlertDialog.Builder alert = new AlertDialog.Builder(this);
		alert.setTitle(R.string.logout_dialog_question_title);
		alert.setMessage(R.string.logout_dialog_question_message);
		alert.setPositiveButton(R.string.logout_dialog_question_yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						doLogoutActions();
					}
				});
		alert.setNegativeButton(R.string.logout_dialog_question_no, null);
		alert.show();
	}
	
	private void doLogoutActions(){
		this.setResult(TRCardManagerApplication.SESSION_CLOSED);
		this.finish();
	}
	
    @Override
    public void onBackPressed() {
    	Log.i(TAG, "On Back pressed.......");
    	UserDao user = TRCardManagerApplication.getUser();
    	if(user.isConfirmationClose()){
			AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.exit_dialog_question_title);
			alert.setMessage(R.string.exit_dialog_question_message);
			alert.setPositiveButton(R.string.exit_dialog_question_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							closeApplication();
						}
					});
			alert.setNegativeButton(R.string.exit_dialog_question_no, null);
			alert.show();
    	}else{
    		closeApplication();
    	}
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
		linearMovements.setSelection(1);
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
    

    
    private void addCardsToView(final UserDao user){
    	
//    	getWindow().setFeatureInt(Window.FEATURE_CUSTOM_TITLE,
//                R.layout.custom_title_bar_layout);
    	
    	
    	RelativeLayout cardsLayout = (RelativeLayout)findViewById(R.id.layout_card_information);
    	
//    	((RelativeLayout)cardsLayout.findViewById(R.id.layout_card_number_balance)).setVisibility(View.VISIBLE);
    	
    	CardDao actualCard = user.getActualCard();
	    TextView cardNumber = (TextView)cardsLayout.findViewById(R.id.card_number);
	    cardNumber.setText(actualCard.getCardNumber());
	    TextView cardBalance = (TextView)cardsLayout.findViewById(R.id.card_balance);
    	cardBalance.setText(actualCard.getBalance());
    	
    	
	    addMovementsToView(actualCard.getMovementsData().getMovements());
	    
	    //load on background
		loadMoreMovementsBackGround(user);
    }
    
    
    private void loadMoreMovementsBackGround(final UserDao user){
    	
    	new Thread(new Runnable() {
			public void run() {
				try {
					List<MovementDao> movements = new TRCardManagerHttpCardAction().getNextMovements(user);
					user.getActualCard().getMovementsData().setNextMovements(movements);
				} catch (IOException e) {
					Log.e(this.getClass().toString(), e.getMessage(),e);
				}catch(TRCardManagerSessionException se){
					Log.e(this.getClass().toString(), se.getMessage(),se);
		    	}catch (Exception e){
					Log.e(this.getClass().toString(), e.getMessage(),e);
				}
			}
		}).start();
    }
 
    
    
}