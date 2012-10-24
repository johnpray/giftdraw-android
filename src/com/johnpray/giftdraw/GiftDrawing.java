package com.johnpray.giftdraw;

import java.util.ArrayList;
import java.util.Random;

import android.database.Cursor;
import android.database.SQLException;
import android.util.Log;

/**
 * Provides all the logic for determining who ends up with what giftee.
 * 
 * @author John P. Pray
 * 
 */
public class GiftDrawing {

	DBAdapter db;
	int numTries = 0;

	public GiftDrawing(DBAdapter db) {
		this.db = db;
	}

	public boolean performDrawing() {

		numTries++;

		db.open();

		Cursor cursor = db.getAllPeople();

		int tempPersonId;
		int tempGroupId;

		cursor.moveToFirst();
		// For each person
		int tries;
		do {
			tempPersonId = cursor.getInt(cursor
					.getColumnIndex(DBAdapter.KEY_PERSON_ID));
			tempGroupId = cursor.getInt(cursor
					.getColumnIndex(DBAdapter.KEY_GROUP_ID));
			tries = 0;
			while (!drawForOnePerson(tempPersonId, tempGroupId)) {
				tries++;
				if (tries >= 10) {
					// If drawing for this person has failed 10 times, start the
					// whole drawing over
					break;
				}
			}
			if (tries >= 10)
				break;
		} while (cursor.moveToNext());

		if (tries >= 250) {
			if (numTries < 250) {
				return performDrawing();
			} else {
				// If doing the whole drawing from scratch has failed 10 times,
				// just forget about it
				db.close();
				return false;
			}
		}

		db.close();
		return true;
	}

	public boolean drawForOnePerson(int tempPersonId, int tempGroupId) {
		try {
			int tempGiftee1Id;
			int tempGiftee2Id;

			// Randomly select two people not in the same group (if in a group)
			// and who aren't the same person
			tempGiftee1Id = getRandomOtherPerson(tempPersonId, -1, tempGroupId);
			Log.d("GiftDraw", "tempGiftee1Id = " + tempGiftee1Id);
			tempGiftee2Id = getRandomOtherPerson(tempPersonId, tempGiftee1Id,
					tempGroupId);
			Log.d("GiftDraw", "tempGiftee2Id = " + tempGiftee2Id);

			// Update person with giftee ids
			db.setGiftee1(tempPersonId, tempGiftee1Id);
			db.setGiftee2(tempPersonId, tempGiftee2Id);

			// Update numTimesSelected values on giftees
			int giftee1TimesSelected = db.getNumTimesSelected(tempGiftee1Id);
			db.setNumTimesSelected(tempGiftee1Id, giftee1TimesSelected + 1);
			int giftee2TimesSelected = db.getNumTimesSelected(tempGiftee2Id);
			db.setNumTimesSelected(tempGiftee2Id, giftee2TimesSelected + 1);
		} catch (SQLException e) {
			return false;
		} catch (IllegalArgumentException e) {
			return false;
		}
		return true;
	}

	/**
	 * 
	 * @param notThisPersonId
	 *            for null, use -1
	 * @param notThisPersonIdEither
	 *            for null, use -1
	 * @param notThisGroupId
	 *            for null, use -1
	 * @return personId of the random person
	 */
	int getRandomOtherPerson(int notThisPersonId, int notThisPersonIdEither,
			int notThisGroupId) throws IllegalArgumentException {
		ArrayList<Integer> personIds;
		Cursor cursor;
		if (notThisGroupId < 0) {
			// There's no group
			cursor = db.getAllPeople();
		} else {
			// There's a group
			cursor = db.getPeopleNotInGroupId(notThisGroupId);
		}
		personIds = new ArrayList<Integer>();
		cursor.moveToFirst();
		int tempPersonId;
		int tempTimesSelectedCount;
		do {
			// Gather each person's id, except for the passed-in ones and ones
			// already chosen twice
			// TODO: Make the number of drawn names configurable
			tempPersonId = cursor.getInt(cursor
					.getColumnIndex(DBAdapter.KEY_PERSON_ID));
			tempTimesSelectedCount = cursor.getInt(cursor
					.getColumnIndex(DBAdapter.KEY_NUM_TIMES_SELECTED));
			if (tempTimesSelectedCount < 2 && tempPersonId != notThisPersonId
					&& tempPersonId != notThisPersonIdEither) {
				personIds.add(tempPersonId);
			}
		} while (cursor.moveToNext());
		// Pick an id randomly from the personIds array
		// If the array is empty, an IllegalArgumentException is thrown
		return personIds.get(new Random().nextInt(personIds.size()));
	}
}
