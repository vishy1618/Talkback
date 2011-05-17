package com.vish.talkback;

import java.util.ArrayList;

import android.app.Activity;
import android.app.ListActivity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.database.DataSetObserver;
import android.os.Bundle;
import android.os.IBinder;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListAdapter;
import android.widget.TextView;

public class ContactList extends ListActivity {
	private ChatService mBoundService;
	
	private class CustomAdapter extends BaseAdapter {
		ArrayList<String> contacts;
		Context parent;
		public CustomAdapter(ArrayList<String> contacts, Context context) {
			this.contacts = contacts;
			parent = context;
		}
		
		@Override
		public int getCount() {
			return contacts.size();
		}

		@Override
		public Object getItem(int position) {
			return contacts.get(position);
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			String data = contacts.get(position);
			LinearLayout itemLayout= (LinearLayout) LayoutInflater.from(this.parent).inflate(R.layout.contact_list_row, parent, false);
			
			TextView tv = (TextView) itemLayout.findViewById(R.id.con_list_text);
			tv.setText(data);
			
			ImageView iv = (ImageView) itemLayout.findViewById(R.id.con_list_image);
			if(data.contains("google.com") || data.contains("gmail.com"))
				iv.setImageResource(R.drawable.g);
			else if(data.contains("facebook"))
				iv.setImageResource(R.drawable.f);
			else if(data.contains("yahoo"))
				iv.setImageResource(R.drawable.y);
			else
				iv.setImageResource(R.drawable.def_user);
			
			return itemLayout;
		}
		
	}
	ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceDisconnected(ComponentName name) {
			
		}
		
		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mBoundService = ((ChatService.LocalBinder)service).getService();
			ArrayList<String> contacts = mBoundService.getContactList();
			
			ListAdapter adapter = new CustomAdapter(contacts, ContactList.this);
			
			setListAdapter(adapter);
		}
	};
	
	@Override
    protected void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);

        setContentView(R.layout.contact_list);

        //binding service
        bindService(new Intent(ContactList.this, 
				ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
        
        
    }
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		unbindService(mConnection);
	}
}
