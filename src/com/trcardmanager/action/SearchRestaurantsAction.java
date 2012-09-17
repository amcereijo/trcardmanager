package com.trcardmanager.action;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.ListView;

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
	
	final private static String TAG = GeoDirectionAction.class.getName();
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
	
	
	public SearchRestaurantsAction() {
		activity = TRCardManagerApplication.getActualActivity();
	}
	
	public SearchRestaurantsAction(RestaurantSearchDao restaurantSearchDao,TRCardManagerLocationAction locationAction) {
		this();
		this.restaurantSearchDao = restaurantSearchDao;
		this.locationAction = locationAction;
	}
	
	@Override
	protected void onPreExecute() {
		loadingDialog = ProgressDialog.show(activity, activity.getText(R.string.restaurants_dialog_search_title), 
				activity.getText(R.string.restaurants_dialog_direction_message));
		restaurantsListView = (ListView)activity.findViewById(R.id.restaurants_list_view);
		restaurantsListView.removeAllViewsInLayout();
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
	
	private void searchLocationFromDirection() throws IOException{
		int stateToPublish = SEARCH_MORE_RESTAURANTS;
		if(restaurantSearchDao.getDirectionDao() == null){
			DirectionDao directionToSearch = locationAction.getLocationFromAddress(restaurantSearchDao.getAddressSearch());
			restaurantSearchDao.setDirectionDao(directionToSearch);
			stateToPublish = SEARCH_RESTAURANTS;
		}
		publishProgress(stateToPublish);
	}
	
	@Override
	protected Void doInBackground(Void... arg0) {
		try {
			prepareActualRestaurantList();
			
			searchLocationFromDirection();
			
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
			createAdapterAndSetAndListView();
		}else{
			((TRCardManagerRestaurantsActivity)activity).showErrorRestaurantLoading();
		}
	}
	
	
	
	
}
