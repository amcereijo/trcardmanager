package com.trcardmanager.adapter;

import java.util.List;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.dao.RestaurantSearchDao;
import com.trcardmanager.listener.GMapsClickListener;
import com.trcardmanager.listener.GNavClickListener;
import com.trcardmanager.listener.TouchElementsListener;
import com.trcardmanager.listener.WazeClickListener;


/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantListViewAdvancedAdapter extends
		RestaurantListViewAdapter {

	private boolean wazeInstalled;
	private boolean gNavigationInstalled;
	
	private Activity activity;
	
	public RestaurantListViewAdvancedAdapter(Context context,
			int textViewResourceId, List<RestaurantDao> objects) {
		super(context, textViewResourceId, objects);
		intializeElements();
	}
	
	public RestaurantListViewAdvancedAdapter(Context context, int textViewResourceId,
			RestaurantSearchDao resturantSearchDao) {
		super(context,textViewResourceId,resturantSearchDao);
		intializeElements();
	}
	
	private void intializeElements(){
		activity = TRCardManagerApplication.getActualActivity();
		wazeInstalled = WazeClickListener.isWazeInstalled();
		gNavigationInstalled = GNavClickListener.isGNavigationInstalled();
	}
	
	@Override
	protected RelativeLayout createAndFillDataMovementLayout(
			RestaurantDao restaurant, int position) {
		
		RelativeLayout relativeMovementLayout = (RelativeLayout)inflater.inflate(
				R.layout.restaurant_data_advanced_list_view, null,false);
		
		RelativeLayout relativeMovementInfoLayout = (RelativeLayout)relativeMovementLayout.findViewById(R.id.restaurant_data_info_layout);  
		
		((TextView)relativeMovementInfoLayout.findViewById(R.id.restaurant_data_name)).setText(restaurant.getRetaurantName());
		((TextView)relativeMovementInfoLayout.findViewById(R.id.restaurant_data_direction)).setText(restaurant.getRestaurantDisplayDirection());
		String foodType = getFoodType(restaurant);
		((TextView)relativeMovementInfoLayout.findViewById(R.id.restaurant_data_type)).setText(foodType);
		((TextView)relativeMovementInfoLayout.findViewById(R.id.restaurant_data_phone)).setText(restaurant.getPhoneNumber());
		
		LinearLayout buttonsLayout = (LinearLayout)relativeMovementLayout.findViewById(R.id.restaurant_data_apps_layout);
		buttonsLayout.setId(position);
		
		if(wazeInstalled){	
			ImageButton wazeButton = (ImageButton)buttonsLayout.findViewById(R.id.restaurant_data_waze_image);
			wazeButton.setVisibility(View.VISIBLE);
			wazeButton.setOnTouchListener(new TouchElementsListener<ImageButton>());
			wazeButton.setOnClickListener(new WazeClickListener(restaurant));
		}
		if(gNavigationInstalled){
			ImageButton gNavigationButton = (ImageButton)buttonsLayout.findViewById(R.id.restaurant_data_nav_image);
			gNavigationButton.setVisibility(View.VISIBLE);
			gNavigationButton.setOnTouchListener(new TouchElementsListener<ImageButton>());
			gNavigationButton.setOnClickListener(new GNavClickListener(restaurant));
		}
		
		
		ImageButton iButtonMaps = (ImageButton)buttonsLayout.findViewById(R.id.restaurant_data_maps);
        iButtonMaps.setOnTouchListener(new TouchElementsListener<ImageButton>());
        iButtonMaps.setOnClickListener(new GMapsClickListener(restaurant));
        
		return relativeMovementLayout;
		
	}
	
	
	private String getFoodType(RestaurantDao restaurant) {
		String foodTypeTitle = activity.getResources().getText(R.string.restaurant_data_text_type).toString();
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
	


}
