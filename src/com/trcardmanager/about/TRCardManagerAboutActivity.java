package com.trcardmanager.about;

import android.app.Activity;
import android.app.Dialog;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.trcardmanager.R;

/**
 * 
 * @author angelcereijo
 *
 */
public class TRCardManagerAboutActivity extends Activity {

	private static final String TAG = TRCardManagerAboutActivity.class.getName();
	
	private static final String FEED_BACK_EMAIL_SUBJECT = "TRCardManager Opina";
	private static final String FEED_BACK_MESSAGE_TYPE = "text/plain";

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
			Log.e(TAG, "Error loading app version: "+e.getMessage(),e);
		}
		TextView versionView = (TextView)findViewById(R.id.about_version_value);
		versionView.setText(versionName);
	}

	
	public void showFeedBackInput(View v){
		final Dialog dialog = new Dialog(this);
	    dialog.setContentView(R.layout.feed_back);
	    dialog.setCancelable(true);
	    dialog.setTitle(R.string.feed_back_title);
	    Button sendButton = (Button) dialog.findViewById(R.id.feed_back_button);
	    sendButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				EditText text = (EditText)dialog.findViewById(R.id.feed_back_text);
				sendFeedBack(text);
				dialog.dismiss();
			}
		});
	    Button closeButton = (Button) dialog.findViewById(R.id.feed_back_close_button);
	    closeButton.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				dialog.dismiss();
			}
		});
	    dialog.show();
	}
	
	private void sendFeedBack(EditText text){
		Intent i = new Intent(Intent.ACTION_SEND);
		i.setType(FEED_BACK_MESSAGE_TYPE);
		i.putExtra(Intent.EXTRA_EMAIL  , new String[]{getText(R.string.deveolper_email1).toString()});
		i.putExtra(Intent.EXTRA_SUBJECT, FEED_BACK_EMAIL_SUBJECT);
		i.putExtra(Intent.EXTRA_TEXT   , text.getText().toString());
		try {
		    startActivity(Intent.createChooser(i, getText(R.string.feed_back_intention_text)));
		} catch (android.content.ActivityNotFoundException ex) {
		    Toast.makeText(this, R.string.feed_back_no_intention_found, Toast.LENGTH_SHORT).show();
		}
	}
	
}
