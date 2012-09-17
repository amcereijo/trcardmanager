package com.trcardmanager.restaurant;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.GeoDirectionAction;
import com.trcardmanager.action.SearchRestaurantsAction;
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
				findLocation();
			} catch (InterruptedException e) {
				Log.e(TAG, e.getMessage(),e);
			} catch (ExecutionException e) {
				Log.e(TAG, e.getMessage(),e);
			}
		}
	}
	
	
	public void showSearch(View v){
		showSearchSelectLayout(false);
		showMinimizedSearchLayout(false);
		showSearchLayout(true);
	}
	
	public void findInLocation(View v) throws InterruptedException, ExecutionException{
		restaurantSearchDao = new RestaurantSearchDao();
		showSearchSelectLayout(false);
		findLocation();
		showMinimizedSearchLayout(true);
	}
	
	private void findLocation() throws InterruptedException, ExecutionException {
		new GeoDirectionAction(restaurantSearchDao,locationAction).execute().get();
		findRestaurants();
	}

	
	private void findRestaurants(){
		new SearchRestaurantsAction(restaurantSearchDao,locationAction).execute();
	}

	public void showErrorRestaurantLoading(){
		Toast.makeText(this, R.string.restaurants_search_error, Toast.LENGTH_LONG).show();
		showSearch(null);
	}
	
	
	public void viewMoreRestaurants(View v){
		findRestaurants();
	}
	
	public void search(View v){
		restaurantSearchDao = new RestaurantSearchDao();
		showSearchLayout(false);
		restaurantSearchDao.setAddressSearch(((EditText)findViewById(R.id.restaurants_search_direction_text)).getText().toString());
		restaurantSearchDao.setAffiliate(((EditText)findViewById(R.id.restaurants_search_restaurant_text)).getText().toString());;
		
		findRestaurants();
		
		TextView textOfSearch = (TextView)findViewById(R.id.restaurant_search_minimized_text);
		textOfSearch.setText(restaurantSearchDao.getAddressSearch());
		showMinimizedSearchLayout(true);
		
		InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
		imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.restaurants_search_direction_text)).getWindowToken(), 0);	
	}

	
	public void showMoreSearch(View v){
		showMinimizedSearchLayout(false);
		showSearchLayout(true);
	}
	
	
	@Override
	public void onBackPressed() {
		LinearLayout selectSearchLayout = (LinearLayout)findViewById(R.id.restaurants_select_search_layout);
		if(selectSearchLayout.getVisibility() == LinearLayout.GONE){
			showSearchLayout(false);
			showMinimizedSearchLayout(false);
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
	
	private void showMinimizedSearchLayout(boolean show){
		RelativeLayout searchLayout = (RelativeLayout)findViewById(R.id.restaurants_search_minimized_layout);
		ListView restaurantListView = (ListView)findViewById(R.id.restaurants_list_view);
		if(show){
			searchLayout.setVisibility(RelativeLayout.VISIBLE);
			restaurantListView.setVisibility(RelativeLayout.VISIBLE);
		}else{
			searchLayout.setVisibility(RelativeLayout.GONE);
			restaurantListView.setVisibility(RelativeLayout.GONE);
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
