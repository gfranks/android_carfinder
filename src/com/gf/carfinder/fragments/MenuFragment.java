package com.gf.carfinder.fragments;

import com.gf.carfinder.R;
import com.gf.carfinder.utils.CarFinderApplication;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.ExpandableListView;
import android.widget.ExpandableListView.OnChildClickListener;
import android.widget.ExpandableListView.OnGroupClickListener;
import android.widget.ExpandableListView.OnGroupCollapseListener;
import android.widget.ExpandableListView.OnGroupExpandListener;
import android.widget.ImageButton;
import android.widget.TextView;

public class MenuFragment extends Fragment implements OnGroupClickListener, OnGroupExpandListener, 
	OnGroupCollapseListener, OnChildClickListener {
	
	private ExpandableListView menuList;
	private MenuListAdapter menuListAdapter;

	public MenuFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
	    View view = inflater.inflate(R.layout.fragment_menu, container, false);
	    
	    registerMenuFragmentReceiver();
	    setupMenuList(view);
	    
	    return view;
	}
	
	public void setupMenuList(View inView) {
		menuList = (ExpandableListView) inView.findViewById(R.menu.list);
		menuListAdapter = new MenuListAdapter();
		menuList.setGroupIndicator(null);
		menuList.setAdapter(menuListAdapter);
		menuList.setOnGroupClickListener(this);
		menuList.setOnGroupCollapseListener(this);
		menuList.setOnChildClickListener(this);
	}
	
	public void registerMenuFragmentReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(MenuFragmentReceiver,
        	      new IntentFilter(CarFinderApplication.CARFINDER_BROADCAST_ACTION));
	}
	
	private class MenuListAdapter extends BaseExpandableListAdapter {
		
		private String[] groups;
		private String[] group4Children;
		private ImageButton drivingImage, transitImage, walkingImage, bicyclingImage;
		
		public MenuListAdapter() {
			groups = getResources().getStringArray(R.array.settings);
			group4Children = getResources().getStringArray(R.array.mapTypes);
	    }

	    @Override
	    public Object getChild(int groupPos, int childPos) {
	        switch (groupPos) {
	        case 0:
	        case 1:
	        case 2:
	        case 3:
	        case 5:
	        default:
	        	return null;
	        case 4:
	        	return group4Children[childPos];
	        }
	    }

	    @Override
	    public long getChildId(int groupPos, int childPos) {
	        return childPos;
	    }

	    @Override
	    public int getChildrenCount(int groupPos) {
	    	switch (groupPos) {
	        case 0:
	        case 1:
	        case 2:
	        case 5:
	        default:
	        	return 0;
	        case 3:
	        	return 1;
	        case 4:
	        	return group4Children.length;
	        }
	    }

	    @Override
	    public View getChildView(int groupPos, int childPos, boolean isLastChild, View convertView, ViewGroup parent) { 
            LayoutInflater inflater = LayoutInflater.from(getActivity());
            SharedPreferences prefs;
	        switch (groupPos) {
	        case 3:
	            convertView = inflater.inflate(R.layout.adapter_menu_direction_type_item, null);
	        	drivingImage = (ImageButton) convertView.findViewById(R.direction.driving);
	        	transitImage = (ImageButton) convertView.findViewById(R.direction.transit);
	        	walkingImage = (ImageButton) convertView.findViewById(R.direction.walking);
	        	bicyclingImage = (ImageButton) convertView.findViewById(R.direction.bicycling);
	        	drivingImage.setOnClickListener(trafficButtonClickListener);
	        	transitImage.setOnClickListener(trafficButtonClickListener);
	        	walkingImage.setOnClickListener(trafficButtonClickListener);
	        	bicyclingImage.setOnClickListener(trafficButtonClickListener);
        		drivingImage.setBackgroundResource(R.drawable.btn_traffic_normal);
        		transitImage.setBackgroundResource(R.drawable.btn_traffic_normal);
        		walkingImage.setBackgroundResource(R.drawable.btn_traffic_normal);
        		bicyclingImage.setBackgroundResource(R.drawable.btn_traffic_normal);
	        	
	    		prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, Context.MODE_PRIVATE);
	        	switch (prefs.getInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_WALKING)) {
	        	case CarFinderApplication.CARFINDER_TRAFFIC_DRIVING:
	        		drivingImage.setBackgroundResource(0);
	        		break;
	        	case CarFinderApplication.CARFINDER_TRAFFIC_TRANSIT:
	        		transitImage.setBackgroundResource(0);
	        		break;
	        	case CarFinderApplication.CARFINDER_TRAFFIC_WALKING:
	        		walkingImage.setBackgroundResource(0);
	        		break;
	        	case CarFinderApplication.CARFINDER_TRAFFIC_BICYCLING:
	        		bicyclingImage.setBackgroundResource(0);
	        		break;
	        	}
		        return convertView;
	        case 4:
	            convertView = inflater.inflate(R.layout.adapter_menu_child_item, null);
		        TextView tv = (TextView) convertView;
	        	tv.setText("\t"+group4Children[childPos]);
	        	prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_MAP_PREF, Context.MODE_PRIVATE);
	        	int mapType = prefs.getInt(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA, 4);
	        	if (childPos == 0 && mapType == 1) {
	        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_on, 0, 0, 0);
	        	} else if (childPos == 1 && mapType == 2) {
	        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_on, 0, 0, 0);
	        	} else if (childPos == 2 && mapType == 4) {
	        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_on, 0, 0, 0);
	        	} else {
	        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.checkbox_off, 0, 0, 0);
	        	}
		        return tv;
	        default:
		        return new View(getActivity());
	        }
	        
	    }

	    @Override
	    public Object getGroup(int groupPos) {
	        return groups[groupPos];
	    }

	    @Override
	    public int getGroupCount() {
	        return groups.length;
	    }

	    @Override
	    public long getGroupId(int groupPos) {
	        return groupPos;
	    }

	    @Override
	    public View getGroupView(int groupPos, boolean isExpanded, View convertView, ViewGroup parent) { 
	        if (convertView == null) {
	            LayoutInflater inflater = LayoutInflater.from(getActivity());
	            convertView = inflater.inflate(R.layout.adapter_menu_group_item, null);
	        }

	        TextView tv = (TextView) convertView;
    		
	        tv.setText(groups[groupPos]);
	        
	        if (getChildrenCount(groupPos) > 0) {
		        if (isExpanded) {
		        	tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_arrow_gray_down, 0, 0, 0);
		        } else {
		        	tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_arrow_gray_right, 0, 0, 0);
		        }
	        } else {
	        	if (groupPos == groups.length-1) {
	        		SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_MAP_PREF, Context.MODE_PRIVATE);
					boolean trafficEnabled = prefs.getBoolean(CarFinderApplication.CARFINDER_MAP_TRAFFIC_EXTRA, false);
					
					if (trafficEnabled) { 
		        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_arrow_gray_right, 0, R.drawable.toggle_on, 0);
					} else {
		        		tv.setCompoundDrawablesWithIntrinsicBounds(R.drawable.icon_arrow_gray_right, 0, R.drawable.toggle_off, 0);
					}
	        	}
	        }

	        return convertView;
	    }
	    
	    @Override
	    public boolean hasStableIds() {
	        return false;
	    }
	    
	    @Override
	    public boolean isChildSelectable(int groupPos, int childPos) {
	        return true;
	    }
	    
	    OnClickListener trafficButtonClickListener = new OnClickListener() {
	    	@Override
	    	public void onClick(View v) {
				SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, Context.MODE_PRIVATE);
				Editor ed = prefs.edit();
	    		switch (v.getId()) {
	    		case R.direction.driving:
					ed.putInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_DRIVING);
	    			break;
	    		case R.direction.transit:
					ed.putInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_TRANSIT);
	    			break;
	    		case R.direction.walking:
					ed.putInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_WALKING);
	    			break;
	    		case R.direction.bicycling:
					ed.putInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_BICYCLING);
	    			break;
	    		}
				ed.commit();
				menuListAdapter.notifyDataSetChanged();
	    	}
	    };
	}
	
	@Override
	public boolean onGroupClick(ExpandableListView parent, View v, int groupPos, long id) {
		Intent broadcast;
		switch (groupPos) {
		case 0:
			broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
			broadcast.putExtra(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA, 1);
	        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
			break;
		case 1:
			broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
			broadcast.putExtra(CarFinderApplication.CARFINDER_CLEAR_MARKERS_EXTRA, true);
			broadcast.putExtra(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA, 1);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
			menuList.collapseGroup(groupPos);
			break;
		case 2:
			broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
			broadcast.putExtra(CarFinderApplication.CARFINDER_DIRECTIONS_EXTRA, true);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
			menuList.collapseGroup(groupPos);
			break;
		case 3:
		case 4:
			break;
		case 5:
			broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
			broadcast.putExtra(CarFinderApplication.CARFINDER_TRAFFIC_EXTRA, true);
			broadcast.putExtra(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA, 1);
			LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
			break;
		default:
			break;
		}
		return false;
	}

	@Override
	public boolean onChildClick(ExpandableListView parent, View v, int groupPos, int childPos, long id) {
		Intent broadcast;
		switch (groupPos) {
		case 0:
		case 1:
		case 2:
		case 3:
		case 5:
		default:
			break;
		case 4:
			broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
			broadcast.putExtra(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA, childPos);
			broadcast.putExtra(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA, 1);
	        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
	        menuList.collapseGroup(groupPos);
			break;
		}
		
        return false;
	}
	
	@Override
	public void onGroupCollapse(int groupPos) {
		menuListAdapter.notifyDataSetChanged();	
	}
	
	@Override
	public void onGroupExpand(int groupPosition) {
		menuListAdapter.notifyDataSetChanged();
	}
	
	private BroadcastReceiver MenuFragmentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				if (extras.containsKey(CarFinderApplication.CARFINDER_MAP_TYPE_MENU_EXTRA)) {
					menuList.expandGroup(4);
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_MENU_LIST_RELOAD_EXTRA)) {
					menuListAdapter.notifyDataSetChanged();
				}
			}
		}
	};
}
