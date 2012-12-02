package com.trcardmanager.restaurant;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.OverlayItem;
import com.trcardmanager.action.RestaurantInfoAction;
import com.trcardmanager.dao.RestaurantDao;

public class RestaurantItemOverlay extends ItemizedOverlay {
	
	
    private ArrayList<RestaurantOverlayItemDao> mOverlays = new ArrayList< RestaurantOverlayItemDao >();
    Context mContext;
    
 
    public RestaurantItemOverlay(Drawable marker, Context context) {
        super(boundCenterBottom(marker));
        mContext = context;
        
    }
 
    public void addOverlay(RestaurantOverlayItemDao overlay) {
        mOverlays.add(overlay);
        populate();
    }
 
    @Override
    protected OverlayItem createItem(int i) {
        return mOverlays.get(i);
    }
 
    @Override
    public int size() {
        return mOverlays.size();
    }
 
    
    @Override
    protected boolean onTap(int i) {
        //when you tap on the marker this will show the informations provided by you when you create in the 
        //main class the OverlayItem
    	RestaurantOverlayItemDao item = mOverlays.get(i);
        RestaurantDao restaurant = item.getRestaurantDao();
        new RestaurantInfoAction(restaurant,i).execute();
        
        return true;
    }

   
    
}
