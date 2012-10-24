package com.johnpray.giftdraw;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.ListFragment;
import android.support.v4.view.Menu;
import android.support.v4.view.MenuItem;
import android.text.Html;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

public class ListPeopleFragment extends ListFragment {

	public static final int MODE_EDIT_NAMES = 0;
	public static final int MODE_DRAW_NAMES = 1;

	Activity mActivity;
	ListPeopleFragment mFragment;
	DBAdapter db;
	String intentMessage;
	String intentSubject;

	private int mode = -1;

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		setHasOptionsMenu(true);

		mActivity = getActivity();
		mFragment = this;
		db = new DBAdapter(mActivity);

		// Check intent for mode
		if (getActivity().getIntent() != null
				&& getActivity().getIntent().getExtras() != null)
			mode = getActivity().getIntent().getIntExtra("mode",
					MODE_EDIT_NAMES);

		// Change appearance and functionality based on mode
		switch (mode) {
		case MODE_EDIT_NAMES:
			getSupportActivity().getSupportActionBar().setTitle(
					R.string.edit_people);
			// TODO
			break;
		case MODE_DRAW_NAMES:
			getSupportActivity().getSupportActionBar().setTitle(
					R.string.draw_names);
			// TODO
			break;
		}

		setListShown(true);
	}

	@Override
	public void onResume() {
		super.onResume();
		loadNames();
	}

	ArrayList<String> names;
	ArrayList<Integer> personIds;
	ArrayList<String> personGroupNames;
	ArrayList<Integer> personGroupIds;
	ArrayList<String> groupNames;
	int peopleCount;

	void loadNames() {
		setListAdapter(null);
		names = new ArrayList<String>();
		personIds = new ArrayList<Integer>();
		personGroupNames = new ArrayList<String>();
		personGroupIds = new ArrayList<Integer>();
		groupNames = new ArrayList<String>();
		db.open();
		Cursor cursor = null;
		switch (mode) {
		case MODE_EDIT_NAMES:
			cursor = db.getAllPeopleSortedByGroup();
			break;
		case MODE_DRAW_NAMES:
			cursor = db.getAllPeople();
			break;
		}
		peopleCount = cursor.getCount();
		Log.d("GiftDraw", "peopleCount=" + peopleCount);
		if (peopleCount > 0) {
			cursor.moveToFirst();
			int i = 0;
			do {
				Log.d("GiftDraw", "Cursor index = " + cursor.getPosition());
				names.add(cursor.getString(cursor
						.getColumnIndex(DBAdapter.KEY_NAME)));
				Log.d("GiftDraw", "name=" + names.get(i));
				personIds.add(cursor.getInt(cursor
						.getColumnIndex(DBAdapter.KEY_PERSON_ID)));
				personGroupIds.add(cursor.getInt(cursor
						.getColumnIndex(DBAdapter.KEY_GROUP_ID)));
				if (personGroupIds.get(i) < 0) {
					personGroupNames.add("");
				} else {
					personGroupNames
							.add("Group " + (personGroupIds.get(i) + 1));
				}
				i++;
			} while (cursor.moveToNext());

			PeopleAdapter mAdapter = new PeopleAdapter(mActivity, names,
					personGroupNames, personIds, personGroupIds);
			setListAdapter(mAdapter);
		}
		cursor.close();
		db.close();
	}

	void addPerson() {
		// Start Edit Person activity in Add mode
		// TODO: On tablets, populate second fragment instead of starting
		// activity
		Intent intent = new Intent();
		intent.setClass(mActivity, EditPersonActivity.class);
		intent.putExtra("mode", EditPersonFragment.MODE_ADD);
		startActivity(intent);
	}

	void editPerson(int personId) {
		// Start Edit Person activity in Edit mode
		// TODO: On tablets, populate second fragment instead of starting
		// activity
		Intent intent = new Intent();
		intent.setClass(mActivity, EditPersonActivity.class);
		intent.putExtra("mode", EditPersonFragment.MODE_EDIT);
		intent.putExtra("personId", personId);
		startActivity(intent);
	}

	void deleteAllPeopleAfterDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
		alert.setTitle(R.string.delete_everyone_);
		alert.setMessage(R.string.cannot_be_undone);
		alert.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						db.open();
						db.deleteAllPeople();
						db.close();
						loadNames();
					}
				});

		alert.setNegativeButton(R.string.no,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();
	}

	void showGifteeNames(int personId) {
		db.open();
		Cursor cursor = db.getPersonById(personId);
		cursor.moveToFirst();

		String personName = cursor.getString(cursor
				.getColumnIndex(DBAdapter.KEY_NAME));
		int giftee1Id = cursor.getInt(cursor
				.getColumnIndex(DBAdapter.KEY_GIFTEE_1_ID));
		int giftee2Id = cursor.getInt(cursor
				.getColumnIndex(DBAdapter.KEY_GIFTEE_2_ID));

		cursor = db.getPersonById(giftee1Id);
		cursor.moveToFirst();
		String giftee1Name = cursor.getString(cursor
				.getColumnIndex(DBAdapter.KEY_NAME));

		cursor = db.getPersonById(giftee2Id);
		cursor.moveToFirst();
		String giftee2Name = cursor.getString(cursor
				.getColumnIndex(DBAdapter.KEY_NAME));

		cursor.close();
		db.close();

		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
		alert.setTitle(personName + ", " + getString(R.string.your_names_are)
				+ "…");
		alert.setMessage(Html.fromHtml("<big><b>" + giftee1Name + "</b></big> "
				+ getString(R.string.and) + " <big><b>" + giftee2Name
				+ "</b></big>"));
		intentMessage = giftee1Name + " " + getString(R.string.and) + " "
				+ giftee2Name;
		intentSubject = personName + ", " + getString(R.string.email_subject);
		alert.setPositiveButton(R.string.send_results,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						Intent emailIntent = new Intent(
								android.content.Intent.ACTION_SEND);
						emailIntent.setType("plain/text");
						emailIntent.putExtra(
								android.content.Intent.EXTRA_SUBJECT,
								intentSubject);
						emailIntent.putExtra(android.content.Intent.EXTRA_TEXT,
								intentMessage);
						mActivity.startActivity(Intent.createChooser(
								emailIntent,
								getString(R.string.send_email_using) + "…"));
					}
				});

		alert.setNegativeButton(R.string.i_ll_remember,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						// Canceled.
					}
				});

		alert.show();

	}
	
	void performRedrawing() {
		db.open();
		db.resetDrawing();
		// Save that names are not drawn
		SharedPreferences sharedPreferences = mActivity.getPreferences(Activity.MODE_PRIVATE);
	    SharedPreferences.Editor editor = sharedPreferences.edit();
	    editor.putBoolean("drawn", false);
	    editor.commit();
    	GiftDrawing mDrawing = new GiftDrawing(new DBAdapter(mActivity));
    	boolean success = mDrawing.performDrawing();  	
    	if (success) {
    		// Save that names are already drawn
    	    editor.putBoolean("drawn", true);
    	    editor.commit();
    		// Open name selection screen
    		Intent intent = new Intent();
			intent.setClass(mActivity, ListPeopleActivity.class);
			intent.putExtra("mode", ListPeopleFragment.MODE_DRAW_NAMES);
			startActivity(intent);
			mActivity.finish();
    	} else {
    		// Show error dialog
    		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
    		alert.setTitle(R.string.uh_oh);
    		alert.setMessage(R.string.problem_drawing_message);
    		alert.setPositiveButton(R.string.try_again,
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						performRedrawing();
    					}
    				});
    		alert.setNegativeButton(R.string.i_ll_check,
    				new DialogInterface.OnClickListener() {
    					public void onClick(DialogInterface dialog, int whichButton) {
    						Intent intent = new Intent();
    						intent.setClass(mActivity, ListPeopleActivity.class);
    						intent.putExtra("mode", ListPeopleFragment.MODE_EDIT_NAMES);
    						startActivity(intent);
    						mActivity.finish();
    					}
    				});
    		alert.show();
    	}
    }

	@Override
	public void onListItemClick(ListView l, View v, int position, long id) {
		switch (mode) {
		case MODE_EDIT_NAMES:
			editPerson(personIds.get(position));
			break;
		case MODE_DRAW_NAMES:
			showGifteeNames(personIds.get(position));
			break;
		}
	}

	@Override
	public void onCreateOptionsMenu(Menu menu,
			android.view.MenuInflater inflater) {
		super.onCreateOptionsMenu(menu, inflater);
		inflater.inflate(R.menu.edit_people, menu);
		inflater.inflate(R.menu.draw_names, menu);
	}
	
	

	@Override
	public void onPrepareOptionsMenu(Menu menu) {
		switch (mode) {
		case MODE_EDIT_NAMES:
			menu.removeItem(R.id.redraw);
			break;
		case MODE_DRAW_NAMES:
			menu.removeItem(R.id.add);
			menu.removeItem(R.id.delete_all);
			break;
		}
		super.onPrepareOptionsMenu(menu);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.add:
			addPerson();
			return true;
		case R.id.delete_all:
			deleteAllPeopleAfterDialog();
			return true;
		case R.id.redraw:
			
			return true;
		case android.R.id.home:
			// On icon press, go to Dashboard
			Intent intent2 = new Intent();
			intent2.setClass(mActivity, DashboardActivity.class);
			intent2.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
			startActivity(intent2);
			return true;
		}
		return super.onOptionsItemSelected(item);
	}

	class PeopleAdapter extends BaseAdapter {
		private LayoutInflater mInflater;
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<String> personGroupNames = new ArrayList<String>();
		ArrayList<Integer> personIds = new ArrayList<Integer>();
		ArrayList<Integer> personGroupIds = new ArrayList<Integer>();

		public PeopleAdapter(Context context, ArrayList<String> names,
				ArrayList<String> personGroupNames,
				ArrayList<Integer> personIds, ArrayList<Integer> personGroupIds) {
			mInflater = LayoutInflater.from(context);
			this.names.addAll(names);
			this.personGroupNames.addAll(personGroupNames);
			this.personIds.addAll(personIds);
			this.personGroupIds.addAll(personGroupIds);
		}

		public View getView(int position, View convertView, ViewGroup parent) {
			if (convertView == null) {
				convertView = mInflater.inflate(
						R.layout.list_item_icon_two_lines, parent, false);
			}
			TextView name = (TextView) convertView.findViewById(R.id.text1);
			TextView group = (TextView) convertView.findViewById(R.id.text2);
			ImageView image = (ImageView) convertView.findViewById(R.id.image);

			name.setText(names.get(position));
			group.setText(personGroupNames.get(position));
			switch (personGroupIds.get(position)) {
			case -1:
				image.setImageResource(android.R.color.transparent);
				break;
			case 0:
				image.setImageResource(R.drawable.ic_dashboard_mistletoe);
				break;
			case 1:
				image.setImageResource(R.drawable.ic_dashboard_snowflake);
				break;
			case 2:
				image.setImageResource(R.drawable.ic_dashboard_gingerbread);
				break;
			case 3:
				image.setImageResource(R.drawable.ic_dashboard_gift);
				break;
			default:
				image.setImageResource(android.R.color.transparent);
				// TODO: Make unique icons work for more than four groups
			}

			return convertView;
		}

		public int getCount() {
			return names.size();
		}

		public String getItem(int position) {
			return names.get(position);
		}

		public long getItemId(int position) {
			return personIds.get(position);
		}
	}

}
