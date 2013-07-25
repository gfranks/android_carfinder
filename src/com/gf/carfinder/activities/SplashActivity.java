package com.gf.carfinder.activities;

import com.gf.carfinder.R;
import com.gf.carfinder.utils.CarFinderApplication;
import com.gf.carfinder.utils.LocationManager;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.Animation.AnimationListener;
import android.widget.ImageView;
import android.widget.TextView;

public class SplashActivity extends Activity {

	@Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        
        if(CarFinderApplication.DEVELOPMENT_MODE) {
        	TextView dev = (TextView)findViewById(R.id.devModeLabel);
        	dev.setVisibility(View.VISIBLE);
        }
    }
	
	@Override
	protected void onResume() {
		super.onResume();
		LocationManager.locate(this);
		
		Animation mAnim = AnimationUtils.loadAnimation(this, R.anim.simple);
		mAnim.setDuration(2000);    		
		mAnim.setAnimationListener(new AnimationListener(){
			public void onAnimationEnd(Animation animation) {
				Intent intent = new Intent(SplashActivity.this, CarFinderActivity.class);
				startActivity(intent);
				finish();
			}

			public void onAnimationRepeat(Animation animation) { }

			public void onAnimationStart(Animation animation) { }
		});
		
		ImageView logo = (ImageView)findViewById(R.splash.splashLogo);
		logo.startAnimation(mAnim);
	}
}
