package com.trcardmanager.restaurant;

import java.util.concurrent.ExecutionException;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.provider.Settings;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.view.Window;
import android.view.inputmethod.InputMethodManager;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.RestaurantSearchDao.SearchType;
import com.trcardmanager.dao.RestaurantSearchDao.SearchViewType;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsActivity extends Activity {
	
	final private static String TAG = TRCardManagerRestaurantsActivity.class.getName();
	
	private RestaurantSearchDao directiontoSearch;
	
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		
		setContentView(R.layout.restaurants);
		
		TRCardManagerApplication.setActualActivity(this);
	}
	
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
		}else if(requestCode == TRCardManagerApplication.SEARCH_RESTAURANTS_MAP_TO_LIST && 
				(resultCode != TRCardManagerApplication.SEARCH_RESTAURANTS_MAP_TO_LIST_BACK_RESULT
						|| TRCardManagerApplication.getRestaurantSearchDao().getSearchType() == SearchType.DIRECTION_SEARCH)){
			TRCardManagerApplication.getRestaurantSearchDao().setSearchViewType(SearchViewType.LIST_VIEW);
			findRestaurants();
		}else if(requestCode == TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP &&
				(resultCode != TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP_BACK_RESULT
						|| TRCardManagerApplication.getRestaurantSearchDao().getSearchType() == SearchType.LOCATION_SEARCH)){
			TRCardManagerApplication.getRestaurantSearchDao().setSearchViewType(SearchViewType.MAP_VIEW);
			findRestaurants();
		}
	}
	

	@Override
	public void onBackPressed() {
		RelativeLayout relativeLayoutTitleSearch = (RelativeLayout)findViewById(R.id.layout_title_advanced);
		if(relativeLayoutTitleSearch != null){
			setContentView(R.layout.restaurants);
		}else{
			super.onBackPressed();
		}
	}
	
	/**
	 * 
	 * @param v
	 */
	public void showSearch(View v){
		setContentView(R.layout.restaurant_search_advanced);
		ScrollView searchLayout = (ScrollView)findViewById(R.id.restaurants_search_scroll_layout);
		setCustomSpinnerItems(searchLayout);
	}
	
	/**
	 * 
	 * @param v
	 * @throws InterruptedException
	 * @throws ExecutionException
	 */
	public void findInLocation(View v){
		TRCardManagerApplication.setRestaurantSearchDao(null);
		
		TRCardManagerApplication.getRestaurantSearchDao().setSearchType(SearchType.LOCATION_SEARCH);
		
		
		checkGPSLocationAndStartSearch();
	}
	
	
	
	/**
	 * 
	 * @param v
	 */
	public void search(View v){
		
		TRCardManagerApplication.setRestaurantSearchDao(null);
		directiontoSearch = TRCardManagerApplication.getRestaurantSearchDao();
		
		saveDataDirectionSearch();
		
		//restaurants_search_restaurant_text
		directiontoSearch.setAffiliate(((EditText)findViewById(R.id.restaurants_search_restaurant_text)).getText().toString());
		
		
		if("".equals(directiontoSearch.getAddressSearch()) && "".equals(directiontoSearch.getAffiliate())){
			//No data to search
			Toast.makeText(this, R.string.restaurants_search_adv_no_data_in, Toast.LENGTH_LONG).show();
		}else{
			//restaurants_search_foodtype_spinner
			int position = ((Spinner)findViewById(R.id.restaurants_search_foodtype_spinner)).getSelectedItemPosition();
			String[] foodTypesValues = getResources().getStringArray(R.array.food_types_values);
			directiontoSearch.setFoodType(foodTypesValues[position]);
			
			directiontoSearch.setSearchViewType(SearchViewType.LIST_VIEW);
				
			directiontoSearch.setSearchType(SearchType.DIRECTION_SEARCH);
			
			findRestaurants();
			
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.restaurants_search_locality_text)).getWindowToken(), 0);
		}
	}


	protected void saveDataDirectionSearch() {
		DirectionDao directionDao = new DirectionDao();
		//restaurants_search_locality_text
		directionDao.setLocality(((EditText)findViewById(R.id.restaurants_search_locality_text)).getText().toString());
		//restaurants_search_subarea_text
		directionDao.setSubArea(((EditText)findViewById(R.id.restaurants_search_subarea_text)).getText().toString());
		//restaurants_search_cp_text
		directionDao.setPostalCode(((EditText)findViewById(R.id.restaurants_search_cp_text)).getText().toString());
		
		//restaurants_search_vtype_spinner
		directionDao.setAddressType(((Spinner)findViewById(R.id.restaurants_search_vtype_spinner)).getSelectedItem().toString());
		//restaurants_search_street_text
		directionDao.setStreet(((EditText)findViewById(R.id.restaurants_search_street_text)).getText().toString());
		if(directionDao.getStreet()!=null && !"".equals(directionDao.getStreet())){
			directionDao.setStreetNumber(directionDao.getStreet().replaceAll("[^0-9]", ""));
			directionDao.setStreet(directionDao.getStreet().replaceAll("[^A-Za-z ]", "").trim());
		}
		
		directiontoSearch.setDirectionDao(directionDao);
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
		if(TRCardManagerApplication.getRestaurantSearchDao().getSearchViewType() == 
			SearchViewType.MAP_VIEW){
			findRestaurantsInMap();
		}else{
			findRestarurantsInList();
		}
	}

	
	private void findRestarurantsInList(){
		Intent restaturants = new Intent(this,TRCardManagerRestaurantsListActivity.class);
		startActivityForResult(restaturants, TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP);
	}
	
	private void findRestaurantsInMap(){
		Intent restaturants = new Intent(this,TRCardManagerRestaurantMapsActivity.class);
		startActivityForResult(restaturants,TRCardManagerApplication.SEARCH_RESTAURANTS_MAP_TO_LIST);
	}


	private void setCustomSpinnerItems(ScrollView searchLayout) {
		Spinner foodTypeSpinner = (Spinner) searchLayout.findViewById(R.id.restaurants_search_foodtype_spinner);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> arrayAdapter = (ArrayAdapter<String>)foodTypeSpinner.getAdapter();
			arrayAdapter.setDropDownViewResource(R.layout.search_spinner_item);
		Spinner vTypeSpinner = (Spinner) searchLayout.findViewById(R.id.restaurants_search_vtype_spinner);
		@SuppressWarnings("unchecked")
		ArrayAdapter<String> vArrayAdapter = (ArrayAdapter<String>)vTypeSpinner.getAdapter();
		vArrayAdapter.setDropDownViewResource(R.layout.search_spinner_item);
		
		
		//add on click to hide keyboard
		foodTypeSpinner.setOnTouchListener(new SpinnerTouchListener());
		vTypeSpinner.setOnTouchListener(new SpinnerTouchListener());
	}


	
	/**
	 * private class to hide keyboard on touch a spinner
	 * @author angelcereijo
	 *
	 */
	private class SpinnerTouchListener implements OnTouchListener {
		
		public boolean onTouch(View v, MotionEvent event) {
			InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
			imm.hideSoftInputFromWindow(((EditText)findViewById(R.id.restaurants_search_locality_text)).getWindowToken(), 0);
			return false;
		}
	}
	
}
