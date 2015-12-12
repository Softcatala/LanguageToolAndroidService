/*
 * Copyright (C) 2015 Jordi Mas i Hernàndez <jmas@softcatala.org>
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as
 * published by the Free Software Foundation; either version 2 of the
 * License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * General Public License for more details.
 *
 * You should have received a copy of the GNU General Public
 * License along with this program; if not, write to the
 * Free Software Foundation, Inc., 59 Temple Place - Suite 330,
 * Boston, MA 02111-1307, USA.
 */

package org.softcatala.corrector;

import org.softcatala.corrector.R;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.util.Log;

/**
 * Preference screen.
 */

public class SpellCheckerSettingsFragment extends PreferenceFragment {

    private static String PREF_DIALECT = "corrector.softcatala.dialect";
    private static final String TAG = SampleSpellCheckerService.class
            .getSimpleName();

    static int HttpConnections = 0;

    public static SpellCheckerSettingsActivity SettingsActivity;

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.spell_checker_settings);

        SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        Boolean dialect = spref.getBoolean(PREF_DIALECT, false);

        CheckBoxPreference cb = (CheckBoxPreference) findPreference("dialect");
        cb.setChecked(dialect);
        Log.d(TAG, "onCreateFragment");

        Preference http = findPreference("http");
        http.setSummary(Integer.toString(HttpConnections));

        cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
            public boolean onPreferenceChange(Preference preference, Object newValue) {
                SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
                Boolean dialect = spref.getBoolean(PREF_DIALECT, false);
                spref.edit().putBoolean(PREF_DIALECT, !dialect).commit();
                Log.d(TAG, "Pref " + preference.getKey() + " changed to " + newValue.toString());
                return true;
            }
        });
    }
}