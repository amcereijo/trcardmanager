package com.trcardmanager.restaurant;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.drawable.Drawable;

import com.google.android.maps.ItemizedOverlay;
import com.trcardmanager.action.RestaurantInfoAction;
import com.trcardmanager.dao.RestaurantDao;

/**
 * 
 * @author angelcereijo
 *
 */
public class RestaurantItemOverlay extends ItemizedOverlay<RestaurantOverlayItemDao> {
	
	
    private ArrayList<RestaurantOverlayItemDao> mOverlays = new ArrayList< RestaurantOverlayItemDao >();
    
 
    public RestaurantItemOverlay(Drawable marker) {
        super(boundCenterBottom(marker));
    }
    
 
    public void addOverlay(RestaurantOverlayItemDao overlay) {
        mOverlays.add(overlay);
        populate();
    }
 
    @Override
    protected RestaurantOverlayItemDao createItem(int i) {
        return mOverlays.get(i);
    }
 
    @Override
    public int size() {
        return mOverlays.size();
    }
 
    
    @Override
    protected boolean onTap(int i) {
    	RestaurantOverlayItemDao item = mOverlays.get(i);
        RestaurantDao restaurant = item.getRestaurantDao();
        new RestaurantInfoAction(restaurant,i).execute();
        return true;
    }

   
    
}
