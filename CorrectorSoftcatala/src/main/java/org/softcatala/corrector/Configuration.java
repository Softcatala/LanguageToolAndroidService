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

import android.content.SharedPreferences;

import androidx.preference.PreferenceManager;

import java.util.Date;
import java.util.HashSet;
import java.util.Set;

public class Configuration {
    private static final String languagetoolServerDefault = "https://api.languagetool.org";
    private static final String languageDefault = "system";
    private static final String motherTongueDefault = "";
    private static final Set<String> preferredVariantsDefault = new HashSet<>();

    private static volatile Configuration instance = null;
    private static final String PREF_SERVER = "corrector.softcatala.server";
    private static final String PREF_LANGUAGE = "corrector.softcatala.language";
    private static final String PREF_MOTHER_TONGUE = "corrector.softcatala.mother_tongue";
    private static final String PREF_PREFERRED_VARIANTS = "corrector.softcatala.preferred_variants";
    private static int HttpConnections = 0;
    private static Date LastConnection = null;
    public static SpellCheckerSettingsActivity SettingsActivity;

    public static synchronized Configuration getInstance() {
        if (instance == null) {
            instance = new Configuration();
        }

        return instance;
    }

    public String getServer()
    {
        if (SettingsActivity == null) {
            return languagetoolServerDefault;
        }
        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        return sharedPreferences.getString(PREF_SERVER, languagetoolServerDefault);
    }

    public String setServer(String server)
    {
        if (server.isEmpty()) {
            server = languagetoolServerDefault;
        }

        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        sharedPreference.edit().putString(PREF_SERVER, server).apply();
        return server;
    }

    public int getHttpConnections() {
        return HttpConnections;
    }

    public void incConnections() {
        HttpConnections++;
    }

    public Date getLastConnection() {
        return LastConnection;
    }

    public void setLastConnection(Date date) {
        LastConnection = date;
    }

    public String getLanguage()
    {
        if (SettingsActivity == null) {
            return languageDefault;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        return sharedPreferences.getString(PREF_LANGUAGE, languageDefault);
    }

    public String getMotherTongue()
    {
        if (SettingsActivity == null) {
            return motherTongueDefault;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        return sharedPreferences.getString(PREF_MOTHER_TONGUE, motherTongueDefault);
    }

    public Set<String> getPreferredVariants()
    {
        if (SettingsActivity == null) {
            return preferredVariantsDefault;
        }

        SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        return sharedPreferences.getStringSet(PREF_PREFERRED_VARIANTS, preferredVariantsDefault);
    }

    public void setLanguage(String language) {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        sharedPreference.edit().putString(PREF_LANGUAGE, language).apply();
    }

    public void setMotherTongue(String motherTongue) {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        sharedPreference.edit().putString(PREF_MOTHER_TONGUE, motherTongue).apply();
    }

    public void setPreferredVariants(Set<String> preferredVariants) {
        SharedPreferences sharedPreference = PreferenceManager.getDefaultSharedPreferences(SettingsActivity);
        sharedPreference.edit().putStringSet(PREF_PREFERRED_VARIANTS, preferredVariants).apply();
    }
}
