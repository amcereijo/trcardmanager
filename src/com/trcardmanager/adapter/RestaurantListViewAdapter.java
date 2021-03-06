package com.trcardmanager.adapter;

import java.util.List;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
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
	
	protected LayoutInflater inflater;
	protected Context context;
	protected ListView.LayoutParams linearLayoutParams;
	protected int numberOfPages;
	protected int actualPage;
	
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
		view = loadRestaurantElementLayout(position);
		view.setLayoutParams(linearLayoutParams);
		((LinearLayout)view).addView(createAndFillDataMovementLayout(restaurant,position));
		return view;
	}

	private View loadRestaurantElementLayout(int position) {
		View view;
		if(position%2!=0){
			view = (LinearLayout)inflater.inflate(R.layout.restaurant_element, null,false);
		}else{
			view = (LinearLayout)inflater.inflate(R.layout.restaurant_element_odd, null,false);
		}
		return view;
	}
	
	
	protected RelativeLayout createAndFillDataMovementLayout(final RestaurantDao restaurant, final int position){
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
	
}
