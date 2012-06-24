package com.trcardmanager.myaccount;

import android.app.TabActivity;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;
import android.widget.TabHost.TabSpec;
import android.widget.TextView;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.updatecard.TRCardManagerUpdateCardActivity;
import com.trcardmanager.updatepassword.TRCardManagerUpdatePasswordActivity;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerMyAccountActivity extends TabActivity {

	private TabHost tabHost;
	private int previousTab = 0;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.my_account_tab_layout);
		setTitle(R.string.myaccount_tab_title);
		prepareTabHost();
		addTabUpdateCard();
		addTabUpdatePassword();
		TRCardManagerApplication.setActualActivity(this);
	}

	private void prepareTabHost() {
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			public void onTabChanged(String tabId) {
				getTabWidget().getChildAt(previousTab).setBackgroundColor(Color.parseColor("#041F50"));
				getTabWidget().getChildAt(getTabHost().getCurrentTab()).setBackgroundColor(Color.parseColor("#2A4678"));
				getTabWidget().getChildAt(getTabHost().getCurrentTab()).setBackgroundResource(R.drawable.selected_tab_border);
				previousTab = getTabHost().getCurrentTab();
			}
		});
	}
	
	private void addTabUpdateCard(){
		Intent intent = new Intent(this,TRCardManagerUpdateCardActivity.class);
		TabSpec spec = tabHost.newTabSpec(getString(R.string.myaccount_tab_update_car_tab_title))
			.setIndicator(titleRelativeLayout(R.drawable.card_update_tab,
					getString(R.string.myaccount_tab_update_car_tab_title)))
			.setContent(intent);
		tabHost.addTab(spec);		
	}
	
	private void addTabUpdatePassword(){
		Intent intent = new Intent(this,TRCardManagerUpdatePasswordActivity.class);
		TabSpec spec = tabHost.newTabSpec(getString(R.string.myaccount_tab_update_password_tab_title))
			.setIndicator(titleRelativeLayout(R.drawable.password_update_tab,
					getString(R.string.myaccount_tab_update_password_tab_title)))
			.setContent(intent);
		tabHost.addTab(spec);		
	}

	
	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
	}
	
	
	private LinearLayout titleRelativeLayout(int resourceImage,String title){
		LinearLayout ly = new LinearLayout(getApplicationContext());
		ly.setOrientation(LinearLayout.VERTICAL);
		ImageView imageView = new ImageView(getApplicationContext());
		imageView.setPadding(0, 2, 0, 1);
		imageView.setImageResource(resourceImage);
		ly.addView(imageView);
		ly.addView(createTitleTabTextView(title));
		return ly;
	}
	
	private TextView createTitleTabTextView(String title){
		TextView titleTextView = new TextView(getApplicationContext());
		titleTextView.setText(title);
		titleTextView.setTextColor(Color.WHITE);
		titleTextView.setGravity(Gravity.CENTER);
		return titleTextView;
	}

}
