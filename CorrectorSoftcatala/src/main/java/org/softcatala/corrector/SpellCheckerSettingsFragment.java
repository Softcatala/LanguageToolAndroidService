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

import android.os.Bundle;
import android.preference.Preference;
import android.preference.EditTextPreference;
import android.preference.PreferenceFragment;
import android.util.Log;

import android.preference.ListPreference;
import android.preference.MultiSelectListPreference;

import java.util.Date;
import java.util.Set;


/**
 * Preference screen.
 */

public class SpellCheckerSettingsFragment extends PreferenceFragment {

    private static final String TAG = SpellCheckerSettingsFragment.class
            .getSimpleName();

    @Override
    public void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        addPreferencesFromResource(R.xml.spell_checker_settings);

        setHttpConnections();
        setVersion();
        setServer();
        setLanguageChangeListener();
        setMotherTongueChangeListener();
        setPreferredVariantsChangeListener();
    }

    private void setServer() {
        EditTextPreference serverField = ((EditTextPreference) findPreference("server"));
        serverField.setSummary(Configuration.getInstance().getServer());

        serverField.setOnPreferenceChangeListener((preference, newValue) -> {
            String newServer = newValue.toString();
            newServer = Configuration.getInstance().setServer(newServer);
            ((EditTextPreference) preference).setSummary(newServer);
            return true;
        });
    }

    private void setLanguageChangeListener() {
        ListPreference languageField = ((ListPreference) findPreference("language"));
        languageField.setOnPreferenceChangeListener((preference, newValue) -> {
            Configuration.getInstance().setLanguage(newValue.toString());
            return true;
        });
    }

    private void setMotherTongueChangeListener() {
        ListPreference motherTongueField = ((ListPreference) findPreference("mother_tongue"));
        motherTongueField.setOnPreferenceChangeListener((preference, newValue) -> {
            Configuration.getInstance().setMotherTongue(newValue.toString());
            return true;
        });
    }

    @SuppressWarnings("unchecked")
    private void setPreferredVariantsChangeListener() {
        MultiSelectListPreference preferredVariantsField = ((MultiSelectListPreference) findPreference("preferred_variants"));
        preferredVariantsField.setOnPreferenceChangeListener((preference, newValue) -> {
            Configuration.getInstance().setPreferredVariants((Set<String>) newValue);
            return true;
        });
    }

    private void setVersion() {
        Date buildDate = BuildConfig.buildTime;
        Preference version = findPreference("version");
        String v = String.format(getResources().getString(R.string.version_text), getVersion(), buildDate);
        version.setSummary(v);
    }

    private String getVersion() {
        try {
            return getActivity().getPackageManager().getPackageInfo(getActivity().getPackageName(), 0).versionName;
        } catch (Exception e) {
            return null;
        }
    }

    private void setHttpConnections() {
        Preference http = findPreference("http");
        Date lastConnection = Configuration.getInstance().getLastConnection();
        String status = String.format(getResources().getString(R.string.connections),
                Configuration.getInstance().getHttpConnections(),
                lastConnection == null ? getResources().getString(R.string.connection_none)
                        : lastConnection.toString());

        http.setSummary(status);
    }
}