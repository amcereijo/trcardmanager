package com.trcardmanager.action;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.RestaurantDao;
import com.trcardmanager.http.TRCardManagerHttpAction;
import com.trcardmanager.listener.TouchElementsListener;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantInfoAction extends AsyncTask<Void, Void, Void> {
	
	private final static String TAG = RestaurantInfoAction.class.toString();
	
	private static final String WAZE_APP_URL = "waze://?q=Hawaii";
	
	private RestaurantDao restaurant;
	private int position;
	private Activity activity;
	
	private boolean wazeInstalled;
    private LayoutInflater inflater;
    private AlertDialog dialog = null; 
    private boolean error = Boolean.FALSE;
    private ProgressDialog progressDialog;
    
	public RestaurantInfoAction(RestaurantDao restaurant, int position){
		this.restaurant = restaurant;
		this.position = position;
		activity = TRCardManagerApplication.getActualActivity();
		inflater = LayoutInflater.from(activity);
		setWazeInstalled();
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
		}else if(error){
			Toast.makeText(activity, "Error", Toast.LENGTH_LONG).show();
		}
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
			
			RelativeLayout buttonsLayout = (RelativeLayout)relativeMovementLayout.findViewById(R.id.restaurant_data_apps_layout);
			buttonsLayout.setId(position);
			
			if(wazeInstalled){	
				ImageButton wazeButton = (ImageButton)buttonsLayout.findViewById(R.id.restaurant_data_waze_image);
				wazeButton.setVisibility(View.VISIBLE);
				wazeButton.setOnTouchListener(new TouchElementsListener<ImageButton>());
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
	        
	        restaurant.setCompleteDataLoaded(Boolean.TRUE);
	        
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
		
		
		
		private void setWazeInstalled(){
			 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( WAZE_APP_URL ) );
			 List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,     
			            PackageManager.MATCH_DEFAULT_ONLY);
			 wazeInstalled = (list.size()>0);  
		}
}
