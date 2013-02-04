package com.trcardmanager.restaurant;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.action.SearchRestaurantsAction;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao.SearchViewType;
import com.trcardmanager.listener.TouchElementsListener;
import com.trcardmanager.location.TRCardManagerLocationAction;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerRestaurantsListActivity extends Activity {

	final private static String TAG = TRCardManagerRestaurantsListActivity.class.getName();
	
	
	private TRCardManagerLocationAction locationAction;
	private ArrayAdapter<RestaurantDao> adapter;
	
	
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        //setTitle(R.string.restaurants_title);
        setContentView(R.layout.restaurants_list);
        TRCardManagerApplication.setActualActivity(this);
        
        locationAction = new TRCardManagerLocationAction();
        
        ((TextView)findViewById(R.id.restaurants_list_maps_textView))
    		.setOnTouchListener(new TouchElementsListener<TextView>());
        
        launchSearchRestaurantAction();
    }


	@Override
	public void onBackPressed() {
		setResult(TRCardManagerApplication.SEARCH_RESTAURANTS_LIST_TO_MAP_BACK_RESULT);
		super.onBackPressed();
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
	public void viewMoreRestaurants(View v){
		TRCardManagerApplication.getRestaurantSearchDao().setCurrentPage(
				TRCardManagerApplication.getRestaurantSearchDao().getCurrentPage()+1);
		TRCardManagerApplication.getRestaurantSearchDao().setSearchDone(Boolean.FALSE);
		launchSearchRestaurantAction();
	}


	private void launchSearchRestaurantAction() {
		new SearchRestaurantsAction(locationAction,adapter).execute();
	}
	
	
	/**
	 * 
	 */
	public void showErrorRestaurantLoading(){
		Toast.makeText(this, R.string.restaurants_search_error, Toast.LENGTH_LONG).show();
	}
	
}
