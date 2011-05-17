package com.vish.talkback;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;


import android.app.Activity;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.util.Log;
import android.widget.ArrayAdapter;
import android.widget.Toast;


public class ChatService extends Service {
	private NotificationManager mNM;
	private XMPPManager gxmanager = null;
	private XMPPManager fxmanager = null;

	private BluetoothAdapter mBluetoothAdapter;
	private BluetoothManager blueman;
	ArrayList<String> groupnames;
    ArrayList<ArrayList<String>> children;
    final int SERVICE_NOTIFY=0;
    final int CHAT_NOTIFY=1;
    
    //bluetooth stuff
    public static final int MESSAGE_STATE_CHANGE = 1;
    public static final int MESSAGE_READ = 2;
    public static final int MESSAGE_WRITE = 3;
    public static final int MESSAGE_DEVICE_NAME = 4;
    public static final int MESSAGE_TOAST = 5;
    // Key names received from the BluetoothManager Handler
    public static final String DEVICE_NAME = "device_name";
    public static final String TOAST = "toast";
    //other bluetooth stuff
    ArrayAdapter<String> blueConversationArrayAdapter;
    private String blueConnectedDeviceName = null;
    private StringBuffer blueOutStringBuffer;
    
    boolean[] accounts = new boolean[3];
    
    public ArrayList<String> getGroups() {
    	groupnames = new ArrayList<String>();
        groupnames.add("Gtalk");
        groupnames.add("Facebook");
        groupnames.add("Bluetooth");
        
        return groupnames;
    }
    
    public ArrayList<String> getContactList() {
    	ArrayList<String> list = new ArrayList<String>();
    	
    	if(gxmanager != null) {
    		list.addAll(gxmanager.getAllContacts());
    	}
    	if(fxmanager != null) {
    		list.addAll(fxmanager.getAllContacts());
    	}
    	return list;
    }
    
    public ArrayList<ArrayList<String>> getChildren() throws InterruptedException {
    	ArrayList<String> users = null;
    	ArrayList<String> status = null;
    	ArrayList<String> fusers = null;
    	ArrayList<String> fstatus = null;
    	
    	ArrayList<ArrayList<String>> children = new ArrayList<ArrayList<String>>();
    	
        try {
        	if(gxmanager != null) {
        		users = gxmanager.getChatRoster();
        		status = gxmanager.getStatuses();
        		children.add(new ArrayList<String>());
                for(int i=0;i<users.size();i++) {
                	children.get(0).add(users.get(i) + "\n" + status.get(i));
                }
        	} else {
        		children.add(new ArrayList<String>());
        	}
        	if(fxmanager != null) {
        		fusers = fxmanager.getChatRoster();
        		fstatus = fxmanager.getStatuses();
        		children.add(new ArrayList<String>());
                for(int i=0;i<fusers.size();i++) {
                	children.get(1).add(fusers.get(i) + "\n" + fstatus.get(i));
                }
        	} else {
        		children.add(new ArrayList<String>());
        	}
        	if(blueman != null) {
        		if(blueConnectedDeviceName != null) {
        			children.add(new ArrayList<String>());
        			children.get(2).add(blueConnectedDeviceName + "\n" + "Available");
        		} else {
        			children.add(new ArrayList<String>());
        		}
        	} else {
        		children.add(new ArrayList<String>());
        	}
        	
        } catch(NullPointerException e) {
        	return new ArrayList<ArrayList<String>>();
        }
        
        if(children.size() < 3) {
        	children.add(new ArrayList<String>());
        	if(children.size()<3) {
        		for (int i = 0; i < 2; i++) {
					children.get(1).add("Value" + "\n" + i);
				}
        		children.add(new ArrayList<String>());
				for (int i = 0; i < 2; i++) {
					children.get(2).add("Value" + "\n" + i);
				}
        	} else {
        		for (int i = 0; i < 2; i++) {
					children.get(2).add("Value" + "\n" + i);
				}
        	}
        }
                
        return children;
    }
    
	public class LocalBinder extends Binder {
		public ChatService getService() {
			return ChatService.this;
		}
	}
	
	public void changeStatus(String status) {
		if(gxmanager != null)
			gxmanager.changeStatus(status);
		if(fxmanager != null)
			fxmanager.changeStatus(status);
 	}
	
	private final IBinder mBinder = new LocalBinder();
	
	@Override
	public IBinder onBind(Intent arg0) {
		
		//Log.e("onBind:","in OnBind"); 
		return mBinder;
	}
	
	private BroadcastReceiver presenceReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			//Log.i("in chat service", "presence changed");
			sendBroadcast(new Intent("Service: presence changed"));
		}
		
	};
	
	private BroadcastReceiver disconnectionReciever = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			Log.e("Service error", "no internets!!1");
			sendBroadcast(new Intent("Service: no connection"));
		}
		
	};
	
	private BroadcastReceiver sentMessageReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(arg1.getStringExtra("from").contains("gmail")) {
				gxmanager.sendMessage(arg1.getStringExtra("from"), arg1.getStringExtra("msg"));
			} else if(arg1.getStringExtra("from").contains("@bluetooth")) {
				 // Check that we're actually connected before trying anything
		        if (blueman.getState() != BluetoothManager.STATE_CONNECTED) {
		            
		            return;
		        }

		        // Check that there's actually something to send
		        if (arg1.getStringExtra("msg").length() > 0) {
		            // Get the message bytes and tell the BluetoothChatService to write
		            byte[] send = arg1.getStringExtra("msg").getBytes();
		            blueman.write(send);

		            // Reset out string buffer to zero and clear the edit text field
		            blueOutStringBuffer.setLength(0);
		            
		        }
			} else
				fxmanager.sendMessage(arg1.getStringExtra("from"), arg1.getStringExtra("msg"));
			
		}
		
	};
	
	private BroadcastReceiver gotAddressReceiver = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			String address = arg1.getStringExtra("address");
			Log.i("address", arg1.getStringExtra("address"));
			BluetoothDevice device = mBluetoothAdapter.getRemoteDevice(address);
            // Attempt to connect to the device
            blueman.connect(device);
		}
	};
	
	private BroadcastReceiver msgListener = new BroadcastReceiver() {

		@Override
		public void onReceive(Context arg0, Intent arg1) {
			if(arg1.getStringExtra("body") == null)
				return;
			
			Notification note = new Notification(R.drawable.msg_emoticon,
					arg1.getStringExtra("body"),
					System.currentTimeMillis());
			note.flags |= Notification.FLAG_AUTO_CANCEL;
			
			note.setLatestEventInfo(ChatService.this, arg1.getStringExtra("name"), arg1.getStringExtra("body"), 
					PendingIntent.getActivity(ChatService.this.getBaseContext(), 0, 
							new Intent(ChatService.this,ChatDialog.class)
							.setAction("note_clicked")
							.putExtra("from", arg1.getStringExtra("from"))
							.putExtra("name", arg1.getStringExtra("name")),
							PendingIntent.FLAG_UPDATE_CURRENT));
			//Log.i("packet listener", "got message");
			mNM.notify(CHAT_NOTIFY, note);
			
			//sound if chat dialog is not visible
			Log.e("instance count", ""+ChatDialog.getInstanceCount());
			if(!ChatDialog.isRunning) {
				MediaPlayer mp = MediaPlayer.create(getBaseContext(), R.raw.new_im);
				mp.start();
				mp = null;
			}
			
			//add it to the chat history database
			DBManager db = new DBManager(ChatService.this);
			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
			Date date = new Date();
			
			String[] chatColumns = new String[]{arg1.getStringExtra("from").trim(),
												dateFormat.format(date),
												arg1.getStringExtra("from").trim(), 
												arg1.getStringExtra("body")};
			db.addRow("chat_history", chatColumns);
			db.closeConnection();
			db = null;
		}
		
	};
	
	public ArrayList<ArrayList<byte[]>> getAvatar() {
		ArrayList<ArrayList<byte[]>> avlist = new ArrayList<ArrayList<byte[]>>();
		
		if(accounts[0]) {
			try {
				ArrayList<byte[]> gavlist= gxmanager.getAvatar();
				avlist.add(gavlist);
			} catch(NullPointerException ne) {
				return avlist;
			}
		} else {
			avlist.add(new ArrayList<byte[]>());
		}
		
		if(accounts[1]) {
			try {
				ArrayList<byte[]> favlist= fxmanager.getAvatar();
				avlist.add(favlist);
			} catch(NullPointerException ne) {
				return avlist;
			}
		} else {
			avlist.add(new ArrayList<byte[]>());
		}
		
		return avlist;
	}
	
	@Override
	public void onCreate() {
		Log.e("onCreate:","in oncreate");
		mNM = (NotificationManager)getSystemService(NOTIFICATION_SERVICE);
		
		showNotification();
		int mode = Activity.MODE_PRIVATE;
		SharedPreferences myAppPref = getSharedPreferences((String)getResources().getText(R.string.app_pref),mode);
		
		//register broadcast listeners
		registerReceiver(presenceReciever, new IntentFilter("presence changed"));
		registerReceiver(disconnectionReciever, new IntentFilter("no internet"));
		registerReceiver(msgListener, new IntentFilter("new message"));
		registerReceiver(sentMessageReceiver, new IntentFilter("sent message"));
		registerReceiver(gotAddressReceiver, new IntentFilter("scan result received"));
//		Editor prefEdit = myAppPref.edit();
//        prefEdit.putBoolean("hasGtalkAccount", true);
//        prefEdit.commit();
        
		if(myAppPref.getBoolean("hasGtalkAccount", false))
			accounts[0] = true;
		else
			accounts[0] = false;
		if(myAppPref.getBoolean("hasFBookAccount", false))
			accounts[1] = true;
		else
			accounts[1] = false;
		if(myAppPref.getBoolean("hasBlueAccount", false)) 
			accounts[2] = true;
		else
			accounts[2] = false;
		
		//Log.i("accounts", "accounts[0]" + accounts[0]);
		if(accounts[0]) {
			
			//getting usernames and password
	        DBManager db = new DBManager(this);
	        ArrayList<ArrayList<Object>> rows = db.getAllRowsAsArrays("gtalk_cfg");
	        db.closeConnection();
	        
	        String username = null;
	        String encPassword = null;
	        try {
	        	username = (String)rows.get(0).get(0);
	        	encPassword = (String)rows.get(0).get(1);
			} catch(Exception npe) {
				return;
			}
	        //decrypting password
	        String password = null;
			try {
				password = StringEncrypter.decrypt(username, encPassword);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			//declaring final so thread class can accept it
	        final String user = username;
	        final String pass = password;
	        
	        Thread networkThread = new Thread() {
	        	@Override
	        	public void run() {
	        		gxmanager = new XMPPManager(user,pass,"gtalk",ChatService.this.getApplicationContext());
	        	}
	        };
	        networkThread.start();
	        
		}
		if(accounts[1]) {
			//getting usernames and password
	        DBManager db = new DBManager(this);
	        ArrayList<ArrayList<Object>> rows = db.getAllRowsAsArrays("fbook_cfg");
	        db.closeConnection();
	        
	        String username = null;
	        String encPassword = null;
	        try {
	        	username = (String)rows.get(0).get(0);
	        	encPassword = (String)rows.get(0).get(1);
	        } catch(Exception npe) {
	        	return;
	        }
	        //decrypting password
	        String password = null;
			try {
				password = StringEncrypter.decrypt(username, encPassword);
			} catch (Exception e) {
				
				e.printStackTrace();
			}
			//declaring final so thread class can accept it
	        final String user = username;
	        final String pass = password;
	        
	        Thread networkThread = new Thread() {
	        	@Override
	        	public void run() {
	        		fxmanager = new XMPPManager(user,pass,"fbook",ChatService.this.getApplicationContext());
	        	}
	        };
	        networkThread.start();
		}
		if(accounts[2]) {
			mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
			
			
			final Handler blueExceptionHandler = new Handler();
			final Handler discoverHandler = new Handler();
			new Thread() {
				public void run() {
					if(mBluetoothAdapter == null) {
						try {
							Thread.sleep(100);
						} catch (InterruptedException e) {
							
						}
						if(mBluetoothAdapter == null) {
							blueExceptionHandler.post(new Runnable() {
								
								@Override
								public void run() {
									Toast.makeText(getApplicationContext(), "You dont seem to have bluetooth. Please untick bluetooth \nin the Account manager.", Toast.LENGTH_LONG).show();
									sendBroadcast(new Intent("Service: presence changed"));
								}
							});
							
							return;
						}
					}
					//check if enabled, else request enable of bluetooth
					if (!mBluetoothAdapter.isEnabled()) {
			            mBluetoothAdapter.enable();	  
			            while(!mBluetoothAdapter.isEnabled()) {
			            	try {
								Thread.sleep(40);
							} catch (InterruptedException e) {
								
								e.printStackTrace();
							}
			            }
			            if (blueman == null) {
			            	blueman = new BluetoothManager(ChatService.this, blueHandler);
			            }
			        
			        } else {
			            if (blueman == null) {
			            	blueman = new BluetoothManager(ChatService.this, blueHandler);
			            }
			        }
					blueOutStringBuffer = new StringBuffer("");
					
					
					//then check for the state
					if (blueman.getState() == BluetoothManager.STATE_NONE) {
			              // Start the Bluetooth chat services
						discoverHandler.post(new Runnable() {
							
							@Override
							public void run() {
								ensureDiscoverable();
							}
						});  
						
			            blueman.start();
			        }
					
					
				}
			}.start();
			
		}
	}
	
	private void ensureDiscoverable() {
        
        if (mBluetoothAdapter.getScanMode() !=
            BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE) {
            Intent discoverableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE);
            discoverableIntent.putExtra(BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300);
            discoverableIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            getApplicationContext().startActivity(discoverableIntent);
            
        }
    }
	
	private final Handler blueHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
            case MESSAGE_STATE_CHANGE:
                
                switch (msg.arg1) {
                case BluetoothManager.STATE_CONNECTED:
                    if(blueConversationArrayAdapter != null)
                    	blueConversationArrayAdapter.clear();
                    sendBroadcast(new Intent("Service: presence changed"));
                    break;
                case BluetoothManager.STATE_CONNECTING:
                    
                    break;
                case BluetoothManager.STATE_LISTEN:
                case BluetoothManager.STATE_NONE:
                    blueConnectedDeviceName = null;
                    sendBroadcast(new Intent("Service: presence changed"));
                    break;
                }
                break;
            case MESSAGE_WRITE:
                byte[] writeBuf = (byte[]) msg.obj;
                // construct a string from the buffer
                String writeMessage = new String(writeBuf);
                //blueConversationArrayAdapter.add("Me:  " + writeMessage);
                break;
            case MESSAGE_READ:
                byte[] readBuf = (byte[]) msg.obj;
                // construct a string from the valid bytes in the buffer
                String readMessage = new String(readBuf, 0, msg.arg1);
                
                if(readMessage == null || readMessage.equals(""))
    				return;
    			
    			Notification note = new Notification(R.drawable.msg_emoticon,
    					readMessage,
    					System.currentTimeMillis());
    			note.flags |= Notification.FLAG_AUTO_CANCEL;
    			
    			note.setLatestEventInfo(ChatService.this, blueConnectedDeviceName, readMessage, 
    					PendingIntent.getActivity(ChatService.this.getBaseContext(), 0, 
    							new Intent(ChatService.this,ChatDialog.class)
    							.setAction("note_clicked")
    							.putExtra("from", blueConnectedDeviceName + "@bluetooth")
    							.putExtra("name", blueConnectedDeviceName),
    							PendingIntent.FLAG_UPDATE_CURRENT));
    			//Log.i("packet listener", "got message");
    			mNM.notify(CHAT_NOTIFY, note);

    			//add it to the chat history database
    			/*DBManager db = new DBManager(ChatService.this);
    			SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"); 
    			Date date = new Date();
    			
    			String[] chatColumns = new String[]{blueConnectedDeviceName+"@bluetooth",
    												dateFormat.format(date),
    												blueConnectedDeviceName+"@bluetooth", 
    												readMessage};
    			db.addRow("chat_history", chatColumns);
    			db.closeConnection();
    			db = null;*/
    			
    			//send the message as a broadcast for the ChatDialog
    			sendBroadcast(new Intent("new message")
    					.putExtra("from", blueConnectedDeviceName+"@bluetooth")
    					.putExtra("name", blueConnectedDeviceName)
    					.putExtra("body", readMessage));
                break;
            case MESSAGE_DEVICE_NAME:
                // save the connected device's name
                blueConnectedDeviceName = msg.getData().getString(DEVICE_NAME);
                Toast.makeText(getApplicationContext(), "Connected to "
                               + blueConnectedDeviceName, Toast.LENGTH_SHORT).show();
                break;
            case MESSAGE_TOAST:
                Toast.makeText(getApplicationContext(), msg.getData().getString(TOAST),
                               Toast.LENGTH_SHORT).show();
                break;
            }
        }
    };
	
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		
		return START_STICKY;
	}
	
	private void showNotification() {
		Notification notification = new Notification(R.drawable.status_icon, "TalkBack service started", System.currentTimeMillis());
		notification.flags |= Notification.FLAG_NO_CLEAR;
		PendingIntent contentIntent = PendingIntent.getActivity(this, 0, 
				new Intent(this,chatRoster.class), 0);
		notification.setLatestEventInfo(this, getResources().getText(R.string.app_name), "Chat Service",contentIntent);
	
		mNM.notify(SERVICE_NOTIFY, notification);
		
	}
	
	public String getFromForName(String name,int groupPos) {
		if(groupPos == 0) {
			if(gxmanager != null)
				return(gxmanager.getFromForName(name));
		} else if(groupPos == 1) {
			if(fxmanager != null)
				return(fxmanager.getFromForName(name));
		} else if(groupPos == 2) {
			if(blueman != null) 
				return blueConnectedDeviceName + "@bluetooth";
		}
		return null;
	}
	
	@Override
	public void onDestroy() {
		mNM.cancel("Service stopped", 0);
		mNM.cancelAll();
		unregisterReceiver(presenceReciever);
		unregisterReceiver(disconnectionReciever);
		unregisterReceiver(msgListener);
		unregisterReceiver(sentMessageReceiver);
		unregisterReceiver(gotAddressReceiver);
		
		presenceReciever = null;
		
		try {
			gxmanager.disconnect();
			fxmanager.disconnect();
		} catch (Exception e) {
			
			//e.printStackTrace();
		}
		gxmanager = null;
		fxmanager = null;
		
		//trying to disable bluetooth if on.
		if(blueman != null) {
			try {
				if(mBluetoothAdapter != null) {
					mBluetoothAdapter.disable();
				}
			} catch(Exception e) {}
		}
	}
	
}
