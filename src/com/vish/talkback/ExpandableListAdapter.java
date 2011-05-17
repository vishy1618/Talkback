package com.vish.talkback;

import java.util.ArrayList;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class ExpandableListAdapter extends BaseExpandableListAdapter {
	ArrayList<String> groups;
	ArrayList<ArrayList<String>> children;
	ArrayList<ArrayList<byte[]>> avlist;
	Context mcontext;
	
	public ExpandableListAdapter(Context context,ArrayList<String> groups, ArrayList<ArrayList<String>> children, ArrayList<ArrayList<byte[]>> avlist) {
		mcontext = context;
		this.groups = groups;
		this.children = children;
		this.avlist = avlist;
		
	}
	
	public void changeData(ArrayList<String> groups, ArrayList<ArrayList<String>> children, ArrayList<ArrayList<byte[]>> avlist) {
		this.groups = groups;
		this.children = children;
		this.avlist = avlist;
	}
	
	@Override
	public Object getChild(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return children.get(arg0).get(arg1);
	}

	@Override
	public long getChildId(int arg0, int arg1) {
		
		return arg1;
	}

	@Override
	public View getChildView(int groupPosition, int childPosition, boolean isLastChild, View convertView,
			ViewGroup parent) {
		boolean isAvailable = false;
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater)mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.list_element, null);
		}
		TextView tv = (TextView)convertView.findViewById(R.id.child_element);
		tv.setText(((String)getChild(groupPosition, childPosition)));
		
		//set the icon
		ImageView iv = (ImageView)convertView.findViewById(R.id.status_icon);
		if(getChild(groupPosition,childPosition).toString().contains("Available")) {
			isAvailable = true;
			iv.setImageResource(R.drawable.avail);			
		} else if(getChild(groupPosition,childPosition).toString().contains("Away")) {
			isAvailable = false;
			iv.setImageResource(R.drawable.away);
		}

		//set the avatar
		ImageView avatariv = (ImageView)convertView.findViewById(R.id.avatar);
		byte[] avatar;
		
		try {
			avatar = avlist.get(groupPosition).get(childPosition);
		} catch(Exception e) {
			avatar = null;
		}
		if(!(avatar == null)) {
			Bitmap bmp = BitmapFactory.decodeByteArray(avatar, 0, avatar.length);
			
			avatariv.setImageBitmap(bmp);
		} else {
			avatariv.setImageResource(R.drawable.def_user);
		}
		
		
		//set the max characters to be displayed
		if(tv.getText().toString().split("\n")[0].length() > 24) {
			tv.setText(tv.getText().toString().substring(0, 20) + "..." + "\n" + ((isAvailable) ? "Available" : "Away"));
		}
		
		
		tv.setTextColor(Color.BLACK);
		
		return convertView;
	}

	@Override
	public int getChildrenCount(int arg0) {
		try {
			return children.get(arg0).size();
		} catch(Exception e) {
			return 0;
		}
	}

	@Override
	public Object getGroup(int arg0) {
		
		return groups.get(arg0);
	}

	@Override
	public int getGroupCount() {
		// TODO Auto-generated method stub
		return groups.size();
	}

	@Override
	public long getGroupId(int arg0) {
		// TODO Auto-generated method stub
		return arg0;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
		String groupname = (String)getGroup(groupPosition);
		
		if(convertView == null) {
			LayoutInflater inflater = (LayoutInflater) mcontext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.group_element, null);
		}
		TextView tv = (TextView)convertView.findViewById(R.id.group_element);
		tv.setText(groupname);
		
		tv.setTextColor(Color.BLACK);
		
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		// TODO Auto-generated method stub
		return true;
	}

	@Override
	public boolean isChildSelectable(int arg0, int arg1) {
		// TODO Auto-generated method stub
		return true;
	}
}
