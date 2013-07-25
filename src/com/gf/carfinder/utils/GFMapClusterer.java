package com.gf.carfinder.utils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.concurrent.ExecutionException;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Paint.Align;
import android.graphics.Point;
import android.graphics.Rect;
import android.os.AsyncTask;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.util.TypedValue;
import android.view.Display;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Interpolator;
import android.view.animation.LinearInterpolator;

import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.GoogleMap.CancelableCallback;
import com.google.android.gms.maps.GoogleMap.InfoWindowAdapter;
import com.google.android.gms.maps.GoogleMap.OnCameraChangeListener;
import com.google.android.gms.maps.GoogleMap.OnInfoWindowClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapClickListener;
import com.google.android.gms.maps.GoogleMap.OnMapLongClickListener;
import com.google.android.gms.maps.GoogleMap.OnMarkerClickListener;
import com.google.android.gms.maps.Projection;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.GroundOverlayOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

public class GFMapClusterer implements OnMarkerClickListener, OnCameraChangeListener, 
	OnMapClickListener, OnMapLongClickListener, OnInfoWindowClickListener {
	
	private GoogleMap mMap;
	private Context   mContext;
	
	private HashMap<MarkerOptions, Object>            markerOptions;
	private HashMap<Marker, ArrayList<MarkerOptions>> clusters;
	private HashMap<Marker, Object>                   markers;
	
	private Integer sm_cluster_image_resource = null, med_cluster_image_resource = null, 
			        lg_cluster_image_resource = null;
	
	private boolean markerAnimationEnabled = false;
	private boolean isInitialLoad		   = false;
	private boolean didSelectMarker		   = false;
	private double MIN_CLUSTER_DISTANCE    = 100;
	
	private OnMapClustererMarkerClickListener onMapClustererMarkerClickListener;
	private OnMarkerChangeListener            onMarkerChangeListener;
	private OnMapUserInteractionListener      onMapUserInteractionListener;
	private GFMapClustererInfoWindowAdapter  GFMapClustererInfoWindowAdapter;
	
	private CameraUpdate shouldUpdateCameraOnCameraChangeWithCU = null;
	
	private final String LogTag = "GFMapClusterer";
	
	public GFMapClusterer(GoogleMap mMap, Context mContext) {
		this(mMap, mContext, new HashMap<MarkerOptions, Object>(), null);
	}
	
	public GFMapClusterer(GoogleMap mMap,  Context mContext, Double MIN_CLUSTER_DISTANCE) {
		this(mMap, mContext, new HashMap<MarkerOptions, Object>(), MIN_CLUSTER_DISTANCE);
	}
	
	public GFMapClusterer(GoogleMap mMap, Context mContext, HashMap<MarkerOptions, Object> markerOptions) {
		this(mMap, mContext, markerOptions, null);
	}
	
	public GFMapClusterer(GoogleMap mMap, Context mContext, HashMap<MarkerOptions, Object> markerOptions, Double MIN_CLUSTER_DISTANCE) {
		this.mMap = mMap;
		this.mContext = mContext;
		this.markerOptions = markerOptions;
		markers = new HashMap<Marker, Object>();
		clusters = new HashMap<Marker, ArrayList<MarkerOptions>>();
		if (MIN_CLUSTER_DISTANCE != null) {
			this.MIN_CLUSTER_DISTANCE = MIN_CLUSTER_DISTANCE;
		}

		setupListeners();
	}
	
	public GoogleMap getMap() {
		return this.mMap;
	}
	
	public HashMap<MarkerOptions, Object> getMarkerOptions() {
		HashMap<MarkerOptions, Object> markersCopy = new HashMap<MarkerOptions, Object>();
		for (MarkerOptions mO : markerOptions.keySet()) {
			markersCopy.put(mO, markerOptions.get(mO));
		}
		return markersCopy;
	}
	
	public void setOnMapClustererMarkerClickListener(OnMapClustererMarkerClickListener onMapClustererMarkerClickListener) {
		this.onMapClustererMarkerClickListener = onMapClustererMarkerClickListener;
	}
	
	public void setOnMapLoadedListener(OnMarkerChangeListener onMarkerChangeListener) {
		this.onMarkerChangeListener = onMarkerChangeListener;
	}
	
	public void setGFMapClustererInfoWindowAdapter(GFMapClustererInfoWindowAdapter GFMapClustererInfoWindowAdapter) {
		this.GFMapClustererInfoWindowAdapter = GFMapClustererInfoWindowAdapter;
	}
	
	public void setOnMapUserInteractionListener(OnMapUserInteractionListener onMapUserInteractionListener) {
		this.onMapUserInteractionListener = onMapUserInteractionListener;
	}
	
	public void setClusterImageResources(int sm_cluster_image_resource, int med_cluster_image_resource, int lg_cluster_image_resource) {
		this.sm_cluster_image_resource = sm_cluster_image_resource;
		this.med_cluster_image_resource = med_cluster_image_resource;
		this.lg_cluster_image_resource = lg_cluster_image_resource;
	}
	
	private void setupListeners() {
		setupMarkerClickListener();
		setupCameraChangeListener();
		setupMapClickListener();
		setupMapLongClickListener();
		setupInfoWindowAdapter();
		setupInfoWindowClickListener();
	}
	
	private void setupMarkerClickListener() {
		this.mMap.setOnMarkerClickListener(this);
	}
	
	private void setupCameraChangeListener() {
		this.mMap.setOnCameraChangeListener(this);
	}
	
	private void setupMapClickListener() {
		this.mMap.setOnMapClickListener(this);
	}
	
	private void setupMapLongClickListener() {
		this.mMap.setOnMapLongClickListener(this);
	}
	
	private void setupInfoWindowAdapter() {
		this.mMap.setInfoWindowAdapter(new CustomInfoWindowAdapter());
	}
	
	private void setupInfoWindowClickListener() {
		this.mMap.setOnInfoWindowClickListener(this);
	}
	
	public void showUserLocation() {
		Log.d(LogTag+".showUserLocation()", "Adding user location to map");
		this.mMap.setMyLocationEnabled(true);
	}
	
	public void setDistanceInterval(double MIN_CLUSTER_DISTANCE) {
		this.MIN_CLUSTER_DISTANCE = MIN_CLUSTER_DISTANCE;
	}
	
	public MarkerOptions addMarker(Object obj, double latitude, double longitude, int marker_image_resource) {
		MarkerOptions markerOption = new MarkerOptions().position(
				new LatLng(latitude, longitude));
		
    	Bitmap b = BitmapFactory.decodeResource(mContext.getResources(), marker_image_resource);
    	
    	markerOption.icon(BitmapDescriptorFactory.fromBitmap(b));
		markerOptions.put(markerOption, obj);
		Log.d(LogTag+".addMarker()", markerOption.toString());
		
		return markerOption;
	}
	
	public void animateCameraUpdate(CameraUpdate cu) {
		mMap.animateCamera(cu);
	}
	
	public void animateToMarkers(boolean markerAnimationEnabled, boolean circleOverlayEnabled) {
		Log.d(LogTag+".animateToMarkers()", "Loading and animating to map markers");
		this.markerAnimationEnabled = markerAnimationEnabled;
		this.isInitialLoad = true;
		
		LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (MarkerOptions m : markerOptions.keySet()) {
		    builder.include(m.getPosition());
		}
		
		LatLngBounds bounds = builder.build();
		LatLng center = new LatLng((bounds.northeast.latitude+bounds.southwest.latitude)/2, 
				(bounds.northeast.longitude+bounds.southwest.longitude)/2);
		
		WindowManager wm = (WindowManager) mContext.getSystemService(Context.WINDOW_SERVICE);
		Display display = wm.getDefaultDisplay();
		
    	postCameraUpdate(CameraUpdateFactory.newLatLngBounds(bounds, (int)(display.getWidth()/1.5), 
    			(int)(display.getHeight()/1.5), 5));
    	if (circleOverlayEnabled) {
    		addLocationOverlay(center);
    	}
    }
	
	public void updateMapWithMarkers() {
		try {
			Log.d(LogTag+".updateMapWithMarkers()", "Updating map markers");
			clusterMarkers();
		} catch (ExecutionException e) {
			e.printStackTrace();
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public void updateMarkerIcon(MarkerOptions markerOption, int marker_image_resource) {
		Object obj = markerOptions.get(markerOption);
		markerOptions.remove(markerOption);
		addMarker(obj, markerOption.getPosition().latitude, markerOption.getPosition().longitude, marker_image_resource);
		updateMapWithMarkers();
	}
	
	public void removeMapMarkers() {
		Log.d(LogTag+".removeMapMarkers()", "Removing map markers");
		this.mMap.clear();
		this.markers.clear();
		this.markers = new HashMap<Marker, Object>();
		this.clusters.clear();
		this.clusters = new HashMap<Marker, ArrayList<MarkerOptions>>();
	}
	
	public void resetMarkersMap() {
		Log.d(LogTag+".resetMarkersMap()", "Clearing marker/object map");
		this.markerOptions.clear();
		this.markerOptions = new HashMap<MarkerOptions, Object>();
	}
	
	private void postCameraUpdate(CameraUpdate cu) {
		postCameraUpdate(cu, getCameraCallbackListener());
	}
	
	private void postCameraUpdate(CameraUpdate cu, CancelableCallback callback) {
		Log.d(LogTag+".postCameraUpdate()", "Posting new CameraUpdate");
		this.mMap.animateCamera(cu, callback);
	}
	
	private CancelableCallback getCameraCallbackListener() {
		return new CancelableCallback() {
			@Override
			public void onFinish() {
				updateMapWithMarkers();
			}
			
			@Override
			public void onCancel() {
				Log.e(LogTag+".postCameraUpdate().onCancel()", "CameraUpdate cancelled");
			}
		};
	}
	
	@SuppressWarnings("unchecked")
	private void clusterMarkers() throws ExecutionException, InterruptedException {
		Log.d(LogTag+".clusterMarkers()", "Updating clusters on map");
		
        Projection projection = mMap.getProjection();
        LinkedHashMap<MarkerOptions, Point> points = new LinkedHashMap<MarkerOptions, Point>();
        for (MarkerOptions markerOption : markerOptions.keySet()) {
            points.put(markerOption, projection.toScreenLocation(markerOption.getPosition()));
        }

        CheckMarkersTask checkMarkersTask = new CheckMarkersTask();
        checkMarkersTask.execute(points);
    }

    private class CheckMarkersTask extends AsyncTask<LinkedHashMap<MarkerOptions, Point>, Void, LinkedHashMap<Point, ArrayList<MarkerOptions>>> {

        private double findDistance(float x1, float y1, float x2, float y2) {
            return Math.sqrt(((x2 - x1) * (x2 - x1)) + ((y2 - y1) * (y2 - y1)));
        }

        @Override
        protected LinkedHashMap<Point, ArrayList<MarkerOptions>> doInBackground(LinkedHashMap<MarkerOptions, Point>... params) {
        	LinkedHashMap<Point, ArrayList<MarkerOptions>> clusterOptions = new LinkedHashMap<Point, ArrayList<MarkerOptions>>();
            LinkedHashMap<MarkerOptions, Point> points = params[0];
            
            for (MarkerOptions markerOptions : points.keySet()) {
                Point point = points.get(markerOptions);
                double minDistance = -1;
                Point nearestPoint = null;
                double currentDistance;
                for (Point existingPoint : clusterOptions.keySet()) {
                    currentDistance = findDistance(point.x, point.y, existingPoint.x, existingPoint.y);
                    if (currentDistance <= MIN_CLUSTER_DISTANCE) {
                        if ((currentDistance < minDistance) || (minDistance == -1)) {
                            minDistance = currentDistance;
                            nearestPoint = existingPoint;
                        }
                    }
                }

                if (nearestPoint != null) {
                	if (!clusterOptions.get(nearestPoint).contains(markerOptions)) {
                		clusterOptions.get(nearestPoint).add(markerOptions);
                	} else {
                		clusterOptions.get(nearestPoint).remove(markerOptions);
                		clusterOptions.get(nearestPoint).add(markerOptions);
                	}
                } else {
                    ArrayList<MarkerOptions> markersForPoint = new ArrayList<MarkerOptions>();
                    markersForPoint.add(markerOptions);
                    clusterOptions.put(point, markersForPoint);
                }
            }
            return clusterOptions;
        }

        @Override
        protected void onPostExecute(LinkedHashMap<Point, ArrayList<MarkerOptions>> clusterOptions) {
        	HashMap<Marker, ArrayList<MarkerOptions>> clusterSet = new HashMap<Marker, ArrayList<MarkerOptions>>();
        	HashMap<Marker, Object> markerSet = new HashMap<Marker, Object>();
        	
            for (Point point : clusterOptions.keySet()) {
                ArrayList<MarkerOptions> markersForPoint = clusterOptions.get(point);
                MarkerOptions mainMarker = markersForPoint.get(0);
                
                if (markersForPoint.size() > 10) {
                	if (lg_cluster_image_resource != null) {
                		setupClusterMarkerBitmap(lg_cluster_image_resource, mainMarker, markersForPoint, clusterSet);
                	}
                } else if (markersForPoint.size() > 5) {
                	if (med_cluster_image_resource != null) {
                		setupClusterMarkerBitmap(med_cluster_image_resource, mainMarker, markersForPoint, clusterSet);
                	}
                } else if (markersForPoint.size() > 1) {
                	if (sm_cluster_image_resource != null) {
                		setupClusterMarkerBitmap(sm_cluster_image_resource, mainMarker, markersForPoint, clusterSet);
                	}
                } else {
                	addNewMarker(mainMarker, markerSet);
                }
            }
            
            resetMarkers(clusterSet, markerSet);
            
            if (onMarkerChangeListener != null) {
            	onMarkerChangeListener.onMarkerChange(markerOptions, clusters);
            }
        }
        
        private void setupClusterMarkerBitmap(int resource, MarkerOptions markerOption, 
        		ArrayList<MarkerOptions> markersForPoint, HashMap<Marker, ArrayList<MarkerOptions>> clusterSet) {
        	Bitmap immutableBitmap = BitmapFactory.decodeResource(mContext.getResources(), resource);
        	Bitmap bmp = immutableBitmap.copy(Bitmap.Config.ARGB_8888, true);
        	Canvas canvas = new Canvas(bmp);
        	
        	String text = String.valueOf(markersForPoint.size());
        	Paint paint = new Paint();
        	paint.setColor(Color.WHITE);
        	paint.setShadowLayer(2, 1, 1, Color.BLACK);
        	paint.setStrokeWidth(1);
        	paint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,
                    20, mContext.getResources().getDisplayMetrics()));
        	paint.setTextAlign(Align.CENTER);
        	
        	Rect textBounds = new Rect();
            paint.getTextBounds(text, 0, text.length(), textBounds);

        	canvas.drawText(text, bmp.getWidth()/2, bmp.getHeight()/2 + textBounds.height()/2, paint);

        	// copy position into new marker options so we can retain the original marker's icon
        	MarkerOptions mOption = new MarkerOptions().position(
    				new LatLng(markerOption.getPosition().latitude, markerOption.getPosition().longitude));
        	mOption.icon(BitmapDescriptorFactory.fromBitmap(bmp));
            Marker marker = mMap.addMarker(mOption);
            
            if (markerAnimationEnabled) {
	            for (Marker m : clusters.keySet()) {
	            	for (MarkerOptions mO : markersForPoint) {
	            		if (clusters.get(m).contains(mO)) {
	            			animateMarker(marker, m.getPosition(), markerOption.getPosition(), false);
	            		}
	            	}
	            }
            }
            
            clusterSet.put(marker, markersForPoint);
        }
        
        private void addNewMarker(MarkerOptions markerOption, HashMap<Marker, Object> markerSet) {
        	Marker marker = mMap.addMarker(markerOption);
	        if (markerAnimationEnabled) {
	        	for (Marker m : clusters.keySet()) {
	        		if (clusters.get(m).contains(markerOption)) {
	        			animateMarker(marker, m.getPosition(), markerOption.getPosition(), false);
	        		}
	            }
        	}
        	
        	markerSet.put(marker, markerOptions.get(markerOption));
        }
        
        private void animateMarker(final Marker marker, final LatLng origin, final LatLng destination, final boolean shouldRemoveUponCompletion) {
        	marker.setPosition(origin);
        	
        	final long duration = 400;
        	final Handler handler = new Handler();
        	final long start = SystemClock.uptimeMillis();
        	Projection proj = mMap.getProjection();

        	Point startPoint = proj.toScreenLocation(marker.getPosition());
        	final LatLng startLatLng = proj.fromScreenLocation(startPoint);

        	final Interpolator interpolator = new LinearInterpolator();
        	handler.post(new Runnable() {
        	    @Override
        	    public void run() {
        	        long elapsed = SystemClock.uptimeMillis() - start;
        	        float t = interpolator.getInterpolation((float) elapsed / duration);
        	        double lng = t * destination.longitude + (1 - t) * startLatLng.longitude;
        	        double lat = t * destination.latitude + (1 - t) * startLatLng.latitude;
        	        marker.setPosition(new LatLng(lat, lng));
        	        if (t < 1.0) {
        	            handler.postDelayed(this, 10);
        	        } else {
        	        	if (shouldRemoveUponCompletion) {
        	        		marker.remove();
        	        	}
        	        }
        	    }
        	});
        }
        
        private void resetMarkers(HashMap<Marker, ArrayList<MarkerOptions>> clusterSet, HashMap<Marker, Object> markerSet) {
        	for (Marker m : clusters.keySet()) {
            		m.remove();
            }
            for (Marker m : markers.keySet()) {
            		m.remove();
            }
            clusters = clusterSet;
            markers = markerSet;
        }
    }
    
    private void addLocationOverlay(LatLng centerOfCircle) {
        int d = 500;
        Bitmap bm = Bitmap.createBitmap(d, d, Config.ARGB_8888);
        Canvas c = new Canvas(bm);
        Paint p = new Paint();
        p.setColor(Color.RED);
        p.setStrokeWidth(2);
        p.setStyle(Paint.Style.STROKE);
        c.drawCircle(d/2, d/2, d/2, p);

        BitmapDescriptor bmD = BitmapDescriptorFactory.fromBitmap(bm);
        this.mMap.addGroundOverlay(new GroundOverlayOptions().
                image(bmD).
                position(centerOfCircle, getRadiusOfMarkers()/2,getRadiusOfMarkers()/2).
                transparency(0.4f));
    }
    
    private float getRadiusOfMarkers() {
		double furthestDistance = 0;
	    double currentDistance = 0;
	    LatLng furthestLatLng = null;
	    LatLngBounds.Builder builder = new LatLngBounds.Builder();
		for (MarkerOptions m : markerOptions.keySet()) {
		    builder.include(m.getPosition());
		}
		
		LatLngBounds bounds = builder.build();
		LatLng mapCenter = new LatLng((bounds.northeast.latitude+bounds.southwest.latitude)/2, 
				(bounds.northeast.longitude+bounds.southwest.longitude)/2);
	    
	    for(MarkerOptions mO : markerOptions.keySet()) {
	        LatLng markerLocation = new LatLng(mO.getPosition().latitude, mO.getPosition().longitude);
	        
	        currentDistance = calculateDistance(markerLocation, mapCenter);
	        
	        if(currentDistance > furthestDistance) {
	        	furthestLatLng = markerLocation;
	            furthestDistance = currentDistance;
	        }
	    }
	    float radius = (float)Math.sqrt(Math.pow(furthestLatLng.longitude - mapCenter.longitude, 2) 
	    		+ Math.pow(furthestLatLng.latitude - mapCenter.latitude, 2)) + 40;
	    
	    Log.d(LogTag+".getRadiusOfView()", "Radius: "+radius);
	    return radius*1000;
	}
    
    private double calculateDistance(LatLng pointA, LatLng pointB) {
		double theta = pointA.longitude - pointB.longitude;
		double dist = Math.sin((pointA.latitude * Math.PI / 180.0))
				* Math.sin((pointB.latitude * Math.PI / 180.0))
				+ Math.cos((pointA.longitude * Math.PI / 180.0))
				* Math.cos((pointB.longitude * Math.PI / 180.0))
				* Math.cos((theta * Math.PI / 180.0));
		dist = Math.acos(dist);
		dist = (dist * 180.0 / Math.PI);
		dist = dist * 60 * 1.1515;
		dist = dist * 1609.344; // meters;
		return (dist*10);
	}

	@Override
	public boolean onMarkerClick(Marker marker) {
		Log.d(LogTag+".onMarkerClick()", marker.getPosition().toString());
		
		if (clusters.keySet().contains(marker)) {
			ArrayList<MarkerOptions> markersForPoint = clusters.get(marker);
			LatLngBounds.Builder builder = new LatLngBounds.Builder();
			for (MarkerOptions m : markersForPoint) {
			    builder.include(m.getPosition());
			}
			
			LatLngBounds bounds = builder.build();
			
			Log.d(LogTag+".onMarkerClick()", "Updating camera bounds: " + bounds.toString());
			
			// CameraUpdate applied as so due to onCancel() being called in CancelableCallback 
			// if updating camera here. This happens due to the camera being positioned above 
			// the selected marker when clicked at this time.
			int padding = markersForPoint.size() < 5 ? 125 : 75;
			shouldUpdateCameraOnCameraChangeWithCU = CameraUpdateFactory.newLatLngBounds(bounds, padding);
			
			if (onMapClustererMarkerClickListener != null) {
				onMapClustererMarkerClickListener.onClusterClick(marker, markersForPoint);
			}
		} else {
			didSelectMarker = true;
			if (onMapClustererMarkerClickListener != null) {
				onMapClustererMarkerClickListener.onMarkerClick(marker, markers.get(marker));
			}
		}
		
		return false;
	}

	@Override
	public void onCameraChange(CameraPosition position) {
		if (shouldUpdateCameraOnCameraChangeWithCU != null) {
			postCameraUpdate(shouldUpdateCameraOnCameraChangeWithCU);
			shouldUpdateCameraOnCameraChangeWithCU = null;
		} else {
			if (!didSelectMarker) {
				updateMapWithMarkers();
			} else {
				didSelectMarker = false;
			}
		}
		
		if (onMapUserInteractionListener != null) {
			if (!isInitialLoad) {
				onMapUserInteractionListener.mapReceivedUserInteraction();
			}  else {
				checkInitialZoomLevel(position);
			}
		} else {
			checkInitialZoomLevel(position);
		}
	}
	
	private void checkInitialZoomLevel(CameraPosition position) {
		if (isInitialLoad) {
			if (position.zoom > 17 && markerOptions.size() == 1) {
				postCameraUpdate(CameraUpdateFactory.newLatLngZoom(position.target, 17.0f));
			}
			isInitialLoad = false;
		}
	}
	
	@Override
	public void onMapLongClick(LatLng latLng) {
		Log.d(LogTag+".onMapLongClick()", latLng.toString());
		if (onMapUserInteractionListener != null) {
			onMapUserInteractionListener.mapReceivedUserInteraction();
		}
	}

	@Override
	public void onMapClick(LatLng latLng) {
		Log.d(LogTag+".onMapClick()", latLng.toString());
		if (onMapUserInteractionListener != null) {
			onMapUserInteractionListener.mapReceivedUserInteraction();
		}
	}
	
	@Override
	public void onInfoWindowClick(Marker marker) {
		if (GFMapClustererInfoWindowAdapter != null) {
    		GFMapClustererInfoWindowAdapter.onGFMarkerInfoWindowClick(marker, markers.get(marker));
    	}
	}
	
	private class CustomInfoWindowAdapter implements InfoWindowAdapter {
        @Override
        public View getInfoWindow(Marker marker) {  
        	// return an inflated xml file as the info window && 
        	// getInfoContents is not called if this returns null
        	if (GFMapClustererInfoWindowAdapter != null && !clusters.keySet().contains(marker)) {
	        	return GFMapClustererInfoWindowAdapter.getGFMarkerInfoWindow(marker, markers.get(marker));
        	}
            return null;
        }           

        @Override
        public View getInfoContents(Marker marker) {
        	// return an inflated xml file but use the default window background && 
        	// this method is not called if getInfoWindow(Marker) does not return null
        	if (GFMapClustererInfoWindowAdapter != null && !clusters.keySet().contains(marker)) {
	        	return GFMapClustererInfoWindowAdapter.getGFMarkerInfoWindowContents(marker, markers.get(marker));
        	}
        	return null;
        }
    }
	
	public interface OnMapClustererMarkerClickListener {
		// passes object associated with selected marker
		public void onMarkerClick(Marker marker, Object obj);
		
		// passes cluster marker as well as the contained maker options within this cluster
		public void onClusterClick(Marker cluster, ArrayList<MarkerOptions> markers);
	}
	
	public interface OnMapUserInteractionListener {
		public void mapReceivedUserInteraction();
	}
	
	// used so map user class can inflate custom layout for the info window and receive click event callbacks
	public interface GFMapClustererInfoWindowAdapter {
		public View getGFMarkerInfoWindow(Marker marker, Object obj);
        public View getGFMarkerInfoWindowContents(Marker marker, Object obj);
        public void onGFMarkerInfoWindowClick(Marker marker, Object obj);
    }
	
	public interface OnMarkerChangeListener {
		public void onMarkerChange(HashMap<MarkerOptions, Object> allMarkers, HashMap<Marker, ArrayList<MarkerOptions>> clusters);
	}
}
