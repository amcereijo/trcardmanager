package com.trcardmanager.action;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.adapter.RestaurantListViewAdapter;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.DirectionDao;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.location.TRCardManagerLocationAction;
import com.trcardmanager.restaurant.TRCardManagerRestaurantsActivity;

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

	public SearchRestaurantsAction() {
		activity = TRCardManagerApplication.getActualActivity();
	}
	
	public SearchRestaurantsAction(RestaurantSearchDao restaurantSearchDao,
			TRCardManagerLocationAction locationAction,SearchType searchMode) {
		this();
		this.restaurantSearchDao = restaurantSearchDao;
		this.locationAction = locationAction;
		this.searchMode = searchMode;
	}
	
	@Override
	protected void onPreExecute() {
		TextView textOfSearch = (TextView)activity.findViewById(R.id.restaurant_search_minimized_text);
		textOfSearch.setText("");
		restaurantsListView = (ListView)activity.findViewById(R.id.restaurants_list_view);
		restaurantsListView.removeAllViewsInLayout();
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.restaurants_dialog_search_title), 
				activity.getText(R.string.restaurants_dialog_direction_message));
	}
	
	@Override
	protected void onProgressUpdate(Integer... progress) {
		switch(progress[0]){
			case CANCEL_PROCESS:
				loadingDialog.cancel();
				loadingDialog.dismiss();
				break;
			case SEARCH_RESTAURANTS:
				loadingDialog.setMessage(activity.getText(R.string.restaurants_dialog_search_message));
				break;
			case SEARCH_MORE_RESTAURANTS:
				loadingDialog.setMessage(activity.getText(R.string.restaurants_dialog_search_more_message));
				break;
		}
	}
	
	
	
	private void getPhisicalDirecction() {
		try{
			userDirection = locationAction.getActualLocation(loadingDialog);
			restaurantSearchDao.setDirectionDao(userDirection);
		}catch(InterruptedException e){
			Log.e(TAG,"",e);
			throw new RuntimeException(e.getMessage());
		}
    }
	
	
	private void searchLocationFromDirection() throws IOException{
		int stateToPublish = SEARCH_MORE_RESTAURANTS;
		if(restaurantSearchDao.getDirectionDao() == null){
			stateToPublish = SEARCH_RESTAURANTS;
		}
		if(searchMode == SearchType.DIRECTION_SEARCH){
			DirectionDao directionToSearch = locationAction.getLocationFromAddress(restaurantSearchDao.getAddressSearch());
			restaurantSearchDao.setDirectionDao(directionToSearch);
		}
		publishProgress(stateToPublish);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			prepareActualRestaurantList();
			
			searchLocationFromDirection();
			
			if(searchMode == SearchType.LOCATION_SEARCH){
		    	getPhisicalDirecction();
			}
			
			searchRestaurantList();
		} catch (IOException e) {
			Log.e(TAG, e.getMessage(),e);
			cancel(true);
		} catch(Exception e){
			Log.e(TAG, e.getMessage(),e);
			cancel(true);
		}
		return null;
	}

	private void createAdapterAndSetAndListView() {
		adapter = new RestaurantListViewAdapter(activity,
				R.id.restaurants_list_view, restaurantSearchDao.getRestaurantList());
		restaurantsListView.setAdapter(adapter);
		restaurantsListView.refreshDrawableState();
		restaurantsListView.setSelection(lastViewPosition);
	}

	private void searchRestaurantList() throws IOException {
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
		List<RestaurantDao> restaurants = httpAction.getRestaurants(restaurantSearchDao);
		addFoundRestaurantsToList(restaurants);
	}

	private void prepareActualRestaurantList() {
		if(restaurantSearchDao!=null && restaurantSearchDao.getRestaurantList()!=null &&
				restaurantSearchDao.getRestaurantList().get(restaurantSearchDao.getRestaurantList().size()-1)==null){
			restaurantSearchDao.getRestaurantList().remove(restaurantSearchDao.getRestaurantList().size()-1);
		}
	}
	
	
	private void addFoundRestaurantsToList(List<RestaurantDao> restaurants){
		List<RestaurantDao> actualRestaurants = restaurantSearchDao.getRestaurantList();
		if(actualRestaurants==null){
			restaurantSearchDao.setRestaurantList(restaurants);
		}else{
			lastViewPosition = actualRestaurants.size()-1;
			actualRestaurants.addAll(restaurants);
		}
		if(restaurantSearchDao.getCurrentPage()<=restaurantSearchDao.getNumberOfPages()){
			restaurantSearchDao.getRestaurantList().add(null);
		}
	}
	
	@Override
	protected void onPostExecute(Void result) {
		publishProgress(CANCEL_PROCESS);
		if(!isCancelled()){
			TextView textOfSearch = (TextView)activity.findViewById(R.id.restaurant_search_minimized_text);
			if(searchMode == SearchType.DIRECTION_SEARCH){
				textOfSearch.setText(restaurantSearchDao.getAddressSearch());
			}
			createAdapterAndSetAndListView();
		}else{
			((TRCardManagerRestaurantsActivity)activity).showErrorRestaurantLoading();
		}
	}
	
	
	
	
}
