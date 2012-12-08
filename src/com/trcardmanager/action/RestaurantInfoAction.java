package com.trcardmanager.action;

import java.io.IOException;
import java.util.List;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
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
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.dao.LocationDao;
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
	private static final String GOOGLE_NAV_APP_URL = "google.navigation:q=New+York+NY";
	private final static String URI_TO_OPEN_MAPS = "http://maps.google.com/maps?z=%d&q=%s";
	private final static int ZOOM_LEVEL = 18; 
	private static final String URI_TO_OPEN_WAZE_APP = "waze://?ll=%s,%s&navigate=yes";
	private static final String URI_TO_OPEN_GOOGLE_NAV_ = "google.navigation:mode=%s&q=%s";
	private static final String GOOGLE_NAV_MODE_WALK = "walking";
	private static final String GOOGLE_NAV_MODE_DRIVE = "driving";
	
	
	private RestaurantDao restaurant;
	private int position;
	private Activity activity;
	
	private boolean wazeInstalled;
	private boolean gNavigationInstalled;
	
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
		setGNavigationInstalled();
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
	
	
	
	private void setWazeInstalled(){
		 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( WAZE_APP_URL ) );
		 List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,     
		            PackageManager.MATCH_DEFAULT_ONLY);
		 wazeInstalled = (list.size()>0);  
	}
	
	private void setGNavigationInstalled(){
		 Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( GOOGLE_NAV_APP_URL ) );
		 List<ResolveInfo> list = activity.getPackageManager().queryIntentActivities(intent,     
		            PackageManager.MATCH_DEFAULT_ONLY);
		 gNavigationInstalled = (list.size()>0);  
	}
	
	
	private class WazeClickListener implements OnClickListener{
		private RestaurantDao restaurantDao;
		public WazeClickListener(RestaurantDao restaurantDao){
			this.restaurantDao = restaurantDao;
		}
		public void onClick(View v) {
			LocationDao location = restaurantDao.getLocation();
			String urlwaze = String.format(URI_TO_OPEN_WAZE_APP,location.getLatitude(),location.getLongitude());
			Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( urlwaze ) );
			activity.startActivity(intent);
			
		}
	}

	private class GMapsClickListener implements OnClickListener{
		private RestaurantDao restaurantDao;
		public GMapsClickListener(RestaurantDao restaurantDao){
			this.restaurantDao = restaurantDao;
		}
		public void onClick(View v) {
			String uri = String.format(URI_TO_OPEN_MAPS,
					ZOOM_LEVEL,restaurantDao.getRestaurantDisplayDirection());
			activity.startActivity(new Intent(android.content.Intent.ACTION_VIEW, Uri.parse(uri)));
		}
	}
	
	private class GNavClickListener implements OnClickListener{
		private RestaurantDao restaurantDao;
		private String mode = GOOGLE_NAV_MODE_WALK; 
		public GNavClickListener(RestaurantDao restaurantDao){
			this.restaurantDao = restaurantDao;
		}
		public void onClick(View v) {
			Context ctx = TRCardManagerApplication.getActualActivity();
			AlertDialog.Builder alert = new AlertDialog.Builder(ctx);
			alert.setMessage(String.format(ctx.getString(R.string.gnavigation_dialog_open_message),
					restaurantDao.getRetaurantName()));
			alert.setPositiveButton(R.string.gnavigation_dialog_open_walking,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						mode = GOOGLE_NAV_MODE_WALK;
						openGNavigation();
					}
				});
			alert.setNegativeButton(R.string.gnavigation_dialog_open_driving,
					new DialogInterface.OnClickListener() {
				public void onClick(DialogInterface dialog, int whichButton) {
					mode = GOOGLE_NAV_MODE_DRIVE;
					openGNavigation();
				}
			});
			alert.show();
		}
		
		private void openGNavigation(){
			String urlGNavigation = String.format(URI_TO_OPEN_GOOGLE_NAV_,mode,
					restaurantDao.getRestaurantDisplayDirection());
			Intent intent = new Intent( Intent.ACTION_VIEW, Uri.parse( urlGNavigation ) );
			activity.startActivity(intent);
		}
	}
		
}
