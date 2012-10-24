package com.johnpray.giftdraw;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class DashboardActivity extends FragmentActivity {
	
	Activity mActivity;
	boolean namesAlreadyDrawn;
	
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_dashboard);
        mActivity = this;
        
        SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
        namesAlreadyDrawn = sharedPreferences.getBoolean("drawn", false);
        
        // TODO: Disable this button if the people table is empty or count<1
        Button buttonDrawNames = (Button) findViewById(R.id.buttonDrawNames);
        buttonDrawNames.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				if(!namesAlreadyDrawn) {
					performDrawingAndOpenList();
				} else {
					// Just go to the name selection screen
					Intent intent = new Intent();
					intent.setClass(mActivity, ListPeopleActivity.class);
					intent.putExtra("mode", ListPeopleFragment.MODE_DRAW_NAMES);
					startActivity(intent);
				}
				
			}
		});
        
        Button buttonEditPeople = (Button) findViewById(R.id.buttonEditPeople);
        buttonEditPeople.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				Intent intent = new Intent();
				intent.setClass(mActivity, ListPeopleActivity.class);
				intent.putExtra("mode", ListPeopleFragment.MODE_EDIT_NAMES);
				startActivity(intent);
			}
		});
    }
    
    void performDrawingAndOpenList() {
    	GiftDrawing mDrawing = new GiftDrawing(new DBAdapter(mActivity));
    	boolean success = mDrawing.performDrawing();  	
    	if (success) {
    		// Save that names are already drawn
    		SharedPreferences sharedPreferences = getPreferences(MODE_PRIVATE);
    	    SharedPreferences.Editor editor = sharedPreferences.edit();
    	    editor.putBoolean("drawn", true);
    	    editor.commit();
    		// Open name selection screen
    		Intent intent = new Intent();
			intent.setClass(mActivity, ListPeopleActivity.class);
			intent.putExtra("mode", ListPeopleFragment.MODE_DRAW_NAMES);
			startActivity(intent);
    	} else {
    		// Show error dialog
    		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
    		alert.setTitle(R.string.uh_oh);
    		alert.setMessage(R.string.problem_drawing_message);
    		alert.setPositiveButton(R.string.try_again,
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						performDrawingAndOpenList();
    					}
    				});
    		alert.setNegativeButton(R.string.i_ll_check,
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						Intent intent = new Intent();
    						intent.setClass(mActivity, ListPeopleActivity.class);
    						intent.putExtra("mode", ListPeopleFragment.MODE_EDIT_NAMES);
    						startActivity(intent);
    					}
    				});
    		alert.show();
    	}
    }
}