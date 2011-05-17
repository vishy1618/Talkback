package com.vish.talkback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import android.app.TabActivity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TabHost;
import android.widget.TabHost.OnTabChangeListener;

public class ChatDialog extends TabActivity {
	private TabHost tabHost;
	
	//reusable msgArray
	private ArrayList<String> msgArray;
	//array for switching between chats...
	private ArrayList<ArrayList<String[]>> chatArray;
	//array list for the from addresses
	private ArrayList<String> fromAdd;
	//intent that started the chat dialog
	Intent startIntent;
	public static boolean isRunning = false;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		setContentView(R.layout.chat_dialog);
		
		msgArray = new ArrayList<String>();
		chatArray = new ArrayList<ArrayList<String[]>>();
		fromAdd = new ArrayList<String>();
		
		startIntent = getIntent();
		
		tabHost = getTabHost();
		tabHost.setOnTabChangedListener(new OnTabChangeListener() {
			
			@Override
			public void onTabChanged(String arg0) {
				refreshList();
				
			}
		});
		addNewTab("Tab");
		
		Button sendButton = (Button)findViewById(R.id.msg_send_button);
		sendButton.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View arg0) {
				sendMessage();
			}
		});
		isRunning = true;
	}
	
	//getting new intents and assigning it to startIntent
	@Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
		startIntent = intent;
		
		boolean isExists = false;
		//check if there is an existing tab with the address
		for(int i=0;i<fromAdd.size();i++) {
			if(fromAdd.get(i).equals(intent.getStringExtra("from"))) {
				//change to the appropriate tab
				tabHost.setCurrentTab(i);
				tabHost.getCurrentTabView().setVisibility(View.VISIBLE);
				isExists = true;
				
			}
		}
		
		if(!isExists) {
			addNewTab(startIntent.getStringExtra("name"));
		}
	}
	
	
	
	private void refreshList() {
		msgArray.clear();
		if(tabHost.getCurrentTab() == -1)
			return;
		//debug
		Log.i("Current tab", ""+tabHost.getCurrentTab());
		
		
		ListView lv = (ListView) findViewById(R.id.chat_text);
		for( int i=0;i<chatArray.get(tabHost.getCurrentTab()).size();i++) {
			String add;
			if(chatArray.get(tabHost.getCurrentTab()).get(i)[0].contains("@"))
				add = chatArray.get(tabHost.getCurrentTab()).get(i)[0].substring(0, chatArray.get(tabHost.getCurrentTab()).get(i)[0].indexOf("@"));
			else
				add = chatArray.get(tabHost.getCurrentTab()).get(i)[0];
			
			String text = add
			+": " + chatArray.get(tabHost.getCurrentTab()).get(i)[1];

			msgArray.add(text);

			ArrayAdapter<String> msgListAdapter = new ArrayAdapter<String>(ChatDialog.this,R.layout.msg_list_item,msgArray) {};

			msgListAdapter.notifyDataSetChanged();
		
			lv.setAdapter(msgListAdapter);
			
			
		}
	}
	
	
	private void addNewTab(String name) {
		//add new entry to the chat Array
		chatArray.add(new ArrayList<String[]>());
		
		TabHost.TabSpec spec; 
		spec=tabHost.newTabSpec("tab");
		spec.setIndicator(getIntent().getStringExtra("name"),getResources().getDrawable(R.drawable.def_user_tab));
				
		spec.setContent(R.id.tab_content_layout);
		tabHost.addTab(spec);
		
		
		fromAdd.add(getIntent().getStringExtra("from"));
		tabHost.setCurrentTab(fromAdd.size()-1);
		
		
		//load the history onto the arraylist
		DBManager db = new DBManager(this);
		
		
		ArrayList<ArrayList<Object>> history = db.getAllRowsAsArrays("chat_history");
		if(history != null) {
			for(int i=0;i<history.size();i++) {
				
				if(history.get(i).get(0).toString().equals(getIntent().getStringExtra("from"))) {
					int tab = 0;
					if(this.tabHost.getCurrentTab() != -1)
						tab = this.tabHost.getCurrentTab();
					

					String namer = null;
					if(history.get(i).get(2).toString().equals("Me"))
						namer = "Me";
					else
						namer = getIntent().getStringExtra("name");
					chatArray.get(tab).add(new String[]{namer,(String)history.get(i).get(3)});
					
				}
				
			}
		}
		
		
		
		refreshList();
		db.closeConnection();
		db = null;
	}
	
	@Override
	protected void onResume() {
		super.onResume();
		registerReceiver(msgListener, new IntentFilter("new message"));
		isRunning = true;
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		unregisterReceiver(msgListener);
		isRunning = false;
	}
	
	private BroadcastReceiver msgListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			int tab = 0;
			for(int i = 0;i<fromAdd.size();i++) {
				if(fromAdd.get(i).equals(arg1.getStringExtra("from"))){
					tab = i;
					if(arg1.getStringExtra("body") == null)
						return;
					chatArray.get(tab).add(new String[]{arg1.getStringExtra("name"),arg1.getStringExtra("body")});
				}
			}
			
			
			
			if(tabHost.getCurrentTab() == tab)
				refreshList();
			else {
				
			}
		}
	};

	private void sendMessage() {
		String msgText = ((EditText) findViewById(R.id.my_msg_edit)).getText().toString();
		
		if(msgText.equals(""))
			return;
		
		Intent msgIntent = new Intent("sent message");
		msgIntent.putExtra("msg", msgText);
		msgIntent.putExtra("from", fromAdd.get(tabHost.getCurrentTab()));
		Log.i("from", fromAdd.get(tabHost.getCurrentTab()));
		sendBroadcast(msgIntent);
		((EditText) findViewById(R.id.my_msg_edit)).setText("");
		
		//add it to chat array and refresh the list
		int tab = 0;
		tab = tabHost.getCurrentTab();
		chatArray.get(tab).add(new String[]{"Me",msgText});
		refreshList();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.dialog_menu, menu);
		return true;
	}
	
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.close_current:
			int tab = tabHost.getCurrentTab();
			tabHost.getCurrentTabView().setVisibility(View.GONE);
			
			Log.i("tab is", tab+"");
			int tabs = tabHost.getTabWidget().getTabCount();
			Log.i("No. Tabs: ", tabs+"");
			if((tabs-1) > tab) {
				if(getVisible() == -1)
					finish();
				else
					tabHost.setCurrentTab(getVisible());
			} else if(((tabs -1) == tab) && (tab == 0)) {
				finish();
			} else {
				if(getVisible() == -1)
					finish();
				else
					tabHost.setCurrentTab(getVisible());
			}
		}
		return true;
	}
	
	private int getVisible() {
		int tabs = tabHost.getTabWidget().getTabCount();
		int tab = -1;
		for(int i=0;i<tabs;i++) {
			int t = tabHost.getTabWidget().getChildAt(i).getVisibility();
			
			if(t == View.GONE) {}
			else {
				tab = i;
			}
		}
		return tab;
	}
	
	@Override
	protected void onDestroy() {
		super.onDestroy();
		DBManager db = new DBManager(this);
		
		db.deleteAllRows("chat_history");
		msgArray.clear();
		for( int i=0;i<chatArray.size();i++) {
			//delete existing history of the user
			int count;
			if(chatArray.get(i).size() < 10)
				count = 0;
			else
				count = chatArray.get(i).size() - 10;
			
			for(int j=count;j<chatArray.get(i).size();j++) {
								
				String from = chatArray.get(i).get(j)[0];
				
				if(from.contains("@"))
					continue;
				
				String curr_user = null;
				if(from.equals("Me")) {
					curr_user = from;
					from = fromAdd.get(i);
				} else {
					from = fromAdd.get(i);
					curr_user = from;
				}
				
				SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
				Date date = new Date();
				String[] chatColumns = new String[]{from,
						dateFormat.format(date),
						curr_user, 
						chatArray.get(i).get(j)[1]};
				db.addRow("chat_history", chatColumns);
				
				/*for(int r=0;r<chatColumns.length;r++)
					Log.i("chatColumn["+r+"]", chatColumns[r] != null ? chatColumns[r]:"");*/
				
				
			}
		}
		//db.deleteAllRows("chat_history");
		db.closeConnection();
		db = null;
		isRunning = false;
	}
}
