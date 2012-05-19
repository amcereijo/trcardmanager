package com.trcardmanager.adapter;

import java.util.List;

import com.trcardmanager.R;
import com.trcardmanager.dao.MovementDao;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

/**
 * Adapter to build movement list
 * @author angelcereijo
 *
 */
public class MovementsListViewAdapter extends ArrayAdapter<MovementDao> {
	
	private LayoutInflater inflater;
	
	public MovementsListViewAdapter(Context context, int textViewResourceId,
			List<MovementDao> objects) {
		super(context, textViewResourceId, objects);
		inflater = LayoutInflater.from(context);
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		ListView.LayoutParams lp = new ListView.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, 
    			LinearLayout.LayoutParams.WRAP_CONTENT);
    	
    	MovementDao movement = getItem(position);

		if(position%2!=0){
			convertView = (LinearLayout) inflater.inflate(R.layout.card_movement, null,false);
		}else{
			convertView = (LinearLayout)inflater.inflate(R.layout.card_movement_odd, null,false);
		}
		convertView.setLayoutParams(lp);
		
		((LinearLayout)convertView).addView(createAndFillDataMovementLayout(movement));
    		    	
		return convertView;
	}
	
	private RelativeLayout createAndFillDataMovementLayout(MovementDao movement){
		RelativeLayout relativeMovementLayout = (RelativeLayout)inflater.inflate(
				R.layout.movement_relative_layout, null,false);
		
		((TextView)relativeMovementLayout.findViewById(R.id.hour_movement)).setText(movement.getHour()+" -");
		((TextView)relativeMovementLayout.findViewById(R.id.date_movement)).setText(movement.getDate());
		((TextView)relativeMovementLayout.findViewById(R.id.operation_movement)).setText(movement.getOperationType());
		((TextView)relativeMovementLayout.findViewById(R.id.balance_movement)).setText(movement.getAmount());
		((TextView)relativeMovementLayout.findViewById(R.id.state_movement)).setText(movement.getState());
		((TextView)relativeMovementLayout.findViewById(R.id.trade_movement)).setText(movement.getTrade());
		
		return relativeMovementLayout;
	}
	
}
