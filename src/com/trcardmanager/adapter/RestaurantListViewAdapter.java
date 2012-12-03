package com.trcardmanager.adapter;

import java.util.List;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.action.RestaurantInfoAction;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.listener.TouchElementsListener;

/**
 * Adapter class to build a list of restaurants
 * @author angelcereijo
 *
 */
public class RestaurantListViewAdapter extends ArrayAdapter<RestaurantDao> {
	
	private static final String WAZE_APP_URL = "waze://?q=Hawaii";
	private static final int MAPS_ICON_POSITION = 3;
	private LayoutInflater inflater;
	private Context context;
	private ListView.LayoutParams linearLayoutParams;
	private int numberOfPages;
	private int actualPage;
	private boolean wazeInstalled;
	
	public RestaurantListViewAdapter(Context context, int textViewResourceId,
			RestaurantSearchDao resturantSearchDao) {
		this(context, textViewResourceId,resturantSearchDao.getRestaurantList());
		this.numberOfPages = resturantSearchDao.getNumberOfPages();
		this.actualPage = resturantSearchDao.getCurrentPage();
		
	}
	
	public RestaurantListViewAdapter(Context context, int textViewResourceId,
			List<RestaurantDao> objects) {
		super(context, textViewResourceId,objects);
		inflater = LayoutInflater.from(context);
		this.context = context;
		this.linearLayoutParams = getDefaultLinearLayoutParams();
		setWazeInstalled();
	}
	
	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		RestaurantDao restaurant = getItem(position);
    	if(restaurant!=null){ 
    		convertView = getRestaurantView(position, restaurant);
    	}else{
    		if(position==0){
    			convertView = createNoRestaurantLayout();
    		}else{
    			if(actualPage<numberOfPages){
    				convertView = showViewMoreRestaurants();
    			}
    		}
    	}
		return convertView;
	}
	
	
	private LinearLayout showViewMoreRestaurants(){
		return (LinearLayout)inflater.inflate(R.layout.more_restaurants_layout,null, false);
	}
	
	private LinearLayout createNoRestaurantLayout(){
		LinearLayout view = new LinearLayout(context);
			view.setLayoutParams(linearLayoutParams);
			TextView textView = new TextView(context);
			textView.setText(R.string.restaurant_data_no_restaurants);
			textView.setTextColor(Color.BLACK);
			textView.setTypeface(Typeface.create(Typeface.SERIF,Typeface.BOLD));
			textView.setGravity(Gravity.CENTER_VERTICAL);
			textView.setTextSize(textView.getTextSize()+2L);
			textView.setHeight(50);
			((LinearLayout)view).addView(textView);
		return view;
	}
	
	private View getRestaurantView(int position, RestaurantDao restaurant){
		View view;
		if(position%2!=0){
			view = (LinearLayout)inflater.inflate(R.layout.restaurant_element, null,false);
		}else{
			view = (LinearLayout)inflater.inflate(R.layout.restaurant_element_odd, null,false);
		}
		view.setLayoutParams(linearLayoutParams);
		((LinearLayout)view).addView(createAndFillDataMovementLayout(restaurant,position));
		return view;
	}
	
	
	private RelativeLayout createAndFillDataMovementLayout(final RestaurantDao restaurant, final int position){
		RelativeLayout relativeMovementLayout = (RelativeLayout)inflater.inflate(
				R.layout.restaurant_data_list_view, null,false);
		
		((TextView)relativeMovementLayout.findViewById(R.id.restaurant_data_name)).setText(restaurant.getRetaurantName());
		((TextView)relativeMovementLayout.findViewById(R.id.restaurant_data_direction)).setText(restaurant.getRestaurantDisplayDirection());
		
		TextView moreInfo = (TextView)relativeMovementLayout.findViewById(R.id.restaurant_more_info); 
		moreInfo.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				new RestaurantInfoAction(restaurant,position).execute();
			}
		});
		moreInfo.setOnTouchListener(new TouchElementsListener<TextView>());
		
		return relativeMovementLayout;
	}
	
	private ListView.LayoutParams getDefaultLinearLayoutParams(){
		return new ListView.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT);
	}
	
	
	private void setWazeInstalled(){
		 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( WAZE_APP_URL ) );
		 List<ResolveInfo> list = context.getPackageManager().queryIntentActivities(intent,     
		            PackageManager.MATCH_DEFAULT_ONLY);
		 wazeInstalled = (list.size()>0);  
	}
}
