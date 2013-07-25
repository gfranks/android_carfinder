package com.gf.carfinder.utils;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.util.Log;

public class LocationModel extends Location {
	
	public static final String tag = LocationModel.class.getSimpleName();
	public String fullAddress = "";
	private int latLongDecimalPlaces = 4;
	private double roundfactor = Math.pow(10,latLongDecimalPlaces);
	
	private final static Pattern latLongPattern = Pattern.compile("-?[0-9]*\\.[0-9]*.-?[0-9]*\\.[0-9]*");
	
	
	public LocationModel(Location l, Context context) {
		super(l);
		load(context);
	}
	
	private void load(Context context) {
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
    	try {
			List<Address> addresses = geocoder.getFromLocation(getLatitude(), getLongitude(), 1);
			if (null!=addresses && addresses.size() > 0) {
				if (null!=addresses.get(0)) {
					Address addr = addresses.get(0);
					if (null!=addr.getAddressLine(0)) {
						fullAddress = addr.getAddressLine(0);
					}
					if (null!=addr.getAddressLine(1)) {
						if (!"".equals(fullAddress)) {
							fullAddress += ", ";
						}
						fullAddress += addr.getAddressLine(1);
					}
				}
			}
    	} catch (IOException e) {
    		Log.e(tag, "load(...): Ignored Exception");
    	}
	}
	
	public static boolean isLatitudeLongitude(String possibleLocation) {
		Matcher latLongMatcher = latLongPattern.matcher(possibleLocation);
		return latLongMatcher.matches(); 
	}
	
	private double round(double numberToRound){
		return Math.round(numberToRound * roundfactor) / roundfactor;
	}
	@Override
	public double getLatitude() {
		return round(super.getLatitude());
	}
	
	@Override
	public double getLongitude() {
		return round(super.getLongitude());
	}
	
	public String addressOrCoordinates() {
    	if ( ! "".equals(fullAddress)) {
    		return fullAddress; 
    	}
    	return this.getLatitude()+","+this.getLongitude();
	}
	
	public static String getAddressForLatLong(Context context, double latitude, double longitude){
		Geocoder geocoder = new Geocoder(context, Locale.getDefault());
		String address = "";
		
		try {
			List<Address> addresses = geocoder.getFromLocation(latitude, longitude, 1);
			if (null!=addresses && addresses.size() > 0) {
				if (null!=addresses.get(0)) {
					Address addr = addresses.get(0);
					if (null!=addr.getAddressLine(0)) {
						address = addr.getAddressLine(0);
					}
					if (null!=addr.getAddressLine(1)) {
						if (!"".equals(address)) {
							address += ", ";
						}
						address += addr.getAddressLine(1);
					}
				}
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
		return address;
	}
	
	public static double getLatitudeFromString(String coordinates) {
		double latitude = 0.0;
		
		String[] coords = coordinates.split(",");
		if (coords.length > 0) {
			latitude = new Double(coords[0]);
		}
		
		return latitude;
	}
	
	public static double getLongitudeFromString(String coordinates) {
		double longitude = 0.0;
		
		String[] coords = coordinates.split(",");
		if (coords.length > 0) {
			longitude = new Double(coords[1]);
		}
		
		return longitude;
	}
	
	public static boolean isLatLongQuery(String query) {
		return Pattern.matches("(-?)([0-9]+)((\\.[0-9]+)?),(\\s*)(-?)([0-9]+)((\\.[0-9]+)?)", query);
	}
}
