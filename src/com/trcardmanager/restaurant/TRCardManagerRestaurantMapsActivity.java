package com.trcardmanager.restaurant;

import android.content.res.Configuration;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.TextView;

import com.google.android.maps.MapActivity;
import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction;
import com.trcardmanager.action.SearchRestaurantsAction.SearchType;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.RestaurantSearchDao.SearchViewType;
import com.trcardmanager.listener.TouchElementsListener;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantMapsActivity extends MapActivity {
	
	private RestaurantSearchDao restaurantSearchDao;
	private SearchType searchType;
	private TRCardManagerLocationAction locationAction;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        this.requestWindowFeature(Window.FEATURE_NO_TITLE);
        
        TRCardManagerApplication.setActualActivity(this);
        setContentView(R.layout.restaurants_maps_layout);
        
	    locationAction = new TRCardManagerLocationAction();
	    restaurantSearchDao = new RestaurantSearchDao();
	    
	    ((TextView)findViewById(R.id.restaurants_maps_change_list_textView))
	    	.setOnTouchListener(new TouchElementsListener<TextView>());
	    
	    getIntentParameters();
	    
	    launchSearchRestaurantAction();
    }

	@Override
	protected boolean isRouteDisplayed() {
		return false;
	}
    
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
	}
	
	
	@Override
	public void onBackPressed() {
		setResult(TRCardManagerApplication.SEARCH_RESTAURANTS_MAP_TO_LIST_BACK_RESULT);
		super.onBackPressed();
	}
	
	
	/**
	 * 
	 * @param v
	 */
	public void changeToListView(View v){
		TRCardManagerApplication.getRestaurantSearchDao().setSearchViewType(SearchViewType.LIST_VIEW);
		setResult(TRCardManagerApplication.SEARCH_RESTAURANTS_MAP_TO_LIST);
		finish();
	}
	
	
	private void launchSearchRestaurantAction() {
		new SearchRestaurantsAction(restaurantSearchDao,locationAction,searchType,null).execute();
	}
	

	private void getIntentParameters() {
		Bundle bundle = getIntent().getExtras();
        restaurantSearchDao.setAddressSearch(bundle.getString("directiontoSearch"));
        searchType = SearchType.valueOf(bundle.getString("searchType"));
	}

	
}
