package com.gf.carfinder.utils;

import java.util.Timer;
import java.util.TimerTask;

import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

public final class LocationManager {
	
	protected static final String tag = LocationManager.class.getSimpleName();
	
	private static LocationModel bestKnownLocationModel;
	private static android.location.LocationManager androidLocationManager;
	private static android.location.LocationListener androidLocationListener;
	private static final int ONE_MINUTE = 1000 * 60;
	private static Timer timer = null;
	private static boolean isGpsEnabled = true;
	private static boolean isNetworkEnabled = true;
	private static Context mContext;
	
	public static void locate(final Context context) {
		mContext = context;
		isGpsEnabled = getLocationManager(context).isProviderEnabled(android.location.LocationManager.GPS_PROVIDER);
		isNetworkEnabled = getLocationManager(context).isProviderEnabled(android.location.LocationManager.NETWORK_PROVIDER);
		if(!isGpsEnabled && !isNetworkEnabled) {
			fireLocalBroadcastIntent(CarFinderApplication.CARFINDER_LOCATION_ERROR_EXTRA);
			return;
		}

		String providerName = null;
		if (isGpsEnabled) {
	    	Criteria criteria = new Criteria();
	    	criteria.setAccuracy(Criteria.ACCURACY_COARSE);
	    	providerName = getLocationManager(context).getBestProvider(criteria, true);
		} else if (isNetworkEnabled) {
			providerName = android.location.LocationManager.NETWORK_PROVIDER;
		}
    	if(providerName == null) {
    		fireLocalBroadcastIntent(CarFinderApplication.CARFINDER_LOCATION_ERROR_EXTRA);
	        return;
    	}
    	
	    Location lastKnownLocation = getLocationManager(context).getLastKnownLocation(providerName);
	    if (null != lastKnownLocation) {
	    	setBestKnownLocationModel(new LocationModel(lastKnownLocation, context));
	    }
	    
    	if (null!=androidLocationListener) {
    		getLocationManager(context).removeUpdates(androidLocationListener);
    		androidLocationListener = null;
    	}
    	androidLocationListener = new LocationListener(){
    		
    		@Override
			public void onLocationChanged(final Location location) {
    			if (isBetterLocation(location)){
    				setBestKnownLocationModel(new LocationModel(location, context));
    			}
			}
    		@Override
			public void onProviderDisabled(String provider) {}
    		@Override
			public void onProviderEnabled(String provider) {}
    		@Override
			public void onStatusChanged(String provider, int status, Bundle extras) {}
    	};
    	if (isGpsEnabled || isNetworkEnabled) { 
    		androidLocationManager.requestLocationUpdates(providerName, 1000L, 500.0f, androidLocationListener);
    	}
    	
    	if (null != timer){
    		try {
    			timer.cancel();
    			timer = null;
    		} catch (Exception e) {
				Log.e(tag, "Ignored Exception: " + e.getMessage());
			}
    	}
    	timer = new Timer();
		timer.schedule(new TimerTask() {
			@Override
			public void run() {
				LocationManager.stopListening();
			}
		}, ONE_MINUTE);
    	
    }
	
	public static android.location.LocationManager getLocationManager(Context context){
		if (null==androidLocationManager) {
			androidLocationManager = (android.location.LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
		}
		return androidLocationManager;
	}
	
	public static void stopListening(){
		if (null==androidLocationManager || null==androidLocationListener) {
			return;
		}
		
		androidLocationManager.removeUpdates(androidLocationListener);
	}
	
	public static android.location.LocationListener getLocationListener() {
		return androidLocationListener;
	}
	
	

	/** Determines whether one Location reading is better than the current Location fix
	  * @param location  The new Location that you want to evaluate
	  * @param currentBestLocation  The current Location fix, to which you want to compare the new one
	  */
	protected static boolean isBetterLocation(Location location) {
	    if (getBestKnownLocationModel_private() == null) {
	        // A new location is always better than no location
	        return true;
	    }

	    // Check whether the new location fix is newer or older
	    long timeDelta = location.getTime() - getBestKnownLocationModel_private().getTime();
	    boolean isSignificantlyNewer = timeDelta > ONE_MINUTE;
	    boolean isSignificantlyOlder = timeDelta < -ONE_MINUTE;
	    boolean isNewer = timeDelta > 0;

	    // If it's been more than two minutes since the current location, use the new location
	    // because the user has likely moved
	    if (isSignificantlyNewer) {
	        return true;
	    // If the new location is more than two minutes older, it must be worse
	    } else if (isSignificantlyOlder) {
	        return false;
	    }

	    // Check whether the new location fix is more or less accurate
	    int accuracyDelta = (int) (location.getAccuracy() - getBestKnownLocationModel_private().getAccuracy());
	    boolean isLessAccurate = accuracyDelta > 0;
	    boolean isMoreAccurate = accuracyDelta < 0;
	    boolean isSignificantlyLessAccurate = accuracyDelta > 200;

	    // Check if the old and new location are from the same provider
	    boolean isFromSameProvider = isSameProvider(location.getProvider(), getBestKnownLocationModel_private().getProvider());

	    // Determine location quality using a combination of timeliness and accuracy
	    if (isMoreAccurate) {
	        return true;
	    } else if (isNewer && !isLessAccurate) {
	        return true;
	    } else if (isNewer && !isSignificantlyLessAccurate && isFromSameProvider) {
	        return true;
	    }
	    return false;
	}

	/** Checks whether two providers are the same */
	private static boolean isSameProvider(String provider1, String provider2) {
	    if (provider1 == null) {
	      return provider2 == null;
	    }
	    return provider1.equals(provider2);
	}
	
	private synchronized static LocationModel getBestKnownLocationModel_private() {
		return bestKnownLocationModel;
	}
	
	public synchronized static LocationModel getBestKnownLocationModel() throws LocationUnavailableException {
		if ((!isGpsEnabled && !isNetworkEnabled) || null==bestKnownLocationModel) {
			throw new LocationUnavailableException();
		}
		return bestKnownLocationModel;
	}
	
	public synchronized static void setBestKnownLocationModel(LocationModel location) {
		bestKnownLocationModel = location;
		fireLocalBroadcastIntent(CarFinderApplication.CARFINDER_LOCATION_UPDATE_EXTRA);
	}
	
	public static void fireLocalBroadcastIntent(String key) {
		Intent broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
		broadcast.putExtra(key, true);
		LocalBroadcastManager.getInstance(mContext).sendBroadcast(broadcast);
	}
	
	public static class LocationUnavailableException extends Exception {
		private static final long serialVersionUID = 1L;
	}
	
}
