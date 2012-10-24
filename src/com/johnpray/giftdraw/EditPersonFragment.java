package com.johnpray.giftdraw;

import java.util.ArrayList;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.view.MenuItem;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemSelectedListener;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.Toast;

public class EditPersonFragment extends Fragment {

	public static final int MODE_ADD = 0;
	public static final int MODE_EDIT = 1;

	Activity mActivity;
	EditPersonFragment mFragment;
	DBAdapter db;

	private int mode;
	private int personId = -1;

	@Override
	public void onActivityCreated(Bundle savedState) {
		super.onActivityCreated(savedState);
		setHasOptionsMenu(true);

		mActivity = getActivity();
		mFragment = this;
		db = new DBAdapter(mActivity);

		// Check intent for mode
		Intent intent = getActivity().getIntent();
		if (intent != null && intent.getExtras() != null) {
			mode = intent.getIntExtra("mode", MODE_ADD);
			personId = intent.getIntExtra("personId", -1);
		}

		// If trying to edit nothing, add instead
		if (mode == MODE_EDIT && personId < 0) {
			mode = MODE_ADD;
		}

		// Change appearance and functionality based on mode
		switch (mode) {
		case MODE_ADD:
			getSupportActivity().getSupportActionBar().setTitle(
					R.string.add_person);
			populateGroupSpinner();
			break;
		case MODE_EDIT:
			getSupportActivity().getSupportActionBar().setTitle(
					R.string.edit_person);
			populateViews();
			break;
		}

		Button buttonSaveAndAdd = (Button) mActivity
				.findViewById(R.id.buttonSaveAndAdd);
		Button buttonSave = (Button) mActivity.findViewById(R.id.buttonSave);
		Spinner spinnerGroup = (Spinner) mActivity
				.findViewById(R.id.spinnerGroup);

		buttonSave.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				savePerson(false);
			}
		});
		buttonSaveAndAdd.setOnClickListener(new OnClickListener() {
			public void onClick(View v) {
				savePerson(true);
			}
		});
		spinnerGroup.setOnItemSelectedListener(new OnItemSelectedListener() {
			public void onItemSelected(AdapterView<?> parent, View view,
					int pos, long id) {
				personCurrentGroup = pos - 1;
			}

			@SuppressWarnings("rawtypes")
			public void onNothingSelected(AdapterView parent) {
				// Do nothing.
			}
		});
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		return inflater.inflate(R.layout.fragment_edit_person, container);
	}

	String personName = "";
	Integer personCurrentGroup = -1;
	int groupCount = 0;

	void populateViews() {
		db.open();
		Cursor cursor = db.getPersonById(personId);
		cursor.moveToFirst();
		EditText editName = (EditText) mActivity.findViewById(R.id.editName);
		personName = cursor
				.getString(cursor.getColumnIndex(DBAdapter.KEY_NAME));
		editName.setText(personName);
		personCurrentGroup = cursor.getInt(cursor
				.getColumnIndex(DBAdapter.KEY_GROUP_ID));
		cursor.close();
		populateGroupSpinner();
	}

	void populateGroupSpinner() {
		ArrayList<String> names = new ArrayList<String>();
		ArrayList<Integer> personGroupIds = new ArrayList<Integer>();
		ArrayList<String> groupNames = new ArrayList<String>();

		int highestGroupId = -1;

		db.open();
		Cursor cursor = db.getAllPeople();
		int currentGroupId;
		if (cursor.getCount() > 0) {
			cursor.moveToFirst();
			do {
				names.add(cursor.getString(cursor
						.getColumnIndex(DBAdapter.KEY_NAME)));
				currentGroupId = cursor.getInt(cursor
						.getColumnIndex(DBAdapter.KEY_GROUP_ID));
				personGroupIds.add(currentGroupId);
				if (currentGroupId > highestGroupId) {
					highestGroupId = currentGroupId;
				}
			} while (cursor.moveToNext());
		}
		cursor.close();
		db.close();
		
		groupNames.add(" ");
		for (int j = 0; j <= highestGroupId; j++) {
			groupNames.add("Group " + (j+1));
		}
		groupNames.add("New group");
		
		Spinner spinnerGroup = (Spinner) mActivity
				.findViewById(R.id.spinnerGroup);
		ArrayAdapter<String> mAdapter = new ArrayAdapter<String>(mActivity,
				android.R.layout.simple_spinner_item, groupNames);
		mAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
		spinnerGroup.setAdapter(mAdapter);
		spinnerGroup.setSelection(personCurrentGroup + 1);
		
	}

	void savePerson(boolean andAddAnother) {
		db.open();
		EditText editName = (EditText) mActivity.findViewById(R.id.editName);
		if (personCurrentGroup == null) personCurrentGroup = -1;
		switch (mode) {
		case MODE_ADD:
			if (db.insertPerson(editName.getText().toString(),
					personCurrentGroup) >= 0) { // TODO handle giftees
				Toast.makeText(mActivity,
						editName.getText().toString() + " added.",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mActivity, "Add failed.", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		case MODE_EDIT:
			if (db.updatePerson(personId, editName.getText().toString(),personCurrentGroup)) { // TODO handle giftees
				Toast.makeText(mActivity,
						editName.getText().toString() + " updated.",
						Toast.LENGTH_SHORT).show();
			} else {
				Toast.makeText(mActivity, "Update failed.", Toast.LENGTH_SHORT)
						.show();
			}
			break;
		}
		db.close();
		if (andAddAnother) {
			Intent intent = new Intent();
			intent.setClass(mActivity, EditPersonActivity.class);
			intent.putExtra("mode", EditPersonFragment.MODE_ADD);
			startActivity(intent);
			mActivity.finish();
		} else {
			mActivity.finish();
		}
	}

	void deletePersonAfterDialog() {
		AlertDialog.Builder alert = new AlertDialog.Builder(mActivity);
		alert.setTitle(mActivity.getString(R.string.delete) + " " + personName
				+ "?");
		alert.setPositiveButton(R.string.yes,
				new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int whichButton) {
						db.open();
						db.deletePerson(personId);
						db.close();
						mActivity.finish();
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

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.delete:
			deletePersonAfterDialog();
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

}
