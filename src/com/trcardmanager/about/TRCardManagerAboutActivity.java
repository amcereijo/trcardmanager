package com.trcardmanager.about;

import android.app.Activity;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.widget.TextView;

import com.trcardmanager.R;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerAboutActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.about_layout);
		loadAppVersion();
	}
	
	
	private void loadAppVersion(){
		String versionName = "0.0";
		PackageManager manager = this.getPackageManager();
		try {
			PackageInfo info = manager.getPackageInfo(this.getPackageName(), 0);
			versionName = info.versionName;
		} catch (NameNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		TextView versionView = (TextView)findViewById(R.id.about_version_value);
		versionView.setText(versionName);
	}
}
