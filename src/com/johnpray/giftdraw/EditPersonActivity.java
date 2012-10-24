package com.johnpray.giftdraw;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Menu;
import android.support.v4.view.Window;

public class EditPersonActivity extends FragmentActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
        setContentView(R.layout.activity_edit_person);
    }

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		if (getIntent().getIntExtra("mode", EditPersonFragment.MODE_ADD) == EditPersonFragment.MODE_EDIT) {
			getMenuInflater().inflate(R.menu.edit_person, menu);
		}
		return super.onCreateOptionsMenu(menu);
	}
}
