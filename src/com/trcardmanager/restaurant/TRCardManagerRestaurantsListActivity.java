package com.trcardmanager.restaurant;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction;
import com.trcardmanager.action.SearchRestaurantsAction.SearchType;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsListActivity extends Activity {
	
	final private static String TAG = TRCardManagerRestaurantsListActivity.class.getName();
	
	private final static String URI_TO_OPEN_MAPS = "geo:%s,%s?z=%d&q=%s";
	private final static int ZOOM_LEVEL = 18; 
	
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


	private void getIntentParameters() {
		Bundle bundle = getIntent().getExtras();
        restaurantSearchDao.setAddressSearch(bundle.getString("directiontoSearch"));
        searchType = SearchType.valueOf(bundle.getString("searchType"));
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
		int restaurantPosution = v.getId();
		RestaurantDao restaurantDao = restaurantSearchDao.getRestaurantList().get(restaurantPosution);
		LocationDao location = restaurantDao.getLocation();
		String uri = String.format(URI_TO_OPEN_MAPS,location.getLatitude(), location.getLongitude(),
				ZOOM_LEVEL,restaurantDao.getRestaurantDisplayDirection());
		startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
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
