package com.trcardmanager.restaurant;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction;
import com.trcardmanager.action.SearchRestaurantsAction.SearchType;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsActivity extends Activity {
	
	final private static String TAG = TRCardManagerRestaurantsActivity.class.getName();
	
	private TRCardManagerLocationAction locationAction;
	private RestaurantSearchDao restaurantSearchDao;
	private SearchType searchType;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.restaurants);
		setTitle(R.string.restaurants_title);
		TRCardManagerApplication.setActualActivity(this);
		locationAction = new TRCardManagerLocationAction();
	};
	
	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		if (requestCode == TRCardManagerApplication.GPS_ACTIVATED){
    		try {
				findRestaurants();
			} catch (RuntimeException e){
				Log.e(TAG, e.getMessage(),e);
				showErrorRestaurantLoading();
			}
		}
	}
	
	
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
		showRestaurantList(false);
		showRestaurantsSearchMinimizedLayout(false);
		
	}
	
	/**
	 * 
	 * @param v
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void findInLocation(View v) throws InterruptedException, ExecutionException{
		restaurantSearchDao = new RestaurantSearchDao();
		showSearchSelectLayout(false);		
		findLocation();
		showRestaurantsSearchMinimizedLayout(false);
		showRestaurantList(true);
	}
	
	private void findLocation() throws InterruptedException, ExecutionException {
		restaurantSearchDao = new RestaurantSearchDao();
		searchType = SearchType.LOCATION_SEARCH;
		checkGPSLocationAndStartSearch();
	}


	private void checkGPSLocationAndStartSearch() {
		if(!locationAction.isGpsActive()){
			final AlertDialog.Builder alert = new AlertDialog.Builder(this);
			alert.setTitle(R.string.restaurants_no_gps_title);
			alert.setMessage(R.string.restaurants_no_gps_message);
			alert.setPositiveButton(android.R.string.yes,
					new DialogInterface.OnClickListener() {
						public void onClick(DialogInterface dialog, int whichButton) {
							startActivityForResult(new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),
									TRCardManagerApplication.GPS_ACTIVATED);
						}
					});
			alert.setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
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
		new SearchRestaurantsAction(restaurantSearchDao,locationAction,searchType).execute();
	}

	public void showErrorRestaurantLoading(){
		Toast.makeText(this, R.string.restaurants_search_error, Toast.LENGTH_LONG).show();
		showSearch(null);
	}
	
	
	public void viewMoreRestaurants(View v){
		findRestaurants();
	}
	
	/**
	 * 
	 * @param v
	 */
	public void search(View v){
		restaurantSearchDao = new RestaurantSearchDao();
		showSearchLayout(false);
		restaurantSearchDao.setAddressSearch(((EditText)findViewById(R.id.restaurants_search_direction_text)).getText().toString());
		//restaurantSearchDao.setAffiliate(((EditText)findViewById(R.id.restaurants_search_restaurant_text)).getText().toString());;
		searchType = SearchType.DIRECTION_SEARCH;
		findRestaurants();
		
		showRestaurantsSearchMinimizedLayout(true);
		showRestaurantList(true);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.restaurants_search_direction_text)).getWindowToken(), 0);	
	}

	
	public void showMoreSearch(View v){
		showRestaurantsSearchMinimizedLayout(false);
		showRestaurantList(false);
		showSearchLayout(true);
	}
	
	
	@Override
	public void onBackPressed() {
		LinearLayout selectSearchLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(selectSearchLayout.getVisibility() == LinearLayout.GONE){
			showSearchLayout(false);
			showRestaurantList(false);
			showRestaurantsSearchMinimizedLayout(false);
			showSearchSelectLayout(true);
		}else{
			super.onBackPressed();
		}
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
	
	private void showRestaurantList(boolean show){
		ListView restaurantListView = (ListView)findViewById(R.id.restaurants_list_view);
		RelativeLayout searchRestaurantResultView = (RelativeLayout)findViewById(R.id.restaurants_search_results_layout);
		if(show){
			searchRestaurantResultView.setVisibility(View.VISIBLE);
			restaurantListView.setVisibility(View.VISIBLE);
		}else{
			searchRestaurantResultView.setVisibility(View.GONE);
			restaurantListView.setVisibility(View.GONE);
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
	
	private void showRestaurantsSearchMinimizedLayout(boolean show){
		RelativeLayout restaurantsSearchMinimizedLayout = (RelativeLayout)findViewById(R.id.restaurants_search_minimized_layout);
		if(show){
			restaurantsSearchMinimizedLayout.setVisibility(RelativeLayout.VISIBLE);
		}else{
			restaurantsSearchMinimizedLayout.setVisibility(RelativeLayout.GONE);
		}
	}
}
