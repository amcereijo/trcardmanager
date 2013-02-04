package com.trcardmanager.about;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;
import com.trcardmanager.application.TRCardManagerApplication;
import com.trcardmanager.listener.TouchElementsListener;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerAboutActivity extends Activity {

	private static final String TAG = TRCardManagerAboutActivity.class.getName();
	
	private static final String FEED_BACK_EMAIL_SUBJECT = "TRCardManager Opina";
	private static final String SEND_EMAIL_STRING_FORMAT = "mailto:%s?subject=%s";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		//setTitle(R.string.about_version_title);
		setContentView(R.layout.about_layout);
		loadAppVersion();
		TRCardManagerApplication.setActualActivity(this);
		Button b = (Button)findViewById(R.id.about_button_opinon);
		b.setOnTouchListener(new TouchElementsListener<Button>(Color.BLUE));
	}
	
	
	private void loadAppVersion(){
		String versionName = "0.0";
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			Log.e(TAG, "Error loading app version: "+e.getMessage(),e);
		}
		TextView versionView = (TextView)findViewById(R.id.about_version_value);
		versionView.setText(versionName);
	}

	
	public void showFeedBackInput(View v){
		Intent intent = new Intent(Intent.ACTION_VIEW);
		String email = getText(R.string.deveolper_email1).toString();
		Uri data = Uri.parse(String.format(SEND_EMAIL_STRING_FORMAT, email, FEED_BACK_EMAIL_SUBJECT));
		intent.setData(data);
		try {
			startActivity(intent);
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, R.string.feed_back_no_intention_found, Toast.LENGTH_SHORT).show();
		}
	}
	
}
