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

package com.miz.mizuu;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.miz.base.MizActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;

import com.miz.functions.MizLib;
import com.miz.service.MovieLibraryUpdate;
import com.miz.service.TvShowsLibraryUpdate;

public class Update extends MizActivity {

	private CheckBox checkBox, checkBox2;
	private Editor editor;
	private SharedPreferences settings;
	private boolean isMovie;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.update_layout);

		isMovie = getIntent().getExtras().getBoolean("isMovie");

		if (!isMovie)
			setTitle(getString(R.string.updateTvShowsTitle));

		settings = PreferenceManager.getDefaultSharedPreferences(this);
		editor = settings.edit();

		checkBox = (CheckBox) findViewById(R.id.checkBox);
		checkBox.setChecked(false);
		checkBox.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				editor.putBoolean(isMovie ? "prefsClearLibrary" : "prefsClearLibraryTv", isChecked);
				editor.commit();
			}
		});

		checkBox2 = (CheckBox) findViewById(R.id.checkBox2);
		checkBox2.setChecked(false);
		checkBox2.setOnCheckedChangeListener(new OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView,
					boolean isChecked) {
				editor.putBoolean(isMovie ? "prefsRemoveUnavailable" : "prefsRemoveUnavailableTv", isChecked);
				editor.commit();
			}
		});
	}

	@Override
	public void onResume() {
		super.onResume();

		checkBox.setChecked(settings.getBoolean(isMovie ? "prefsClearLibrary" : "prefsClearLibraryTv", false));
		checkBox2.setChecked(settings.getBoolean(isMovie ? "prefsRemoveUnavailable" : "prefsRemoveUnavailableTv", false));
	}

	public void startUpdate(View v) {
		if (isMovie && !MizLib.isMovieLibraryBeingUpdated(this))
			getApplicationContext().startService(new Intent(getApplicationContext(), MovieLibraryUpdate.class));
		else if (!isMovie && !MizLib.isTvShowLibraryBeingUpdated(this))
			getApplicationContext().startService(new Intent(getApplicationContext(), TvShowsLibraryUpdate.class));
		setResult(1); // end activity and reload Main activity

		finish(); // Leave the Update screen once the update has been started
		return;
	}

	@Override
	public void onStart() {
		super.onStart();
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			onBackPressed();
			return true;
		default: return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onPause() {
		super.onPause();
	}

	public void selectSources(View v) {
		// Add the rowID of the selected movie into a Bundle
		Bundle bundle = new Bundle();
		bundle.putBoolean("fromUpdate", true);
		bundle.putBoolean("isMovie", isMovie);
		bundle.putBoolean("completeScan", checkBox.isChecked());

		// Create a new Intent
		Intent intent = new Intent();
		intent.setClass(this, FileSources.class);
		intent.putExtras(bundle);
		startActivity(intent);
	}
}