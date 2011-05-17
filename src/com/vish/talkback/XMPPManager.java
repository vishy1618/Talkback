package com.vish.talkback;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.XMPPException;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smack.provider.ProviderManager;
import org.jivesoftware.smackx.packet.VCard;
import org.jivesoftware.smackx.provider.VCardProvider;
import org.jivesoftware.smackx.pubsub.PresenceState;

import android.content.Context;
import android.content.Intent;
import android.os.Environment;
import android.os.Message;
import android.util.Log;


public class XMPPManager {
	@SuppressWarnings("unused")
	private String username = null;
	@SuppressWarnings("unused")
	private String password = null;
	@SuppressWarnings("unused")
	private String service = null;
	
	private XMPPConnection connection;
	private RosterListener rosterListener;
	private Context appContext;
	//cache of avatars
	private ArrayList<ArrayList<Object>> avlist;
	
	@SuppressWarnings("unchecked")
	public XMPPManager(String username, String password, String service, Context context) {
		this.username = username;
		this.password = password;
		this.service = service;
		this.appContext = context;
		
		//getting back the serialized avlist
		try {
			FileInputStream fis = appContext.openFileInput("avlist.ser");
			ObjectInputStream ois = new ObjectInputStream(fis);
			avlist = (ArrayList<ArrayList<Object>>) ois.readObject();
			ois.close();
			Log.i("Serialization", "Got the cached list");
			Log.i("avlist size", ""+avlist.size());
			
		} catch(Exception e) {
			
			avlist = new ArrayList<ArrayList<Object>>();
		}
		
		connectUser(username,password,service);
		
		rosterListener = new RosterListener() {
			@Override
			public void presenceChanged(Presence arg0) {
				
				Log.i("in presence changed", "presence changed");
				appContext.sendBroadcast(new Intent("presence changed"));
			}

			@Override
			public void entriesAdded(Collection<String> arg0) {
				
				//Log.i("in presence changed", "presence changed");
				//appContext.sendBroadcast(new Intent("presence changed"));
			}

			@Override
			public void entriesDeleted(Collection<String> arg0) {
				
				//Log.i("in presence changed", "presence changed");
				//appContext.sendBroadcast(new Intent("presence changed"));
			}

			@Override
			public void entriesUpdated(Collection<String> arg0) {
				
				//Log.i("in presence changed", "presence changed");
				//appContext.sendBroadcast(new Intent("presence changed"));
			}
		};
		
		try {
			connection.getRoster().addRosterListener(rosterListener);
		} catch(Exception e) {
			
		}
		
		//handling incomming chat requests and messages
		final MessageListener mlistener = new MessageListener() {
			@Override
			public void processMessage(Chat chat,
					org.jivesoftware.smack.packet.Message msg) {
				String from = msg.getFrom().split("/")[0];
				String name = null;
				name = connection.getRoster().getEntry(from).getName();
				if(name == null)
					name = from.substring(0,from.indexOf("@"));
				appContext.sendBroadcast(new Intent("new message")
				.putExtra("name",name)
				.putExtra("from", from)
				.putExtra("body", msg.getBody()));
				
			}
		};
		
		ChatManagerListener chatListener = new ChatManagerListener() {

			@Override
			public void chatCreated(Chat chat, boolean arg1) {
				chat.addMessageListener(mlistener);
			}
			
		};
		connection.getChatManager().addChatListener(chatListener);
		
		
		ProviderManager.getInstance().addIQProvider("vCard", "vcard-temp", new VCardProvider());
	}
	
	private void connectUser(String username, String password, String service) {
		if(service.equals("gtalk")) {
			ConnectionConfiguration config = new ConnectionConfiguration("talk.google.com",5222,"gmail.com");
			
			connection = new XMPPConnection(config);
			
			try {
				connection.connect();
				Log.i("Info:", "Connection Established");
			} catch (Exception xe) {
				appContext.sendBroadcast(new Intent("no internet"));
				//Toast.makeText(appContext, "Unable to connect. Check your connection. Will now exit...", Toast.LENGTH_LONG).show();
				Log.e("XMPP Error:","Falied to connect!");
				return;
			}
			
			
			try {
				
				connection.login(username, password);
				
				appContext.sendBroadcast(new Intent("presence changed"));
				
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
				
				
			} catch(Exception xe) {
				//Toast.makeText(appContext, "Unable to log in. Check your account config.", Toast.LENGTH_LONG).show();
				appContext.sendBroadcast(new Intent("Service: not logged in")
											 .putExtra("service", "GTalk"));
				Log.e("XMPP Error:","Falied to log in!");
				return;
				
			}
			
		} else {
			ConnectionConfiguration config = new ConnectionConfiguration("chat.facebook.com",5222,"chat.facebook.com");
			
			connection = new XMPPConnection(config);
			
			try {
				connection.connect();
				Log.i("Info:", "Facebook Connection Established");
			} catch (Exception xe) {
				appContext.sendBroadcast(new Intent("no internet"));
				//Toast.makeText(appContext, "Unable to connect. Check your connection. Will now exit...", Toast.LENGTH_LONG).show();
				Log.e("XMPP Error:","Failed to connect!");
				return;
			}
			
			
			try {
				
				connection.login(username, password);
				appContext.sendBroadcast(new Intent("presence changed"));
				Presence presence = new Presence(Presence.Type.available);
				connection.sendPacket(presence);
			} catch(Exception xe) {
				xe.printStackTrace();
				appContext.sendBroadcast(new Intent("Service: not logged in")
				 .putExtra("service", "Facebook"));
				//Toast.makeText(appContext, "Unable to log in. Check your account config.", Toast.LENGTH_LONG).show();
				Log.e("XMPP Error:","Falied to log in!");
				return;
				
			}
		}
			
	}
	
	public ArrayList<String> getChatRoster() throws InterruptedException {
		
		
		Roster roster = connection.getRoster();
		
		Collection<RosterEntry> entries = roster.getEntries();
		
		
		ArrayList<String> list = new ArrayList<String>();
		if(entries.size()>0) {
			Iterator<RosterEntry> entryIterator = entries.iterator();
			RosterEntry entry = null;
			
			while(entryIterator.hasNext()) {
				entry = (RosterEntry) entryIterator.next();
				String name = null;
				name = entry.getName();
				if(name == null)
					name = entry.getUser().substring(0,entry.getUser().indexOf("@"));
				Presence presence = roster.getPresence(entry.getUser());
				if(presence.isAvailable() | presence.isAway()) {
					//Log.i("chat name", name);
					list.add(name);
				}
			}
		}
		
		return list;
	}
	
	public ArrayList<String> getAllContacts() {
		Roster roster = connection.getRoster();
		
		Collection<RosterEntry> entries = roster.getEntries();
		
		
		ArrayList<String> list = new ArrayList<String>();
		if(entries.size()>0) {
			Iterator<RosterEntry> entryIterator = entries.iterator();
			RosterEntry entry = null;
			
			while(entryIterator.hasNext()) {
				entry = (RosterEntry) entryIterator.next();
				String name = null;
				String user = null;
				name = entry.getName();
				
				user = entry.getUser();
				if(name == null)
					name = entry.getUser().substring(0,entry.getUser().indexOf("@"));
				if(name.length() > 20) {
					name = name.substring(0,20);
				}
				String total = name + "\n" + user;
				list.add(total);
				
			}
		}
		
		return list;
	}
	public ArrayList<String> getStatuses() {
		Roster roster = connection.getRoster();
		
		Collection<RosterEntry> entries = roster.getEntries();
		
		
		ArrayList<String> list = new ArrayList<String>();
		if(entries.size()>0) {
			Iterator<RosterEntry> entryIterator = entries.iterator();
			RosterEntry entry = null;
			
			while(entryIterator.hasNext()) {
				entry = (RosterEntry) entryIterator.next();
				Presence presence = roster.getPresence(entry.getUser());
				
				if(presence.isAvailable()) {
					
					if(presence.isAway())
						list.add("Away");
					else
						list.add("Available");
				}
			}
		}
		
		return list;
	}
	
	public ArrayList<byte[]>  getAvatar() {
		Roster roster = connection.getRoster();
		
		Collection<RosterEntry> entries = roster.getEntries();
		
		
		ArrayList<byte[]> list = new ArrayList<byte[]>();
		
		if(entries.size()>0) {
			Iterator<RosterEntry> entryIterator = entries.iterator();
			RosterEntry entry = null;
			
			while(entryIterator.hasNext()) {
				entry = (RosterEntry) entryIterator.next();
				Presence presence = roster.getPresence(entry.getUser());
				
				if(presence.isAvailable() | presence.isAway()) {
					byte[] avatarBytes = null;
					
					//iterate the cache to find if the avatar is already existing
					for(int i=0;i<avlist.size();i++) {
						try {
							if(avlist.get(i).get(1).toString().equals(entry.getUser())) {
								avatarBytes = (byte[])avlist.get(i).get(0);
								//Log.i("Found cache",entry.getUser());
							}
						} catch (Exception e) {
							//Log.e("Empty ArrayList","avlist");
						}
					}
					//if we find no matching avatar in cache, then request one from server
					if(avatarBytes == null) {
						//Log.i("cache null",entry.getUser());
						VCard vCard = new VCard();
						try {
							vCard.load(connection, entry.getUser());
						} catch (XMPPException e) {
							//e.printStackTrace();
						}
						avatarBytes = vCard.getAvatar();
						list.add(avatarBytes);
						
						//we add the newly recieved avatar to the cache (if not null)
						boolean isPresent = false;
						for(int i=0;i<avlist.size();i++) {
							if(avlist.get(i).get(1).toString().equals(entry.getUser())) {
								isPresent = true;
							}
						}
						
						if(avatarBytes != null && !isPresent) {
							
							ArrayList<Object> tempObj = new ArrayList<Object>();
							tempObj.add(avatarBytes);
							tempObj.add(entry.getUser());
							avlist.add(tempObj);
							tempObj = null;
						}
					} else {
						//just add the cached avatar if found
						list.add(avatarBytes);
					}
					
					
				}
			}
		}
		
		return list;
	}
	
	public String getFromForName(String name) {
		Roster roster = connection.getRoster();
		
		Collection<RosterEntry> entries = roster.getEntries();
		
		Iterator<RosterEntry> iterator = entries.iterator();
		
		String from = null;
		while(iterator.hasNext()) {
			RosterEntry entry = (RosterEntry) iterator.next();
			try {
				if(entry.getName().toString().contains(name.trim())) {
					from = entry.getUser().toString();
				}
			} catch (NullPointerException npe) {
				if(entry.getUser().toString().contains(name.trim())) {
					from = entry.getUser().toString();
				}
			}
		}
		return from;
	}
	
	public void sendMessage(String from, String message) {
		ChatManager chatmanager = connection.getChatManager();
		Chat newChat = chatmanager.createChat(from, new MessageListener() {
			@SuppressWarnings("unused")
		    public void processMessage(Chat chat, Message message) {
		        
		    }

			@Override
			public void processMessage(Chat arg0,
					org.jivesoftware.smack.packet.Message arg1) {
				
				
			}
		});

		try {
		    newChat.sendMessage(message);
		}
		catch (XMPPException e) {
		    Log.e("Send Message", "Error sending message!");
		}

	}
	
	public void changeStatus(String status) {
		if(connection.isConnected()) {
			if(status.equals("Available")) {
				connection.sendPacket(new Presence(Presence.Type.available));
			} else if(status.equals("Away")) {
				connection.sendPacket(new Presence(Presence.Type.available, "", 0, Presence.Mode.away));
			} 
		}
 	}
	
	public void disconnect() {
		connection.sendPacket(new Presence(Presence.Type.unavailable));
		connection.disconnect();
		
		//serializing the avatars onto the device for later reference
		try {
			FileOutputStream fos = appContext.openFileOutput("avlist.ser",Context.MODE_PRIVATE);
			ObjectOutputStream oos = new ObjectOutputStream(fos);
			oos.writeObject(avlist);
			oos.close();
		} catch (FileNotFoundException e) {
			
			e.printStackTrace();
		} catch (IOException e) {
			
			e.printStackTrace();
		}	
		
	}
}
