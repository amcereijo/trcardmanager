package com.trcardmanager.action;

import java.io.IOException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.listener.GMapsClickListener;
import com.trcardmanager.listener.GNavClickListener;
import com.trcardmanager.listener.TouchElementsListener;
import com.trcardmanager.listener.WazeClickListener;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantInfoAction extends AsyncTask<Void, Void, Void> {
	
	private final static String TAG = RestaurantInfoAction.class.toString();
	
	private RestaurantDao restaurant;
	private int position;
	private Activity activity;
	
	private boolean wazeInstalled;
	private boolean gNavigationInstalled;
	
    private LayoutInflater inflater;
    private AlertDialog dialog = null; 
    private boolean error = Boolean.FALSE;
    private ProgressDialog progressDialog;
    
    /**
     * 
     * @param restaurant
     * @param position
     */
	public RestaurantInfoAction(RestaurantDao restaurant, int position){
		this.restaurant = restaurant;
		this.position = position;
		activity = TRCardManagerApplication.getActualActivity();
		inflater = LayoutInflater.from(activity);
		wazeInstalled = WazeClickListener.isWazeInstalled();
		gNavigationInstalled = GNavClickListener.isGNavigationInstalled();
	}
	
	
	@Override
	protected void onPreExecute() {
		if(!restaurant.isCompleteDataLoaded()){
			progressDialog = ProgressDialog.show(activity, restaurant.getRetaurantName(),
					"Recuperando información");
			progressDialog.setInverseBackgroundForced(true);
		}
	}
	
	@Override
	protected Void doInBackground(Void... params) {
		TRCardManagerHttpAction httpAction = new TRCardManagerHttpAction();
        try {
        	if(!restaurant.isCompleteDataLoaded()){
        		httpAction.completeResaturantInfo(restaurant);
        	}
        } catch (IOException e) {
        	error = Boolean.TRUE;
			Log.e(TAG, "Error loading restaurant info",e);
		}
        return null;
	}
	
	
	@Override
	protected void onPostExecute(Void result) {
		if(progressDialog!=null){
			progressDialog.cancel();
		}
		if(!error){
			restaurant.setCompleteDataLoaded(Boolean.TRUE);
		}
		dialog = new AlertDialog.Builder(activity).create();
		dialog.setInverseBackgroundForced(true);
        View view = createAndFillDataMovementLayout(restaurant,position);
        dialog.setView(view);
		dialog.show();
		dialog.setOnDismissListener(new DialogInterface.OnDismissListener(){ 				
			public void onDismiss(DialogInterface dialog) {
				dialog.cancel();
			}			
		});
	}

	
	 private RelativeLayout createAndFillDataMovementLayout(RestaurantDao restaurant, int position){
		RelativeLayout relativeMovementLayout = (RelativeLayout)inflater.inflate(
				R.layout.restaurant_data, null,false);
		
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
		
		TextView restaurantDataClose = (TextView)relativeMovementInfoLayout.findViewById(R.id.restaurant_data_close);
		restaurantDataClose.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.hide();
				dialog.cancel();
			}
		});
		
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
