package com.trcardmanager.action;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapView;
import com.google.android.maps.Overlay;
import com.trcardmanager.R;
import com.trcardmanager.adapter.RestaurantListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.LocationDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.dao.RestaurantSearchDao.SearchViewType;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.location.TRCardManagerLocationAction;
import com.trcardmanager.restaurant.RestaurantItemOverlay;
import com.trcardmanager.restaurant.RestaurantOverlayItemDao;
import com.trcardmanager.restaurant.TRCardManagerRestaurantMapsActivity;
import com.trcardmanager.restaurant.TRCardManagerRestaurantsListActivity;

/**
 * Async action to search restaurants
 * @author angelcereijo
 *
 */
public class SearchRestaurantsAction extends AsyncTask<Void, Integer, Void> {
	
	public enum SearchType{
		DIRECTION_SEARCH,
		LOCATION_SEARCH
	}
	
	final private static String TAG = SearchRestaurantsAction.class.getName();
	private static final int CANCEL_PROCESS = 0;
	private static final int SEARCH_RESTAURANTS = 1;
	private static final int SEARCH_MORE_RESTAURANTS = 2;
	
	private ProgressDialog loadingDialog;
	private Activity activity;
	private TRCardManagerLocationAction locationAction;
	private ListView restaurantsListView;
	private ArrayAdapter<RestaurantDao> adapter;
	private RestaurantSearchDao restaurantSearchDao;
	private int lastViewPosition = 0;
	private DirectionDao userDirection;
	private SearchType searchMode;
	private TextView textOfSearch = null;
	private boolean error = Boolean.FALSE;
	
	
	public SearchRestaurantsAction() {
		activity = TRCardManagerApplication.getActualActivity();
	}

	
	public SearchRestaurantsAction(RestaurantSearchDao restaurantSearchDao,TRCardManagerLocationAction locationAction,
			SearchType searchMode,ArrayAdapter<RestaurantDao> adapter) {
		this();
		prepareRestaurantSearch(restaurantSearchDao);
		this.locationAction = locationAction;
		this.searchMode = searchMode;
		this.adapter = adapter;
	}
	
	private void prepareRestaurantSearch(RestaurantSearchDao restaurantSearchDao){
		RestaurantSearchDao appRestaurantSearch = TRCardManagerApplication.getRestaurantSearchDao();
		if(appRestaurantSearch != null && appRestaurantSearch.isSearchDone() ){
			this.restaurantSearchDao = appRestaurantSearch;
		}else{
			this.restaurantSearchDao = restaurantSearchDao;
		}
	}
	
	@Override
	protected void onPreExecute() {
		if(restaurantSearchDao.getSearchViewType() == SearchViewType.LIST_VIEW){
			if(searchMode == SearchType.LOCATION_SEARCH){
				RelativeLayout searchedLayout = (RelativeLayout)activity.findViewById(R.id.restaurants_search_minimized_layout);
				searchedLayout.setVisibility(View.GONE);
			}
		}
		
		if(!restaurantSearchDao.isSearchDone()){	
			String message = loadStartLoadMessage();
			loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.restaurants_dialog_search_title), 
					message);
		}
	}


	
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		String message;
		switch(progress[0]){
			case CANCEL_PROCESS:
				if(loadingDialog!=null){
					loadingDialog.cancel();
					loadingDialog.dismiss();
				}
				break;
			case SEARCH_RESTAURANTS:
				if(searchMode == SearchType.DIRECTION_SEARCH){
					message = String.format(activity.getText(R.string.restaurants_dialog_search_message).toString(), restaurantSearchDao.getAddressSearch());
				}else{
					message = activity.getText(R.string.restaurants_dialog_search_message_location).toString();
				}
				loadingDialog.setMessage(message);
				break;
			case SEARCH_MORE_RESTAURANTS:
				if(searchMode == SearchType.DIRECTION_SEARCH){
					message = String.format(activity.getText(R.string.restaurants_dialog_search_more_message).toString(), restaurantSearchDao.getAddressSearch());
				}else{
					message = activity.getText(R.string.restaurants_dialog_search_more_message_location).toString();
				}
				loadingDialog.setMessage(message);
				break;
		}
	}

	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			if(!restaurantSearchDao.isSearchDone()){
				searchLocation();
				searchRestaurantList();
			}
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(),e);
			error = Boolean.TRUE;
		} catch(Exception e){
			Log.e(TAG, e.getMessage(),e);
			error = Boolean.TRUE;
		}
		return null;
	}

	
	@Override
	protected void onPostExecute(Void result) {
		publishProgress(CANCEL_PROCESS);
		if(!error){
			 if(restaurantSearchDao.getSearchViewType() == SearchViewType.MAP_VIEW){
				 printMapView();
			 }else{
				 showDirectionOfSearch();
					cleanRestaurantsView();
					createAdapterAndSetAndListView();
			 }
	         TRCardManagerApplication.setRestaurantSearchDao(restaurantSearchDao);
		}else{
			if(restaurantSearchDao.getRestaurantList() == null || restaurantSearchDao.getRestaurantList().isEmpty()){
				if(restaurantSearchDao.getSearchViewType() == SearchViewType.MAP_VIEW){
					((TRCardManagerRestaurantMapsActivity)activity).onBackPressed();
				}else{
					((TRCardManagerRestaurantsListActivity)activity).onBackPressed();
				}
			}
			Toast.makeText(activity, R.string.restaurants_search_error, Toast.LENGTH_LONG).show();
		}
	}

	
	private void printMapView(){
		MapView mapView = (MapView) activity.findViewById(R.id.mapView);

		LocationDao myLocation = restaurantSearchDao.getDirectionDao().getLocation();
		GeoPoint point = new GeoPoint((int)(myLocation.getLatitude() * 1E6), (int)(myLocation.getLongitude() * 1E6));
        //this will show you the map at the exact location you want (if you not set this you will see the map somewhere in America)
         mapView.getController().setCenter(point);
         
         //sets the zoom to see the location closer
         mapView.getController().setZoom(18);
         //this will let you to zoom in or out using the controllers
         mapView.setBuiltInZoomControls(true);
         
         List<RestaurantDao> restaurants = restaurantSearchDao.getRestaurantList();
         List<Overlay> mapOverlays = mapView.getOverlays();
         Drawable drawable = activity.getResources().getDrawable(R.drawable.map_marker);
         for(RestaurantDao r : restaurants){
              
             RestaurantItemOverlay itemizedoverlay = new RestaurantItemOverlay(drawable, activity);
      
             GeoPoint restaurantPoint = new GeoPoint(
            		 (int)(r.getLocation().getLatitude()*1E6),
            		 (int)(r.getLocation().getLongitude()*1E6));
             RestaurantOverlayItemDao overlayitem = new RestaurantOverlayItemDao(restaurantPoint, r.getRetaurantName(),
            		 r.getStreet()+", "+r.getLocality()+", "+r.getArea(), r);
             itemizedoverlay.addOverlay(overlayitem);
             mapOverlays.add(itemizedoverlay);
         }
	}
	
	private void cleanRestaurantsView() {
		restaurantsListView = (ListView)activity.findViewById(R.id.restaurants_list_view);
		restaurantsListView.removeAllViewsInLayout();
	}


	private void showDirectionOfSearch() {
		if(searchMode == SearchType.DIRECTION_SEARCH){
			textOfSearch = (TextView)activity.findViewById(R.id.restaurant_search_minimized_text);
			textOfSearch.setText(restaurantSearchDao.getAddressSearch());
		}
	}
	
	
	private void getPhisicalDirecction() {
		try{
			userDirection = locationAction.getActualLocation(loadingDialog);
			restaurantSearchDao.setDirectionDao(userDirection);
			publishProgress(SEARCH_RESTAURANTS);
		}catch(InterruptedException e){
			Log.e(TAG,"",e);
			throw new RuntimeException(e.getMessage());
		}
    }
	
	
	private void searchLocationFromDirection() throws IOException{
		DirectionDao directionToSearch = locationAction.getLocationFromAddress(restaurantSearchDao.getAddressSearch());
		restaurantSearchDao.setDirectionDao(directionToSearch);
		publishProgress(SEARCH_RESTAURANTS);
	}
	
	private void searchLocation() throws IOException{
		if(restaurantSearchDao.getDirectionDao() == null){
			switch(searchMode){
				case DIRECTION_SEARCH:
					searchLocationFromDirection();
					break;
				case LOCATION_SEARCH:
					getPhisicalDirecction();
					break;
			}
		}
	}

	private void searchRestaurantList() throws IOException {
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		List<RestaurantDao> restaurants = httpAction.getRestaurants(restaurantSearchDao);
		//TODO remove?
		addFoundRestaurantsToList(restaurants);
	}

	
	private void addFoundRestaurantsToList(List<RestaurantDao> restaurants){
		List<RestaurantDao> actualRestaurants = restaurantSearchDao.getRestaurantList();
		if(actualRestaurants==null){
			restaurantSearchDao.setRestaurantList(restaurants);
		}else{
			lastViewPosition = actualRestaurants.size()-1;
			actualRestaurants.remove(actualRestaurants.size()-1);
			actualRestaurants.addAll(restaurants);
		}
		if(restaurantSearchDao.getCurrentPage()<=restaurantSearchDao.getNumberOfPages()){
			restaurantSearchDao.getRestaurantList().add(null);
		}
	}
	
	
	private void createAdapterAndSetAndListView() {
		if(adapter == null){
			adapter = new RestaurantListViewAdapter(activity,R.id.restaurants_list_view, restaurantSearchDao);
			restaurantsListView.setAdapter(adapter);
			restaurantsListView.refreshDrawableState();
		}else{
			//Delete last("load more" view)
			RestaurantDao r = adapter.getItem(adapter.getCount()-1);
			adapter.remove(r);
			//Add all items, "addAll" method not available
			for(RestaurantDao res:restaurantSearchDao.getRestaurantList()){
				adapter.add(res);
			}
		}
		restaurantsListView.setSelection(lastViewPosition);
	}
	
	
	private String loadStartLoadMessage() {
		String message = "";
		if(restaurantSearchDao.getDirectionDao() != null){
			if(searchMode == SearchType.DIRECTION_SEARCH){
				message = String.format(activity.getText(R.string.restaurants_dialog_search_more_message).toString(),
						restaurantSearchDao.getAddressSearch());
			}else{
				message = activity.getText(R.string.restaurants_dialog_search_more_message_location).toString();
			}
		}else{
			message = activity.getText(R.string.restaurants_dialog_direction_message).toString();
		}
		return message;
	}

	
}