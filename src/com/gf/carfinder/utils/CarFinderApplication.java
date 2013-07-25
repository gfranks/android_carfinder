package com.gf.carfinder.utils;

import android.app.Application;

public class CarFinderApplication extends Application {

	/* Main Broadcast Intent Action */
	public static final String CARFINDER_BROADCAST_ACTION       = "carfinder_broadcast";

	/* Intent extra to change current page */
	public static final String CARFINDER_CHANGE_VIEW_PAGE_EXTRA = "change_view_page";

	/* Intent extra to reload the map */
	public static final String CARFINDER_RELOAD_MAP_PREF        = "reload_map";

	/* Intent extra to modify map type, load directions, toggle traffic, clear markers */
	public static final String CARFINDER_DIRECTIONS_PREF        = "direction_type";
	public static final String CARFINDER_MAP_PREF               = "map_prefs";
	public static final String CARFINDER_MAP_TYPE_EXTRA         = "map_type";
	public static final String CARFINDER_MAP_TRAFFIC_EXTRA      = "map_traffic";
	public static final String CARFINDER_MAP_TYPE_MENU_EXTRA    = "change_map_type";
	public static final String CARFINDER_TRAFFIC_EXTRA          = "traffic_enabled";
	public static final String CARFINDER_MENU_LIST_RELOAD_EXTRA = "menu_list_notify";
	public static final String CARFINDER_CLEAR_MARKERS_EXTRA    = "clear_markers";
	public static final String CARFINDER_DIRECTIONS_EXTRA       = "directions";
	
	/* Traffic identifiers */
	public static final int CARFINDER_TRAFFIC_DRIVING   = 0;
	public static final int CARFINDER_TRAFFIC_TRANSIT   = 1;
	public static final int CARFINDER_TRAFFIC_WALKING   = 2;
	public static final int CARFINDER_TRAFFIC_BICYCLING = 3;

	/* Intent extra for location updates */
	public static final String CARFINDER_LOCATION_UPDATE_EXTRA  = "user_location_update";
	public static final String CARFINDER_LOCATION_ERROR_EXTRA   = "gps_disabled";
	
	public static final boolean DEVELOPMENT_MODE = true;
	
	@Override
    public void onCreate() {
		super.onCreate();
		
		if (DEVELOPMENT_MODE) {
			
		}
    }
}
