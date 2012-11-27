package com.trcardmanager.restaurant;

import java.util.List;

import android.content.res.Configuration;
import android.graphics.drawable.Drawable;
import android.os.Bundle;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;
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
public class TRCardManagerRestaurantMapsActivity extends MapActivity {

	private MapView mapView;
	private RestaurantSearchDao restaurantSearchDao;
	private SearchType searchType;
	private TRCardManagerLocationAction locationAction;
	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setTitle(R.string.restaurants_title);
        TRCardManagerApplication.setActualActivity(this);
        setContentView(R.layout.activity_trcard_manager_restaurant_maps);
        
        
        if(restaurantSearchDao == null ){
	        locationAction = new TRCardManagerLocationAction();
	        restaurantSearchDao = new RestaurantSearchDao();
	        
	        getIntentParameters();
	        
	        mapView = (MapView) findViewById(R.id.mapView);
	        
	        //sets the zoom to see the location closer
	        mapView.getController().setZoom(18);
	 
	        //this will let you to zoom in or out using the controllers
	        mapView.setBuiltInZoomControls(true);

	        launchSearchRestaurantAction();
        }

    }

	@Override
	protected boolean isRouteDisplayed() {
		// TODO Auto-generated method stub
		return false;
	}
    
	
	@Override
	public void onConfigurationChanged(Configuration newConfig) {
		super.onConfigurationChanged(newConfig);
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
