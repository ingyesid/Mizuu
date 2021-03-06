/*
 * Copyright (C) 2014 Michell Bak
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.miz.contentprovider;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.regex.Pattern;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.SearchRecentSuggestionsProvider;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;
import android.provider.BaseColumns;
import android.util.Log;

import com.miz.db.DbAdapter;
import com.miz.functions.MediumMovie;
import com.miz.functions.MizLib;
import com.miz.mizuu.MizuuApplication;

public class MovieContentProvider extends SearchRecentSuggestionsProvider {

	static final String TAG = MovieContentProvider.class.getSimpleName();
	public static final String AUTHORITY = MovieContentProvider.class.getName();
	public static final int MODE = DATABASE_MODE_QUERIES | DATABASE_MODE_2LINES;
	private static final String[] COLUMNS = {
		BaseColumns._ID, // must include this column
		SearchManager.SUGGEST_COLUMN_TEXT_1, // First line (title)
		SearchManager.SUGGEST_COLUMN_TEXT_2, // Second line (smaller text)
		SearchManager.SUGGEST_COLUMN_INTENT_DATA, // Icon
		SearchManager.SUGGEST_COLUMN_ICON_1, // Icon
		SearchManager.SUGGEST_COLUMN_INTENT_EXTRA_DATA, // Movie ID
		SearchManager.SUGGEST_COLUMN_INTENT_ACTION,
		SearchManager.SUGGEST_COLUMN_SHORTCUT_ID };

	public MovieContentProvider() {
		setupSuggestions(AUTHORITY, MODE);
	}

	@Override
	public Cursor query(Uri uri, String[] projection, String selection,
			String[] selectionArgs, String sortOrder) {

		String query = selectionArgs[0];
		if (query == null || query.length() == 0) {
			return null;
		}

		MatrixCursor cursor = new MatrixCursor(COLUMNS);

		try {
			List<MediumMovie> list = getSearchResults(query);
			for (int i = 0; i < list.size(); i++) {
				cursor.addRow(createRow(Integer.valueOf(i), list.get(i).getTitle(), list.get(i).getReleaseYear().replace("(", "").replace(")", ""), Uri.fromFile(new File(list.get(i).getThumbnail())).toString(), list.get(i).getRowId()));
			}
		} catch (Exception e) {
			Log.e(TAG, "Failed to lookup " + query, e);
		}
		return cursor;
	}

	@Override
	public Uri insert(Uri uri, ContentValues values) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int delete(Uri uri, String selection, String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	@Override
	public int update(Uri uri, ContentValues values, String selection,
			String[] selectionArgs) {
		throw new UnsupportedOperationException();
	}

	private Object[] createRow(Integer id, String text1, String text2, String icon, String rowId) {
		return new Object[] {
				id, // _id
				text1, // text1
				text2, // text2
				icon,
				icon,
				rowId,
				"android.intent.action.SEARCH", // action
				SearchManager.SUGGEST_NEVER_MAKE_SHORTCUT };
	}

	private List<MediumMovie> getSearchResults(String query) {
		List<MediumMovie> movies = new ArrayList<MediumMovie>();
		if (!MizLib.isEmpty(query)) {

			DbAdapter db = MizuuApplication.getMovieAdapter();

			query = query.toLowerCase(Locale.ENGLISH);

			Cursor c = db.fetchAllMovies(DbAdapter.KEY_TITLE + " ASC", false, false);
			String title = ""; // Reuse String variable
			Pattern p = Pattern.compile(MizLib.CHARACTER_REGEX); // Use a pre-compiled pattern as it's a lot faster (approx. 3x for ~700 movies)
			
			while (c.moveToNext()) {
				title = c.getString(c.getColumnIndex(DbAdapter.KEY_TITLE)).toLowerCase(Locale.ENGLISH);

				if (title.indexOf(query) != -1 ||  p.matcher(title).replaceAll("").indexOf(query) != -1) {
					movies.add(new MediumMovie(getContext(),
							c.getString(c.getColumnIndex(DbAdapter.KEY_ROWID)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_FILEPATH)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_TITLE)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_TMDBID)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_RATING)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_RELEASEDATE)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_GENRES)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_FAVOURITE)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_CAST)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_COLLECTION)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_EXTRA_2)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_TO_WATCH)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_HAS_WATCHED)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_EXTRA_1)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_CERTIFICATION)),
							c.getString(c.getColumnIndex(DbAdapter.KEY_RUNTIME)),
							false,
							true
							));
				}
			}
			
			Collections.sort(movies);
		}
		return movies;
	}
}