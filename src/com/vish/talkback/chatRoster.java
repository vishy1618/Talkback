package com.vish.talkback;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.LocalActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ExpandableListView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ExpandableListView.OnChildClickListener;

public class chatRoster extends Activity{
	private boolean mIsBound = false;
	private boolean isFirstLaunch = true;
	final Handler mHandler = new Handler();
	private static boolean isProcessing = false;
	private static boolean enqueued = false;
	private boolean bt_enabled = false;
	private ChatService mBoundService;
	//the list widget declarations...
	ArrayList<String> groupnames;
	ArrayList<ArrayList<String>> children;
	ArrayList<ArrayList<byte[]>> avlist;
	ExpandableListAdapter adapter;
	ExpandableListView listView;
	//end of list stuff declarations
	
	//bundle for restart
	Bundle savedInstance;
	
	private BroadcastReceiver presenceReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(isFirstLaunch) {
			
				setContentView(R.layout.chat_roster);
								
				groupnames = mBoundService.getGroups();
				children = null;
				avlist = null;
				try {
					children = mBoundService.getChildren();
					
					//avlist = mBoundService.getAvatar();
				
				} catch (InterruptedException e) {

					e.printStackTrace();
				}
				
					
				adapter = new ExpandableListAdapter(chatRoster.this,groupnames,children,avlist);

				listView = (ExpandableListView)findViewById(R.id.elistview);

				listView.setAdapter(adapter);
				listView.expandGroup(0);
				listView.expandGroup(1);
				listView.expandGroup(2);
				
				//configure spinner
				configureSpinner();
				
				//configure listeners for list
				listView.setOnChildClickListener(new OnChildClickListener() {
					
					@Override
					public boolean onChildClick(ExpandableListView parent, View v, int groupPos,
							int childPos, long id) {
						try {
							String nam = children.get(groupPos).get(childPos);
							String name = null;
							if(nam.contains("Away"))
								name = nam.substring(0,nam.indexOf("Away")-1);
							else
								name = nam.substring(0,nam.indexOf("Available")-1);
							
							//Log.i("name is",name);
							String from = mBoundService.getFromForName(name,groupPos);
							
							startActivity(new Intent(chatRoster.this,ChatDialog.class)
							              .putExtra("name", name)
							              .putExtra("from", from));
						} catch(Exception ie) {
							ie.printStackTrace();
						}
						return false;
					}
				});
				
				isFirstLaunch = false;		
				
			} else {
				if(!isProcessing) {
					//spawn a new thread to check and get the updates
					Thread getChatListThread = new Thread(){
						@Override
						public void run() {
							isProcessing = true;
							groupnames = mBoundService.getGroups();
							children = null;
							avlist = null;
							try {
								children = mBoundService.getChildren();
								avlist = mBoundService.getAvatar();
							} catch (InterruptedException e) {

								e.printStackTrace();
							}
							mHandler.post(mUpdateResults);
						}
					};
					getChatListThread.start();
				} else {
					enqueued = true;
				}
			}
		}
		
	};
	
	private BroadcastReceiver disconnectionReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.e("Chat error", "no internets!!1");
			AlertDialog.Builder builder = new AlertDialog.Builder(chatRoster.this);
			builder.setMessage("Could not connect. Please check if you are online. Press OK to exit.");
			builder.setCancelable(false);
			builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
				
				@Override
				public void onClick(DialogInterface arg0, int arg1) {
					chatRoster.this.finish();
					
				}
			});
			
			AlertDialog alert = builder.create();
			alert.show();
		}
		
	};
	
	private BroadcastReceiver notLoggedInReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Toast.makeText(chatRoster.this, "Unable to log in to your "+arg1.getStringExtra("service")+" account. \nPlease check your credentials again..."
					, Toast.LENGTH_SHORT).show();
			
			startActivity(new Intent(chatRoster.this, AccountManager.class));
		}
		
	};
	
	private BroadcastReceiver scanBlueReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Intent serverIntent = new Intent(chatRoster.this, DeviceListActivity.class)
			  .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
			chatRoster.this.startActivityForResult(serverIntent, 10);
			
		}
		
	};
	
	//runnable called when the update thread has finished
	final Runnable mUpdateResults = new Runnable() {
        public void run() {
        	//change the internal adapter data
			adapter.changeData(groupnames, children, avlist);
			//notify the UI
			adapter.notifyDataSetChanged();
			
			isProcessing = false;
			
			if(enqueued)
				sendBroadcast(new Intent("Service: presence changed"));
			
			enqueued = false;
        }
    };
    
    @Override
	protected void onNewIntent(Intent intent) {
		setIntent(intent);
    }
    
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        savedInstance = savedInstanceState;
        //Checking if any accounts are configured
        int mode = Activity.MODE_PRIVATE;
        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
        
        //if not configured then show welcome screen
        if (!myAppPref.getBoolean("hasAnAccount", false)) {
        	finish();
        	Intent welcomeIntent = new Intent(chatRoster.this,welcomeScreen.class);
            startActivity(welcomeIntent);
        }
        
        setContentView(R.layout.loading_layout);
        
        
        try {
			doBindService();
		} catch (InterruptedException e) {
			
			e.printStackTrace();
		}
		
		
		
	}
	
	@Override
	public void onResume() {
		super.onResume();
		//presence reciever
		registerReceiver(presenceReciever, new IntentFilter("Service: presence changed"));
		//disconnection reciever
		registerReceiver(disconnectionReciever, new IntentFilter("Service: no connection"));
		//not logged in receiver
		registerReceiver(notLoggedInReciever, new IntentFilter("Service: not logged in"));
		
		
	}
	
	@Override
	public void onPause() {
		super.onPause();
		unregisterReceiver(presenceReciever);
		unregisterReceiver(disconnectionReciever);
		unregisterReceiver(notLoggedInReciever);
	}
	
	
	
	private ServiceConnection mConnection = new ServiceConnection() {
		@Override
	    public void onServiceConnected(ComponentName className, IBinder service) {
	        // This is called when the connection with the service has been
	        // established, giving us the service object we can use to
	        // interact with the service.  Because we have bound to a explicit
	        // service that we know is running in our own process, we can
	        // cast its IBinder to a concrete class and directly access it.
			
			Log.i("Info", "Servce is connected");
	        mBoundService = ((ChatService.LocalBinder)service).getService();
	        mIsBound = true;
	        // Tell the user about this for our demo.
	        //Toast.makeText(Listy.this, "Connected",
	        //        Toast.LENGTH_SHORT).show();
	        
	        //after loading, change the layout
			
	        
	    }
		@Override
	    public void onServiceDisconnected(ComponentName className) {
	        // This is called when the connection with the service has been
	        // unexpectedly disconnected -- that is, its process crashed.
	        // Because it is running in our same process, we should never
	        // see this happen.
			Log.e("Info", "Servce is disconnected");
	        mBoundService = null;
	        Toast.makeText(chatRoster.this, "disconnected",
	                Toast.LENGTH_SHORT).show();
	        
		}
	};
	        
	void doBindService() throws InterruptedException {
		// Establish a connection with the service.  We use an explicit
		// class name because we want a specific service implementation that
		// we know will be running in our own process (and thus won't be
		// supporting component replacement by other applications).
		boolean con=bindService(new Intent(chatRoster.this, 
				ChatService.class), mConnection, Context.BIND_AUTO_CREATE);
		Log.e("Info", "connected: "+con);
	}

	void doUnbindService() {
	    if (mIsBound) {
	        // Detach our existing connection.
	        unbindService(mConnection);
	        mIsBound = false;
	    }
	}

	private void configureSpinner() {
		//start configuring status spinner
		Spinner statSpinner = (Spinner)findViewById(R.id.spinner);
		ArrayAdapter<CharSequence> statAdapter = ArrayAdapter.createFromResource(this,
				R.array.status_array, android.R.layout.simple_spinner_item);
		statAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		
	    statSpinner.setAdapter(statAdapter);
	    statSpinner.setOnItemSelectedListener(new OnItemSelectedListener() {

			@Override
			public void onItemSelected(AdapterView<?> parent, View v,
					int pos, long id) {
				
				TextView tv = (TextView) v;
				String text = (String) tv.getText();
				if(text.equals("Sign Out"))
					chatRoster.this.finish();
				else
					mBoundService.changeStatus(text);
			}

			@Override
			public void onNothingSelected(AdapterView<?> arg0) {
				// do nothing
				
			}
		});
	}
	
	//menu inflation
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
	    MenuInflater inflater = getMenuInflater();
	    inflater.inflate(R.menu.roster_menu, menu);
	    int mode = Activity.MODE_PRIVATE;
        SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
        MenuItem item = (MenuItem) menu.findItem(R.id.bt_scan);
		if (myAppPref.getBoolean("hasBlueAccount", false)) {
        	if(item != null)
        		item.setVisible(true);
        }
		
	    return true;
	}
	//menu handling
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
	    // Handle item selection
	    switch (item.getItemId()) {
	    case R.id.contact_list:
	    	startActivity(new Intent(chatRoster.this,ContactList.class));
	        return true;
	    case R.id.accounts:
	        startActivityForResult(new Intent(chatRoster.this,AccountManager.class), 1);
	        return true;
	    case R.id.about:
	    	startActivity(new Intent(chatRoster.this,AboutDialog.class));
	        return true;
	    case R.id.bt_scan:
	    	Intent serverIntent = new Intent(this, DeviceListActivity.class);
            startActivityForResult(serverIntent, 10);
            return true;
	    default:
	        return super.onOptionsItemSelected(item);
	    }
	}
	
	@Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		
		
		
        if (requestCode == 1) {
            if (resultCode == 3) {
            	doUnbindService();
        	    stopService(new Intent(chatRoster.this,ChatService.class));
        	    mConnection = null;
        	    Intent intent = getIntent();
        	    finish();
        	    startActivity(intent);
            } else {
            	
            }
            
           
        }
        if (requestCode == 10) {
        	if (resultCode == Activity.RESULT_OK) {
                // Get the device MAC address
                String address = data.getExtras()
                                     .getString(DeviceListActivity.EXTRA_DEVICE_ADDRESS);
                // Get the BLuetoothDevice object
                sendBroadcast(new Intent("scan result received")
                			  .putExtra("address", address));
                			  
                
            }
        }
    }
	@Override
	protected void onDestroy() {
	    super.onDestroy();
	    
	    doUnbindService();
	    stopService(new Intent(chatRoster.this,ChatService.class));
	    mConnection = null;
	    
	}

}
