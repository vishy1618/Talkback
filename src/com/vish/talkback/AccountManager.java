package com.vish.talkback;

import java.util.ArrayList;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.CompoundButton.OnCheckedChangeListener;

public class AccountManager extends Activity {
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.account_manager);
		
		Button cancelButton = (Button)findViewById(R.id.acc_cancel);
		cancelButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				finish();
			}
		});
		
		Button saveButton = (Button)findViewById(R.id.acc_save);
		saveButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				onSaveClicked();
				
			}
		});
		
		//adding previous entries to edit texts
		fillEdits();
	}
	
	private void fillEdits() {
		DBManager db = new DBManager(this);
		
		//get edit text references
		final EditText gusername = (EditText)findViewById(R.id.guser_edit);
		final EditText gpassword = (EditText)findViewById(R.id.gpass_edit);
		
		final EditText fusername = (EditText)findViewById(R.id.fuser_edit);
		final EditText fpassword = (EditText)findViewById(R.id.fpass_edit);
		
		final TextView guserView = (TextView)findViewById(R.id.guser_view);
		final TextView fuserView = (TextView)findViewById(R.id.fuser_view);
		final TextView gpassView = (TextView)findViewById(R.id.gpass_view);
		final TextView fpassView = (TextView)findViewById(R.id.fpass_view);
		
		//retreive from db
		//gtalk
		ArrayList<Object> row;
		String username;
		String password;
		try {
			row = db.getAllRowsAsArrays("gtalk_cfg").get(0);
			username = (String) row.get(0);
			password = null;
			try {
				password = StringEncrypter.decrypt(username, ((String) row.get(1)));
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			gusername.setText(username);
			gpassword.setText(password);
		} catch (Exception e1) {}
		
		//facebook
		try {
			row = db.getAllRowsAsArrays("fbook_cfg").get(0);
			username = (String) row.get(0);
			password = null;
			try {
				password = StringEncrypter.decrypt(username, ((String) row.get(1)));
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			fusername.setText(username);
			fpassword.setText(password);
		} catch (Exception e) {}
		
		//bluetooth
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
		boolean isEnabled = myAppPref.getBoolean("hasBlueAccount", false);
		if(isEnabled) {
			CheckBox cb = (CheckBox) findViewById(R.id.blue_enable);
			cb.setChecked(true);
			
		}
		
		//checkboxes fill:
		CheckBox gcb = (CheckBox) findViewById(R.id.gtalk_enable);
		CheckBox fcb = (CheckBox) findViewById(R.id.fbook_enable);
		isEnabled = myAppPref.getBoolean("hasGtalkAccount", false);
		if(isEnabled) {
			gcb.setChecked(true);
		} else {
			gusername.setVisibility(View.INVISIBLE);
			gpassword.setVisibility(View.INVISIBLE);
			gpassView.setVisibility(View.INVISIBLE);
			guserView.setVisibility(View.INVISIBLE);
		}
		isEnabled = myAppPref.getBoolean("hasFBookAccount", false);
		if(isEnabled) {
			fcb.setChecked(true);
		} else {
			fusername.setVisibility(View.INVISIBLE);
			fpassword.setVisibility(View.INVISIBLE);
			fpassView.setVisibility(View.INVISIBLE);
			fuserView.setVisibility(View.INVISIBLE);
		}
		
		//check box listeners
		
		gcb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked) {
					gusername.setVisibility(View.VISIBLE);
					gpassword.setVisibility(View.VISIBLE);
					gpassView.setVisibility(View.VISIBLE);
					guserView.setVisibility(View.VISIBLE);
				} else {
					gusername.setVisibility(View.INVISIBLE);
					gpassword.setVisibility(View.INVISIBLE);
					gpassView.setVisibility(View.INVISIBLE);
					guserView.setVisibility(View.INVISIBLE);
				}
				
			}
		});
		
		fcb.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			
			@Override
			public void onCheckedChanged(CompoundButton arg0, boolean isChecked) {
				if(isChecked) {
					fusername.setVisibility(View.VISIBLE);
					fpassword.setVisibility(View.VISIBLE);
					fpassView.setVisibility(View.VISIBLE);
					fuserView.setVisibility(View.VISIBLE);
				} else {
					fusername.setVisibility(View.INVISIBLE);
					fpassword.setVisibility(View.INVISIBLE);
					fpassView.setVisibility(View.INVISIBLE);
					fuserView.setVisibility(View.INVISIBLE);
				}
			}
		});
		
		//close db connections
		db.closeConnection();
		db = null;
	}
	private void onSaveClicked() {
		//get EditText references
		EditText gusername = (EditText)findViewById(R.id.guser_edit);
		EditText gpassword = (EditText)findViewById(R.id.gpass_edit);
		
		EditText fusername = (EditText)findViewById(R.id.fuser_edit);
		EditText fpassword = (EditText)findViewById(R.id.fpass_edit);
		
		CheckBox bcheckbox = (CheckBox)findViewById(R.id.blue_enable);
		CheckBox gcb = (CheckBox) findViewById(R.id.gtalk_enable);
		CheckBox fcb = (CheckBox) findViewById(R.id.fbook_enable);
		
		
		if(!gcb.isChecked()) {
			gusername.setText("");
			gpassword.setText("");
		}
		if(!fcb.isChecked()) {
			fusername.setText("");
			fpassword.setText("");
		}
		//validation
		if((gusername.getText().toString().equals("") || gpassword.getText().toString().equals("")) &&
		   (fusername.getText().toString().equals("") || fpassword.getText().toString().equals("")) &&
		   (!bcheckbox.isChecked())) {
    		Toast.makeText(getApplicationContext(), "Please enter at least one account.", Toast.LENGTH_SHORT).show();
    		return;
    	}
		//insertion into db
		
		DBManager db = new DBManager(this);
		//deleting all existing rows
		db.deleteAllRows("gtalk_cfg");
		db.deleteAllRows("fbook_cfg");
		db.deleteAllRows("bluetooth_cfg");
		
		//gtalk
		String username = null; 
		username = gusername.getText().toString();
		String pass = gpassword.getText().toString();
		String password = null;
		try {
			password = StringEncrypter.encrypt(username, pass);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		if((username != null || password != null) &&
		  (!username.equals("") || !username.equals(""))) {
			db.addRow("gtalk_cfg", new String[]{username,password});
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasGtalkAccount", true);
	        prefEdit.commit();
		} else {
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasGtalkAccount", false);
	        prefEdit.commit();
		}
		
		//facebook
		username = null; 
		username = fusername.getText().toString();
		pass = fpassword.getText().toString();
		password = null;
		try {
			password = StringEncrypter.encrypt(username, pass);
		} catch (Exception e) {
			
			e.printStackTrace();
		}
		if((username != null || password != null) &&
		   (!username.equals("") || !username.equals(""))) {
			db.addRow("fbook_cfg", new String[]{username,password});
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasFBookAccount", true);
	        prefEdit.commit();
		} else {
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasFBookAccount", false);
	        prefEdit.commit();
		}
		
		//bluetooth
		username = null; 
		boolean isEnabled = false;
		if(bcheckbox.isChecked()) {
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasBlueAccount", true);
	        prefEdit.commit();
		} else {
			int mode = Activity.MODE_PRIVATE;
	        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
	        Editor prefEdit = myAppPref.edit();
	        prefEdit.putBoolean("hasBlueAccount", false);
	        prefEdit.commit();
		}
		
		Toast.makeText(getApplicationContext(), "Accounts successfully updated!\nReloading App, standby...", Toast.LENGTH_LONG).show();
		db.closeConnection();
		db = null;
		
		//reloading app
		setResult(3);
		finish();
		
		
	}
}
