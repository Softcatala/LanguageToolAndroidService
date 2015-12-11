/**
 * Copyright (C) 2011 The Android Open Source Project
 * <p/>
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy
 * of the License at
 * <p/>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p/>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */

package org.softcatala.corrector;

import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.preference.PreferenceManager;
import android.content.SharedPreferences;
import android.preference.CheckBoxPreference;
import android.preference.PreferenceFragment;
import android.preference.Preference;
import android.util.Log;

import static android.preference.PreferenceManager.*;


/**
 * Spell checker preference screen.
 */
public class SpellCheckerSettingsActivity extends PreferenceActivity {

    static String PREF_DIALECT = "corrector.softcatala.dialect";
    private static final String TAG = SampleSpellCheckerService.class
            .getSimpleName();

    public static class MyPreferenceFragment extends PreferenceFragment {
        public SpellCheckerSettingsActivity SettingsActivity;

        @Override
        public void onCreate(final Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.spell_checker_settings);

            //SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
            //Boolean dialect = spref.getBoolean(PREF_DIALECT, false);

            //CheckBoxPreference cb = (CheckBoxPreference) findPreference("dialect");
            //cb.setChecked(dialect);
            Log.d(TAG, "onCreateFragment");

            /*cb.setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object newValue) {
                    SharedPreferences spref = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
                    Boolean dialect = spref.getBoolean(PREF_DIALECT, false);
                    //spref.edit().putBoolean(PREF_DIALECT, !dialect).commit();
                    Log.d(TAG, "Pref " + preference.getKey() + " changed to " + newValue.toString());
                    return true;
                }
            });*/
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        MyPreferenceFragment frag = new MyPreferenceFragment();
        frag.SettingsActivity = this;
        getFragmentManager().beginTransaction().replace(android.R.id.content, frag).commit();
        Log.d(TAG, "onCreate");
    }

    @Override
    public Intent getIntent() {
        final Intent modIntent = new Intent(super.getIntent());
        modIntent.putExtra(EXTRA_SHOW_FRAGMENT, SpellCheckerSettingsFragment.class.getName());
        modIntent.putExtra(EXTRA_NO_HEADERS, true);
        return modIntent;
    }

    @Override
    protected boolean isValidFragment(String fragmentName) {
        return SpellCheckerSettingsFragment.class.getName().equals(fragmentName);
    }

    //http://stackoverflow.com/questions/6496450/android-checkbox-preference
}
