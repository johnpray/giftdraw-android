package com.johnpray.giftdraw;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.view.Window;

public class ListPeopleActivity extends FragmentActivity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_ACTION_BAR_ITEM_TEXT);
        setContentView(R.layout.activity_list_people);
    }
}
