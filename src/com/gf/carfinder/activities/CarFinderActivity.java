package com.gf.carfinder.activities;

import java.util.ArrayList;
import java.util.Locale;

import com.gf.carfinder.R;
import com.gf.carfinder.fragments.CarFinderFragment;
import com.gf.carfinder.fragments.MenuFragment;
import com.gf.carfinder.utils.CarFinderApplication;
import com.gf.carfinder.utils.LocationManager;
import com.gf.carfinder.utils.SectionPagerAdapter;
import com.gf.carfinder.widgets.GFViewPager;
import com.gf.carfinder.widgets.PagerTitleStrip;
import com.gf.carfinder.widgets.PagerTitleStrip.OnPagerTitleClickListener;

import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.LocalBroadcastManager;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class CarFinderActivity extends FragmentActivity {

    private SectionPagerAdapter mSectionPagerAdapter;
    private GFViewPager mViewPager;
    private Toast mToast;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_car_finder);

        mViewPager = (GFViewPager) findViewById(R.id.pager);
        mSectionPagerAdapter = new SectionPagerAdapter(getSupportFragmentManager(), getFragments(), getFragmentTitles());
        mViewPager.setAdapter(mSectionPagerAdapter);
        mViewPager.setCurrentItem(1);
        PagerTitleStrip pagerTitleStrip = (PagerTitleStrip) mViewPager.findViewById(R.id.pager_title_strip);
        pagerTitleStrip.setOnPagerTitleClickListener(new OnPagerTitleClickListener() {
			@Override
			public void onPagerTitleSelected(int pos) {
				mViewPager.setCurrentItem(pos);
			}
		});
    }
    
    @Override
    protected void onResume() {
    	super.onResume();
    	registerCarFinderActivityReceiver();
    	LocationManager.locate(this);

		Intent broadcast = new Intent(CarFinderApplication.CARFINDER_BROADCAST_ACTION);
		broadcast.putExtra(CarFinderApplication.CARFINDER_RELOAD_MAP_PREF, true);
		LocalBroadcastManager.getInstance(this).sendBroadcast(broadcast);
    }
    
    @Override
    protected void onPause() {
    	super.onPause();
    	unregisterCarFinderActivityReceiver();
    }
    
    @Override
    protected void onDestroy() {
    	super.onDestroy();
    	unregisterCarFinderActivityReceiver();
    }
    
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if ( keyCode == KeyEvent.KEYCODE_MENU ) {
        	if (mViewPager.getCurrentItem() == 0) {
        		mViewPager.setCurrentItem(1);
        	} else {
        		mViewPager.setCurrentItem(0);
        	}
            
            return true;
        }
        if ( keyCode == KeyEvent.KEYCODE_BACK ) {
        	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Leave Car Finder?");
			alertDialog.setMessage("Are you sure you wish to leave?");
			alertDialog.setButton(DialogInterface.BUTTON_POSITIVE, "Yes", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
					finish();
				}
			});
			alertDialog.setButton(DialogInterface.BUTTON_NEGATIVE, "No", new DialogInterface.OnClickListener() {
				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.dismiss();
				}
			});
			alertDialog.show();
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
    
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.car_finder, menu);
        
        return true;
    }
    
    @Override
    public boolean onOptionsItemSelected(MenuItem item){
        switch(item.getItemId()) {
        
        default:
            break;
        }
         
        return true;
    }

	public void registerCarFinderActivityReceiver() {
        LocalBroadcastManager.getInstance(this).registerReceiver(CarFinderActivityReceiver,
        	      new IntentFilter(CarFinderApplication.CARFINDER_BROADCAST_ACTION));
	}
	
	public void unregisterCarFinderActivityReceiver() {
        LocalBroadcastManager.getInstance(this).unregisterReceiver(CarFinderActivityReceiver);
	}
	
	public ArrayList<Fragment> getFragments() {
		ArrayList<Fragment> fragments = new ArrayList<Fragment>();
		fragments.add(new MenuFragment());
		fragments.add(new CarFinderFragment());
		return fragments;
	}

	public ArrayList<String> getFragmentTitles() {
        Locale l = Locale.getDefault();
		ArrayList<String> titles = new ArrayList<String>();
		titles.add(getString(R.string.title_menu_fragment).toUpperCase(l));
		titles.add(getString(R.string.title_carfinder_fragment).toUpperCase(l));
		return titles;
	}
	
	public void makeToast(String message) {
		makeToast(message, Toast.LENGTH_SHORT);
	}
	
	public void makeToast(String message, int length) {
		if (mToast != null) {
			mToast.cancel();
		}
		
		mToast = Toast.makeText(this, message, length);
		mToast.show();
	}
	
    private BroadcastReceiver CarFinderActivityReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			Bundle extras = intent.getExtras();
			if (extras != null) {
				if (extras.containsKey(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA)) {
					mViewPager.setCurrentItem(extras.getInt(CarFinderApplication.CARFINDER_CHANGE_VIEW_PAGE_EXTRA));
				}
			}
		}
    };
}
