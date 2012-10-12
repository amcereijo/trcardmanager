package com.trcardmanager.adapter;

import java.util.List;
import java.util.regex.Pattern;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.dao.RestaurantDao;

/**
 * Adapter class to build a list of restaurants
 * @author angelcereijo
 *
 */
public class RestaurantListViewAdapter extends ArrayAdapter<RestaurantDao> {
	
	private static final int MAPS_ICON_POSITION = 3;
	private LayoutInflater inflater;
	private Context context;
	private ListView.LayoutParams linearLayoutParams;
	
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
    			convertView = showViewMoreRestaurants();
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
	
	
	private RelativeLayout createAndFillDataMovementLayout(RestaurantDao restaurant, int position){
		RelativeLayout relativeMovementLayout = (RelativeLayout)inflater.inflate(
				R.layout.restaurant_data, null,false);
		((TextView)relativeMovementLayout.findViewById(R.id.restaurant_data_name)).setText(restaurant.getRetaurantName());
		((TextView)relativeMovementLayout.findViewById(R.id.restaurant_data_direction)).setText(restaurant.getRestaurantDisplayDirection());
		String foodType = getFoodType(restaurant);
		((TextView)relativeMovementLayout.findViewById(R.id.restaurant_data_type)).setText(foodType);
		relativeMovementLayout.getChildAt(MAPS_ICON_POSITION).setId(position);
		return relativeMovementLayout;
	}

	private String getFoodType(RestaurantDao restaurant) {
		String foodTypeTitle = getContext().getResources().getText(R.string.restaurant_data_text_type).toString();
		String foodType = restaurant.getFoodType();
		if(foodTypeWithoutTitleWithValue(foodTypeTitle, foodType) ){
				foodType = foodTypeTitle+" "+foodType;
		}
		return foodType;
	}

	private boolean foodTypeWithoutTitleWithValue(String foodTypeTitle, String foodType) {
		return foodType != null && !"".equals(foodType) && 
				(!foodType.toLowerCase().contains(foodTypeTitle.toLowerCase()));
	}
	
	
	private ListView.LayoutParams getDefaultLinearLayoutParams(){
		return new ListView.LayoutParams(LinearLayout.LayoutParams.FILL_PARENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT);
	}
}
