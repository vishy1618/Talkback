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
import android.widget.EditText;
import android.widget.Toast;

public class gtalkCfg extends Activity {
	DBManager db;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.gtalk_cfg);
        setResult(-1);
        
        final Button discard = (Button)findViewById(R.id.gtalk_discard);
        discard.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				try {
					setResult(-1);
					finish();
				} catch (Throwable e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
        
        final Button save = (Button)findViewById(R.id.gtalk_save);
        save.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onSave();
			}
		});
    }
    
    
    private void onSave() {
    	EditText userText = (EditText)findViewById(R.id.gtalk_user);
    	EditText passText = (EditText)findViewById(R.id.gtalk_passwd);
    	if(userText.getText().toString().equals("") || passText.getText().toString().equals("") || !userText.getText().toString().contains("@")) {
    		Toast.makeText(getApplicationContext(), "Please enter the username/password.", Toast.LENGTH_SHORT).show();
    		return;
    	}
    	
    	db = new DBManager(this);
    	String password = null;
		try {
			password = StringEncrypter.encrypt(userText.getText().toString(), passText.getText().toString());
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    	String[] columns = new String[]{userText.getText().toString(),password};
    	
    	db.addRow("gtalk_cfg", columns);
    	//delete after testing
    	//db.deleteAllRows("gtalk_cfg"); 
    	
    	db.closeConnection();
    	
    	//set has account pref to true and has gtalk pref to true
    	int mode = Activity.MODE_PRIVATE;
        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
        Editor prefEdit = myAppPref.edit();
        prefEdit.putBoolean("hasAnAccount", true);
        prefEdit.putBoolean("hasGtalkAccount", true);
        if(!prefEdit.commit()) 
        	Log.e("Pref Error:", "Error saving prefernces hasAnAccount");
        
        //start the chat roster
        Intent chatRosterIntent = new Intent(gtalkCfg.this,chatRoster.class);
        startActivity(chatRosterIntent);
        setResult(welcomeScreen.SAVED_DETAILS);
        finish();
    }
}