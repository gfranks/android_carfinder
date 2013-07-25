package com.gf.carfinder.fragments;

import java.util.ArrayList;

import com.gf.carfinder.R;
import com.gf.carfinder.utils.CarFinderApplication;
import com.gf.carfinder.utils.GFMapClusterer;
import com.gf.carfinder.utils.GFMapClusterer.OnMapClustererMarkerClickListener;
import com.gf.carfinder.utils.LocationManager;
import com.gf.carfinder.utils.LocationManager.LocationUnavailableException;
import com.gf.carfinder.utils.LocationModel;
import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class CarFinderFragment extends Fragment implements OnMapClustererMarkerClickListener, 
	OnMapClickListener, OnMapLongClickListener {
	
	private GFMapClusterer mapClusterer;
	private boolean hasCarMarker;
	private MarkerOptions carMarkerOption;
	private Toast mToast;
	private Button mapTypeSwitch;
	private TextView mapTypeHeader;
	private View containerView;
	private int locationFailureCount;
	private boolean mapLoadFailed;
	
	public CarFinderFragment() {}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		containerView = inflater.inflate(R.layout.fragment_car_finder, container, false);
		locationFailureCount = 0;
		registerCarFinderFragmentReceiver();
	    setupMapView();
	    
	    return containerView;
	}
	
	public void registerCarFinderFragmentReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(CarFinderFragmentReceiver,
        	      new IntentFilter(CarFinderApplication.CARFINDER_BROADCAST_ACTION));
	}
	
	private void setupMapView() {
		SupportMapFragment mapFragment = (SupportMapFragment) getFragmentManager().findFragmentById(R.carfinder.mapview);
		if (mapFragment.getMap() == null) {
			try {
				MapsInitializer.initialize(getActivity());
			} catch (GooglePlayServicesNotAvailableException e) {
				e.printStackTrace();
				mapClusterer = null;
				mapLoadFailed = true;
				return;
			}
		}
		
		if (mapClusterer == null) {
			// Try to obtain the map from the SupportMapFragment.
			mapClusterer = new GFMapClusterer(mapFragment.getMap(), getActivity());
			mapClusterer.setClusterImageResources(R.drawable.pin_cluster, R.drawable.pin_cluster, R.drawable.pin_cluster);
			mapClusterer.setOnMapClustererMarkerClickListener(this);
			mapClusterer.getMap().setOnMapClickListener(this);
			mapClusterer.showUserLocation();
		}
		
		SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_MAP_PREF, Context.MODE_PRIVATE);
		mapClusterer.getMap().setMapType(prefs.getInt(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA, GoogleMap.MAP_TYPE_HYBRID));
		mapClusterer.getMap().setTrafficEnabled(prefs.getBoolean(CarFinderApplication.CARFINDER_MAP_TRAFFIC_EXTRA, false));
		
		updateMapTypeSwitch();
		animateToUserLocation();
		mapLoadFailed = false;
	}
	
	public void updateMapTypeSwitch() {
		if (mapTypeSwitch == null) {
			mapTypeSwitch = (Button) containerView.findViewById(R.carfinder.mapTypeSwitch);
			mapTypeSwitch.setOnClickListener(new OnClickListener() {
				@Override
				public void onClick(View v) {
					Intent broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
					broadcast.putExtra(CarFinderApplication.CARFINDER_MAP_TYPE_MENU_EXTRA, true);
					broadcast.putExtra(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA, 0);
			        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
				}
			});
		}
		if (mapTypeHeader == null) {
			mapTypeHeader = (TextView) containerView.findViewById(R.carfinder.mapTypeHeader);
		}
		
		switch (mapClusterer.getMap().getMapType()) {
		case GoogleMap.MAP_TYPE_NORMAL:
			mapTypeHeader.setTextColor(getResources().getColor(R.color.black));
			mapTypeSwitch.setText("Normal");
			break;
		case GoogleMap.MAP_TYPE_SATELLITE:
			mapTypeHeader.setTextColor(getResources().getColor(R.color.whites));
			mapTypeSwitch.setText("Satellite");
			break;
		case GoogleMap.MAP_TYPE_HYBRID:
			mapTypeHeader.setTextColor(getResources().getColor(R.color.whites));
			mapTypeSwitch.setText("Hybrid");
			break;
		}
	}
	
	public void animateToUserLocation() {
		try {
			LocationModel locModel = LocationManager.getBestKnownLocationModel();
			mapClusterer.animateCameraUpdate(CameraUpdateFactory.newLatLngZoom(
					new LatLng(locModel.getLatitude(), locModel.getLongitude()), 15));
		} catch (LocationUnavailableException e) {
			e.printStackTrace();
			++locationFailureCount;
			if (locationFailureCount > 1) {
				makeToast(getString(R.string.message_location_error), Toast.LENGTH_LONG);
			} else {
				makeToast(getString(R.string.message_location_error_new_attempt), Toast.LENGTH_LONG);
				LocationManager.locate(getActivity());
			}
		}
	}
	
	public void getDirections() {
		if (carMarkerOption == null) {
			makeToast(getString(R.string.message_not_tracking_car), Toast.LENGTH_SHORT);
			return;
		}
		
		final LatLng carLocation = carMarkerOption.getPosition();
		AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
		alertDialog.setTitle("Load Directions");
		alertDialog.setMessage("Do you wish to get directions to your car?");
		alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Intent.ACTION_VIEW, 
					    Uri.parse("http://maps.google.com/maps?f=d&daddr="
					    		+carLocation.latitude+","+carLocation.longitude
					    		+getDirectionsType()));
				if (isMapsAppInstalled()) {
					intent.setComponent(new ComponentName("com.google.android.apps.maps", 
					    "com.google.android.maps.MapsActivity"));
				}
				startActivity(intent);
				dialog.dismiss();
			}
		});
		alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();
			}
		});
		alertDialog.show();
	}
	
	public String getDirectionsType() {
		SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, Context.MODE_PRIVATE);
    	switch (prefs.getInt(CarFinderApplication.CARFINDER_DIRECTIONS_PREF, CarFinderApplication.CARFINDER_TRAFFIC_WALKING)) {
    	case CarFinderApplication.CARFINDER_TRAFFIC_DRIVING:
			return "&dirflg=d";
    	case CarFinderApplication.CARFINDER_TRAFFIC_TRANSIT:
			return "&dirflg=r";
    	case CarFinderApplication.CARFINDER_TRAFFIC_WALKING:
			return "&dirflg=w";
    	case CarFinderApplication.CARFINDER_TRAFFIC_BICYCLING:
			return "&dirflg=b";
		default:
			return "";
    	}
	}
	
	private boolean isMapsAppInstalled() {
	    PackageManager pm = getActivity().getPackageManager();
	    try {
	        pm.getPackageInfo("com.google.android.apps.maps", PackageManager.GET_ACTIVITIES);
	        return true;
	    } catch (PackageManager.NameNotFoundException e) {
	        return false;
	    }
	}
	
	public void makeToast(String message) {
		makeToast(message, Toast.LENGTH_SHORT);
	}
	
	public void makeToast(String message, int length) {
		if (mToast != null) {
			mToast.cancel();
		}
		
		mToast = Toast.makeText(getActivity(), message, length);
		mToast.show();
	}
	
	@Override
	public void onMarkerClick(final Marker marker, Object obj) {
		Log.i("CarFinderFragment.onMarkerClick()", "Map marker was clicked");
		getDirections();
	}
	
	@Override
	public void onClusterClick(Marker cluster, ArrayList<MarkerOptions> markers) {
		Log.i("CarFinderFragment.onClusterClick()", "Node Count: "+markers.size());
	}
	@Override
	public void onMapClick(LatLng latlng) {
		showDialogFormNewCarMarker(latlng);
	}
	
	@Override
	public void onMapLongClick(LatLng latlng) {
		showDialogFormNewCarMarker(latlng);
	}
	
	public void showDialogFormNewCarMarker(LatLng latlng) {
		final LatLng position = latlng;
		Log.i("CarFinderFragment.onMapClick()", "Click Position: "+ latlng.latitude + ":" + latlng.longitude);
		if (!hasCarMarker) {
			AlertDialog alertDialog = new AlertDialog.Builder(getActivity()).create();
			alertDialog.setTitle("New Car Location");
			alertDialog.setMessage("Do you wish to add your car's location here?");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					hasCarMarker = true;
					addCarMarkeratLatLng(position);
					
					dialog.dismiss();
				}
			});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
		}
	}
	
	public void addCarMarkeratLatLng(LatLng position) {
		int carMarkerResource = R.drawable.car_pin_image;
		switch (mapClusterer.getMap().getMapType()) {
		case GoogleMap.MAP_TYPE_SATELLITE:
			carMarkerResource = R.drawable.car_pin_image_white;
			break;
		case GoogleMap.MAP_TYPE_HYBRID:
			carMarkerResource = R.drawable.car_pin_image_white;
			break;
		default: 
			break;
		}
		carMarkerOption = new MarkerOptions().position(new LatLng(position.latitude, position.longitude));
		Bitmap b = BitmapFactory.decodeResource(getResources(), carMarkerResource);
		carMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(b));
		mapClusterer.getMap().addMarker(carMarkerOption);
	}
	
	private BroadcastReceiver CarFinderFragmentReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				if (extras.containsKey(CarFinderApplication.CARFINDER_CLEAR_MARKERS_EXTRA)) {
					mapClusterer.removeMapMarkers();
					makeToast(getString(R.string.message_markers_cleared), Toast.LENGTH_SHORT);
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_DIRECTIONS_EXTRA)) {
					getDirections();
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA)) {
					Integer newCarMarkerResource = null;
					switch (extras.getInt(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA)) {
					case 0:
						mapClusterer.getMap().setMapType(GoogleMap.MAP_TYPE_NORMAL);
						newCarMarkerResource = R.drawable.car_pin_image;
						break;
					case 1:
						mapClusterer.getMap().setMapType(GoogleMap.MAP_TYPE_SATELLITE);
						newCarMarkerResource = R.drawable.car_pin_image_white;
						break;
					case 2:
						mapClusterer.getMap().setMapType(GoogleMap.MAP_TYPE_HYBRID);
						newCarMarkerResource = R.drawable.car_pin_image_white;
						break;
					}
					
					SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_MAP_PREF, Context.MODE_PRIVATE);
					Editor ed = prefs.edit();
					ed.putInt(CarFinderApplication.CARFINDER_MAP_TYPE_EXTRA, mapClusterer.getMap().getMapType());
					ed.commit();
					
					if (carMarkerOption != null && newCarMarkerResource != null) {
						mapClusterer.removeMapMarkers();
						Bitmap b = BitmapFactory.decodeResource(getResources(), newCarMarkerResource);
						carMarkerOption.icon(BitmapDescriptorFactory.fromBitmap(b));
						mapClusterer.getMap().addMarker(carMarkerOption);
					}
					
					updateMapTypeSwitch();

					Intent broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
					broadcast.putExtra(CarFinderApplication.CARFINDER_MENU_LIST_RELOAD_EXTRA, true);
			        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_TRAFFIC_EXTRA)) {
					mapClusterer.getMap().setTrafficEnabled(!mapClusterer.getMap().isTrafficEnabled());
					SharedPreferences prefs = getActivity().getSharedPreferences(CarFinderApplication.CARFINDER_MAP_PREF, Context.MODE_PRIVATE);
					Editor ed = prefs.edit();
					ed.putBoolean(CarFinderApplication.CARFINDER_MAP_TRAFFIC_EXTRA, mapClusterer.getMap().isTrafficEnabled());
					ed.commit();

					Intent broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
					broadcast.putExtra(CarFinderApplication.CARFINDER_MENU_LIST_RELOAD_EXTRA, true);
			        LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(broadcast);
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_LOCATION_UPDATE_EXTRA)) {
					animateToUserLocation();
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_LOCATION_ERROR_EXTRA)) {
					makeToast(getString(R.string.message_location_error));
				}
				if (extras.containsKey(CarFinderApplication.CARFINDER_RELOAD_MAP_PREF)) {
					if (mapLoadFailed) {
						setupMapView();
					}
				}
			}
		}
	};
}
