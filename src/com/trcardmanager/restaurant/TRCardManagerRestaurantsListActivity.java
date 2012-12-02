package com.trcardmanager.restaurant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction;
import com.trcardmanager.action.SearchRestaurantsAction.SearchType;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.RestaurantSearchDao.SearchViewType;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsListActivity extends Activity {

	final private static String TAG = TRCardManagerRestaurantsListActivity.class.getName();
	
	private final static String URI_TO_OPEN_MAPS = "http://maps.google.com/maps?z=%d&q=%s";
	private final static int ZOOM_LEVEL = 18; 
	private static final String URL_WAZE_APP = "waze://?ll=%s,%s&navigate=yes";
	
	private TRCardManagerLocationAction locationAction;
	private RestaurantSearchDao restaurantSearchDao;
	private SearchType searchType;
	private ArrayAdapter<RestaurantDao> adapter;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        setTitle(R.string.restaurants_title);
        setContentView(R.layout.restaurants_list);
        TRCardManagerApplication.setActualActivity(this);
        
        locationAction = new TRCardManagerLocationAction();
        restaurantSearchDao = new RestaurantSearchDao();
        
        getIntentParameters();
        
        launchSearchRestaurantAction();
    }


	@Override
	public void onBackPressed() {
		setResult(TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP_BACK_RESULT);
		super.onBackPressed();
	}
	
	private void getIntentParameters() {
		Bundle bundle = getIntent().getExtras();
        restaurantSearchDao.setAddressSearch(bundle.getString("directiontoSearch"));
        searchType = SearchType.valueOf(bundle.getString("searchType"));
	}

    
	/**
	 * 
	 * @param v
	 */
	public void changeToMapsView(View v){
		TRCardManagerApplication.getRestaurantSearchDao().setSearchViewType(SearchViewType.MAP_VIEW);
		setResult(TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP);
		finish();
	}
	
	
    /**
	 * 
	 * @param v
	 */
	public void showMoreSearch(View v){
		onBackPressed();
	}
	
	/**
	 * 
	 * @param v
	 */
	public void openMap(View v){
		int restaurantPosution = ((View)v.getParent()).getId();
		RestaurantDao restaurantDao = restaurantSearchDao.getRestaurantList().get(restaurantPosution);
		String uri = String.format(URI_TO_OPEN_MAPS,
				ZOOM_LEVEL,restaurantDao.getRestaurantDisplayDirection());
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void openWaze(View v){
		RelativeLayout wazeLayou = (RelativeLayout)v.getParent();
		int restaurantPosution = ((View)wazeLayou.getParent()).getId();
		RestaurantDao restaurantDao = restaurantSearchDao.getRestaurantList().get(restaurantPosution);
		LocationDao location = restaurantDao.getLocation();
		
		String urlwaze = String.format(URL_WAZE_APP,location.getLatitude(),location.getLongitude());
		Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( urlwaze ) );
		startActivity(intent);
	}
	
	/**
	 * 
	 * @param v
	 */
	public void viewMoreRestaurants(View v){
		launchSearchRestaurantAction();
	}


	private void launchSearchRestaurantAction() {
		new SearchRestaurantsAction(restaurantSearchDao,locationAction,searchType,adapter).execute();
	}
	
	
	/**
	 * 
	 */
	public void showErrorRestaurantLoading(){
		Toast.makeText(this, R.string.restaurants_search_error, Toast.LENGTH_LONG).show();
	}
	
}
