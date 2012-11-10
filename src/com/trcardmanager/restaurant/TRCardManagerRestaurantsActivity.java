package com.trcardmanager.restaurant;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;

import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction.SearchType;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsActivity extends Activity {
	
	final private static String TAG = TRCardManagerRestaurantsActivity.class.getName();
	
	private SearchType searchType;
	private String directiontoSearch;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurants);
		setTitle(R.string.restaurants_title);
		TRCardManagerApplication.setActualActivity(this);
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		TRCardManagerApplication.setActualActivity(this);
	}
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TRCardManagerApplication.GPS_ACTIVATED){
			findRestaurants();
		}
	}
	

	@Override
	public void onBackPressed() {
		LinearLayout selectSearchLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(selectSearchLayout.getVisibility() == LinearLayout.GONE){
			showSearchLayout(false);
			showSearchSelectLayout(true);
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * 
	 * @param v
	 */
	public void showSearch(View v){
		if(v!=null){
			searchType = SearchType.DIRECTION_SEARCH;
		}
		boolean showSearchSelectLayout = false;
		boolean showSearchLayout = true;
		if(searchType == SearchType.LOCATION_SEARCH){
			showSearchSelectLayout = true;
			showSearchLayout = false ;
		}
		showSearchSelectLayout(showSearchSelectLayout);
		showSearchLayout(showSearchLayout);	
	}
	
	/**
	 * 
	 * @param v
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void findInLocation(View v){
		searchType = SearchType.LOCATION_SEARCH;
		checkGPSLocationAndStartSearch();
	}
	
	
	
	/**
	 * 
	 * @param v
	 */
	public void search(View v){
		directiontoSearch = ((EditText)findViewById(R.id.restaurants_search_direction_text)).getText().toString();
		searchType = SearchType.DIRECTION_SEARCH;
		findRestaurants();
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.restaurants_search_direction_text)).getWindowToken(), 0);	
	}


	private void checkGPSLocationAndStartSearch() {
		TRCardManagerLocationAction locationAction = new TRCardManagerLocationAction();
		if(!locationAction.isGpsActive()){
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.restaurants_no_gps_title);
			alert.setMessage(R.string.restaurants_no_gps_message);
			alert.setPositiveButton(R.string.restaurants_no_gps_yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
									TRCardManagerApplication.GPS_ACTIVATED);
						}
					});
			alert.setNegativeButton(R.string.restaurants_no_gps_no, new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					findRestaurants();
				}
			});
			runOnUiThread(new Runnable() {
			    public void run() {
			    	alert.show();
			    }
			});
		}else{
			findRestaurants();
		}
	}


	private void findRestaurants(){
		Intent restaturants = new Intent(this,TRCardManagerRestaurantsListActivity.class);
		
		restaturants.putExtra("directiontoSearch", directiontoSearch);
		restaturants.putExtra("searchType", searchType.name());
		
		startActivity(restaturants);
	}
	
	
	private void showSearchLayout(boolean show){
		RelativeLayout searchLayout = (RelativeLayout)findViewById(R.id.restaurants_search_layout);
		if(show){
			searchLayout.setVisibility(RelativeLayout.VISIBLE);
		}else{
			getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
			searchLayout.setVisibility(RelativeLayout.GONE);
		}
	}
	

	private void showSearchSelectLayout(boolean show){
		LinearLayout searchSelectLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(show){
			searchSelectLayout.setVisibility(LinearLayout.VISIBLE);
		}else{
			searchSelectLayout.setVisibility(LinearLayout.GONE);
		}
	}
	
}
