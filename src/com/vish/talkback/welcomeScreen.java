package com.vish.talkback;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.RadioGroup;
import android.widget.Toast;

public class welcomeScreen extends Activity {
	private Button next;
	private RadioGroup typeGroup;
	public static int SAVED_DETAILS = 0;
	
	private static int REQUEST_CODE = 5;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        
        setContentView(R.layout.main);
        
        next = (Button)findViewById(R.id.next_btn);
        typeGroup = (RadioGroup)findViewById(R.id.acc_type_group);
        
        next.setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				int radioCheckId = typeGroup.getCheckedRadioButtonId();
				
				switch(radioCheckId) {
				case R.id.gtalk_radio:
	    	        Intent gtalkIntent = new Intent(welcomeScreen.this,gtalkCfg.class);
	    	        startActivityForResult(gtalkIntent, REQUEST_CODE);
	    	        break;
				case R.id.facebook_radio:
	    	        Intent fbookIntent = new Intent(welcomeScreen.this,fbookCfg.class);
	    	        startActivityForResult(fbookIntent, REQUEST_CODE);
	    	        break;
				case R.id.bluetooth_radio:
					int mode = Activity.MODE_PRIVATE;
			        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
			        Editor prefEdit = myAppPref.edit();
			        prefEdit.putBoolean("hasAnAccount", true);
			        prefEdit.putBoolean("hasBlueAccount", true);
			        if(!prefEdit.commit()) 
			        	Log.e("Pref Error:", "Error saving prefernces hasAnAccount");
	    	        Intent blueIntent = new Intent(welcomeScreen.this,chatRoster.class);
	    	        startActivity(blueIntent);
	    	        finish();
	    	        break;
	    	    default:
	    	    	Toast.makeText(getApplicationContext(), "Please select an account type", Toast.LENGTH_SHORT).show();
	    	        break;	
				}
			}
		});
        
        
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_CODE) {
            if (resultCode == SAVED_DETAILS) {
                finish();
            } else {
            	
            }
        }
    }
}